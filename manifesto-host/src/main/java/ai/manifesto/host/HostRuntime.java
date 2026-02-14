package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.core.Apply;
import ai.manifesto.core.core.Compute;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.host.runtime.ContinueComputeJob;
import ai.manifesto.host.runtime.ExecutionKey;
import ai.manifesto.host.runtime.FulfillRequirementsJob;
import ai.manifesto.host.runtime.HostJob;
import ai.manifesto.host.runtime.HostMailbox;
import ai.manifesto.host.runtime.HostRunner;
import ai.manifesto.host.runtime.InMemoryHostMailbox;
import ai.manifesto.host.runtime.StartIntentJob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: HostRuntime는 Core compute-effect 루프를 실행해 requirement를 처리하고 snapshot을 수렴시키는 런타임입니다.
 * EN: HostRuntime runs the Core compute-effect loop, fulfills requirements, and converges snapshots.
 */
public final class HostRuntime {
    private final Map<String, EffectHandler> handlers = new HashMap<>();
    private final EffectExecutor effectExecutor = new EffectExecutor();

    public HostRuntime register(String effectType, EffectHandler handler) {
        Objects.requireNonNull(effectType, "effectType is required");
        Objects.requireNonNull(handler, "handler is required");
        handlers.put(effectType, handler);
        return this;
    }

    public ComputeResult run(DomainSchema schema, Snapshot snapshot, Intent intent, int timeoutSeconds)
        throws Exception {
        return run(schema, snapshot, intent, HostRuntimeOptions.forTimeoutSeconds(timeoutSeconds));
    }

    public ComputeResult run(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        HostRuntimeOptions options
    ) throws Exception {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        Objects.requireNonNull(intent, "intent is required");
        Objects.requireNonNull(options, "options is required");
        if (intent.getIntentId() == null || intent.getIntentId().isBlank()) {
            throw new IllegalArgumentException("intent.intentId is required");
        }

        HostRunContext runContext = new HostRunContext(schema, snapshot, intent, options);
        ExecutionKey executionKey = ExecutionKey.fromIntentId(intent.getIntentId());
        HostMailbox mailbox = new InMemoryHostMailbox(executionKey);
        HostRunner runner = new HostRunner(
            mailbox,
            (job, currentMailbox) -> processJob(job, currentMailbox, runContext),
            options.getTraceSink()
        );
        runner.enqueue(new StartIntentJob(intent));
        runner.runUntilIdle();

        if (runContext.getFinalResult() != null) {
            return runContext.getFinalResult();
        }
        return ComputeResult.error(runContext.getCurrentSnapshot(), runContext.getLastTrace());
    }

    private void processJob(HostJob job, HostMailbox mailbox, HostRunContext runContext) throws Exception {
        if (runContext.getFinalResult() != null) {
            return;
        }
        switch (job.getType()) {
            case START_INTENT -> handleStartIntent((StartIntentJob) job, mailbox);
            case CONTINUE_COMPUTE -> handleContinueCompute((ContinueComputeJob) job, mailbox, runContext);
            case FULFILL_REQUIREMENTS -> handleFulfillRequirements((FulfillRequirementsJob) job, mailbox, runContext);
            default -> throw new IllegalStateException("Unsupported job type: " + job.getType());
        }
    }

    private void handleStartIntent(StartIntentJob job, HostMailbox mailbox) {
        mailbox.enqueue(new ContinueComputeJob(job.getIntent()));
    }

    private void handleContinueCompute(
        ContinueComputeJob job,
        HostMailbox mailbox,
        HostRunContext runContext
    ) throws Exception {
        HostRuntimeOptions options = runContext.getOptions();
        if (runContext.getComputeIterations() >= options.getMaxIterations()) {
            runContext.setFinalResult(ComputeResult.error(runContext.getCurrentSnapshot(), runContext.getLastTrace()));
            return;
        }
        runContext.incrementComputeIterations();
        Snapshot currentSnapshot = runContext.getCurrentSnapshot();
        HostContext computeContext = HostContext.forSnapshot(currentSnapshot);
        ComputeResult result = Compute.computeSync(
            runContext.getSchema(),
            currentSnapshot,
            job.getIntent(),
            computeContext,
            options.getTimeoutSeconds()
        );
        runContext.setLastTrace(result.getTrace());
        if (result.getStatus() != ComputeStatus.PENDING) {
            runContext.setFinalResult(result);
            return;
        }
        if (result.getRequirements().isEmpty()) {
            runContext.setFinalResult(result);
            return;
        }
        mailbox.enqueue(new FulfillRequirementsJob(result, job.getIntent()));
    }

    private void handleFulfillRequirements(
        FulfillRequirementsJob job,
        HostMailbox mailbox,
        HostRunContext runContext
    ) {
        ComputeResult pendingResult = job.getPendingResult();
        List<Requirement> requirements = pendingResult.getRequirements();
        if (requirements.isEmpty()) {
            mailbox.enqueue(new ContinueComputeJob(job.getIntent()));
            emitContinueEnqueue(runContext);
            return;
        }

        List<Patch> patches = new ArrayList<>();
        Intent intent = job.getIntent();
        patches.add(Patch.set("$host.currentIntentId", intent.getIntentId()));
        patches.add(Patch.set("$host.intentSlots." + intent.getIntentId() + ".type", intent.getType()));
        patches.add(Patch.set("$host.intentSlots." + intent.getIntentId() + ".input", intent.getInput()));
        for (Requirement requirement : requirements) {
            EffectHandler handler = handlers.get(requirement.getType());
            if (handler == null) {
                runContext.setFinalResult(pendingResult);
                return;
            }
            EffectExecutionOutcome effectOutcome = effectExecutor.execute(
                handler,
                requirement,
                runContext.getExecutionKey(),
                intent.getIntentId(),
                runContext.getComputeIterations(),
                pendingResult.getSnapshot(),
                runContext.getOptions(),
                runContext.getOptions().getTraceSink()
            );
            if (!effectOutcome.isSuccess()) {
                Snapshot failed = applyHostFailure(
                    runContext.getSchema(),
                    pendingResult.getSnapshot(),
                    requirement,
                    "HOST_EFFECT_FAILED",
                    effectOutcome.error() == null ? "Effect execution failed" : effectOutcome.error().message(),
                    effectOutcome.error()
                );
                runContext.setFinalResult(ComputeResult.error(failed, runContext.getLastTrace()));
                return;
            }
            patches.addAll(effectOutcome.result().getPatches());
        }
        patches.add(Patch.set("system.pendingRequirements", java.util.List.of()));

        HostContext applyContext = HostContext.forSnapshot(pendingResult.getSnapshot());
        Result<Snapshot, ErrorValue> applied = Apply.apply(
            runContext.getSchema(),
            pendingResult.getSnapshot(),
            patches,
            applyContext
        );
        if (applied.isErr()) {
            runContext.setFinalResult(ComputeResult.error(pendingResult.getSnapshot(), runContext.getLastTrace()));
            return;
        }
        runContext.setCurrentSnapshot(applied.unwrap());
        mailbox.enqueue(new ContinueComputeJob(intent));
        emitContinueEnqueue(runContext);
    }

    private void emitContinueEnqueue(HostRunContext runContext) {
        runContext.getOptions().getTraceSink().onEvent(
            new ai.manifesto.host.runtime.HostRuntimeTraceEvent(
                "continue:enqueue",
                runContext.getExecutionKey(),
                null,
                null,
                null,
                null
            )
        );
    }

    private Snapshot applyHostFailure(
        DomainSchema schema,
        Snapshot snapshot,
        Requirement requirement,
        String code,
        String message,
        EffectExecutionError executionError
    ) {
        Map<String, Object> errorMap = new LinkedHashMap<>();
        errorMap.put("code", code);
        errorMap.put("message", message);
        errorMap.put("requirementId", requirement.getId());
        errorMap.put("requirementType", requirement.getType());
        if (executionError != null) {
            errorMap.put("effectErrorCode", executionError.code().name());
            errorMap.put("effectAttempts", executionError.attempts());
            errorMap.put("effectRetryable", executionError.retryable());
        }

        List<Patch> patches = new java.util.ArrayList<>();
        patches.add(Patch.set("$host.lastError", errorMap));
        @SuppressWarnings("unchecked")
        Map<String, Object> existingHostState = (Map<String, Object>) snapshot.getData().get("$host");
        List<Object> nextErrors = new java.util.ArrayList<>();
        if (existingHostState != null && existingHostState.get("errors") instanceof List<?> list) {
            nextErrors.addAll(list);
        }
        nextErrors.add(errorMap);
        patches.add(Patch.set("$host.errors", nextErrors));
        patches.add(Patch.set("system.pendingRequirements", java.util.List.of()));

        HostContext applyContext = HostContext.forSnapshot(snapshot);
        Result<Snapshot, ErrorValue> applied = Apply.apply(schema, snapshot, patches, applyContext);
        if (applied.isOk()) {
            return applied.unwrap();
        }
        return snapshot;
    }

    /**
     * KR: 단일 intent 실행 동안의 가변 상태(현재 snapshot, trace, 반복 횟수, 최종 결과)를 보관합니다.
     * EN: Mutable execution state for a single intent run (snapshot, trace, iterations, final result).
     */
    private static final class HostRunContext {
        private final DomainSchema schema;
        private final HostRuntimeOptions options;
        private final String executionKey;
        private Snapshot currentSnapshot;
        private TraceGraph lastTrace;
        private ComputeResult finalResult;
        private int computeIterations;

        private HostRunContext(
            DomainSchema schema,
            Snapshot currentSnapshot,
            Intent intent,
            HostRuntimeOptions options
        ) {
            this.schema = Objects.requireNonNull(schema, "schema is required");
            this.currentSnapshot = Objects.requireNonNull(currentSnapshot, "currentSnapshot is required");
            Intent requiredIntent = Objects.requireNonNull(intent, "intent is required");
            this.options = Objects.requireNonNull(options, "options is required");
            this.executionKey = requiredIntent.getIntentId();
            this.computeIterations = 0;
        }

        public DomainSchema getSchema() {
            return schema;
        }

        public HostRuntimeOptions getOptions() {
            return options;
        }

        public String getExecutionKey() {
            return executionKey;
        }

        public Snapshot getCurrentSnapshot() {
            return currentSnapshot;
        }

        public void setCurrentSnapshot(Snapshot currentSnapshot) {
            this.currentSnapshot = Objects.requireNonNull(currentSnapshot, "currentSnapshot is required");
        }

        public TraceGraph getLastTrace() {
            return lastTrace;
        }

        public void setLastTrace(TraceGraph lastTrace) {
            this.lastTrace = lastTrace;
        }

        public ComputeResult getFinalResult() {
            return finalResult;
        }

        public void setFinalResult(ComputeResult finalResult) {
            this.finalResult = Objects.requireNonNull(finalResult, "finalResult is required");
        }

        public int getComputeIterations() {
            return computeIterations;
        }

        public void incrementComputeIterations() {
            this.computeIterations += 1;
        }
    }
}
