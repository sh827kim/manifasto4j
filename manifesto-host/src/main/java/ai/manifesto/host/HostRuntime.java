package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.core.Apply;
import ai.manifesto.core.core.Compute;
import ai.manifesto.core.schema.DomainSchema;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HostRuntime - compute-effect loop 최소 구현
 */
public final class HostRuntime {
    private final Map<String, EffectHandler> handlers = new HashMap<>();

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

        Snapshot current = snapshot;
        int iteration = 0;
        TraceGraph lastTrace = null;
        while (true) {
            if (iteration >= options.getMaxIterations()) {
                return ComputeResult.error(current, lastTrace);
            }
            iteration += 1;
            HostContext computeContext = HostContext.forSnapshot(current);
            ComputeResult result = Compute.computeSync(
                schema,
                current,
                intent,
                computeContext,
                options.getTimeoutSeconds()
            );
            lastTrace = result.getTrace();
            if (result.getStatus() != ComputeStatus.PENDING) {
                return result;
            }

            List<Requirement> requirements = result.getRequirements();
            if (requirements.isEmpty()) {
                return result;
            }
            List<Patch> patches = new java.util.ArrayList<>();
            patches.add(Patch.set("$host.currentIntentId", intent.getIntentId()));
            patches.add(Patch.set("$host.intentSlots." + intent.getIntentId() + ".type", intent.getType()));
            patches.add(Patch.set("$host.intentSlots." + intent.getIntentId() + ".input", intent.getInput()));
            for (Requirement requirement : requirements) {
                EffectHandler handler = handlers.get(requirement.getType());
                if (handler == null) {
                    return result;
                }
                EffectResult effectResult = executeEffectWithPolicy(handler, requirement, options);
                if (effectResult == null) {
                    Snapshot failed = applyHostFailure(
                        schema,
                        result.getSnapshot(),
                        requirement,
                        "HOST_EFFECT_FAILED",
                        "Effect execution failed after retries"
                    );
                    return ComputeResult.error(failed, lastTrace);
                }
                patches.addAll(effectResult.getPatches());
            }
            // Host는 처리된 requirement를 명시적으로 비운다.
            patches.add(Patch.set("system.pendingRequirements", java.util.List.of()));

            HostContext applyContext = HostContext.forSnapshot(result.getSnapshot());
            Result<Snapshot, ErrorValue> applied = Apply.apply(schema, result.getSnapshot(), patches, applyContext);
            if (applied.isErr()) {
                return ComputeResult.error(result.getSnapshot(), lastTrace);
            }
            current = applied.unwrap();
        }
    }

    private EffectResult executeEffectWithPolicy(
        EffectHandler handler,
        Requirement requirement,
        HostRuntimeOptions options
    ) {
        int attempts = options.getMaxEffectRetries() + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                long startedAt = System.nanoTime();
                EffectResult effectResult = handler.handle(requirement.getParams());
                long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000L;
                long maxDuration = options.getMaxEffectDurationMillis();
                if (maxDuration > 0L && elapsedMillis > maxDuration) {
                    continue;
                }
                return effectResult;
            } catch (RuntimeException error) {
                // retry path
            }
        }
        return null;
    }

    private Snapshot applyHostFailure(
        DomainSchema schema,
        Snapshot snapshot,
        Requirement requirement,
        String code,
        String message
    ) {
        Map<String, Object> errorMap = new LinkedHashMap<>();
        errorMap.put("code", code);
        errorMap.put("message", message);
        errorMap.put("requirementId", requirement.getId());
        errorMap.put("requirementType", requirement.getType());

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
}
