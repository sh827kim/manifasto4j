package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.comparison.Eq;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.runtime.HostRuntimeTraceEvent;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HostComplianceMatrixTest {

    @Test
    void mailboxJobRunnerOrderingIsDeterministic() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:ordering",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.If.of(
                    new Eq(new Get("status"), new Lit("ok")),
                    FlowNode.Halt.of("done"),
                    FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
                ))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        List<HostRuntimeTraceEvent> trace = new java.util.ArrayList<>();
        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", Map.of(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().traceSink(trace::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        long jobStart = trace.stream().filter(e -> "job:start".equals(e.type())).count();
        long jobEnd = trace.stream().filter(e -> "job:end".equals(e.type())).count();
        long runnerStart = trace.stream().filter(e -> "runner:start".equals(e.type())).count();
        long runnerEnd = trace.stream().filter(e -> "runner:end".equals(e.type())).count();
        assertEquals(jobStart, jobEnd);
        assertEquals(runnerStart, runnerEnd);
        assertTrue(trace.stream().anyMatch(e -> "continue:enqueue".equals(e.type())));
    }

    @Test
    void fulfillPhaseClearsPendingRequirementsOnSuccess() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:fulfill",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.If.of(
                    new Eq(new Get("status"), new Lit("ok")),
                    FlowNode.Halt.of("done"),
                    FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
                ))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        ComputeResult result = host.run(schema, snapshot, new Intent("notify", Map.of(), "intent-fulfill"), 5);

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertTrue(result.getSnapshot().getSystem().getPendingRequirements().isEmpty());
        assertEquals("ok", result.getSnapshot().getData().get("status"));
    }

    @Test
    void applyPatchesStageRunsBetweenFulfillAndNextCompute() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:apply-patches-stage",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.If.of(
                    new Eq(new Get("status"), new Lit("ok")),
                    FlowNode.Halt.of("done"),
                    FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
                ))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        List<HostRuntimeTraceEvent> trace = new java.util.ArrayList<>();
        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", Map.of(), "intent-apply-stage"),
            HostRuntimeOptions.builder().traceSink(trace::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        List<String> startedJobTypes = trace.stream()
            .filter(e -> "job:start".equals(e.type()))
            .map(HostRuntimeTraceEvent::jobType)
            .toList();

        int fulfillIndex = startedJobTypes.indexOf("FULFILL_REQUIREMENTS");
        int applyIndex = startedJobTypes.indexOf("APPLY_PATCHES");
        int secondComputeIndex = startedJobTypes.lastIndexOf("CONTINUE_COMPUTE");
        assertTrue(fulfillIndex >= 0);
        assertTrue(applyIndex > fulfillIndex);
        assertTrue(secondComputeIndex > applyIndex);
        assertTrue(trace.stream().anyMatch(e -> "applyPatches:enqueue".equals(e.type())));
        assertTrue(trace.stream().anyMatch(e -> "applyPatches:success".equals(e.type())));
    }

    @Test
    void traceReplayModelPreservesRunnerAndJobInvariants() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:trace-replay",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.If.of(
                    new Eq(new Get("status"), new Lit("ok")),
                    FlowNode.Halt.of("done"),
                    FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
                ))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        List<HostRuntimeTraceEvent> trace = new java.util.ArrayList<>();

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", Map.of(), "intent-trace-replay"),
            HostRuntimeOptions.builder().traceSink(trace::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertTrue(replayTrace(trace));
    }

    @Test
    void missingHandlerKeepsIntentPendingAndRetainsRequirement() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:missing",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi"))))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));

        HostRuntime host = new HostRuntime();
        ComputeResult result = host.run(schema, snapshot, new Intent("notify", Map.of(), "intent-missing"), 5);

        assertEquals(ComputeStatus.PENDING, result.getStatus());
        assertFalse(result.getRequirements().isEmpty());
        assertNotNull(result.getRequirements().get(0).getType());
    }

    @Test
    void contextAwareHandlerReceivesExecutionMetadata() throws Exception {
        DomainSchema schema = schema(
            "urn:test:host:matrix:context",
            new ActionSpec.Builder("notify")
                .flow(FlowNode.If.of(
                    new Eq(new Get("status"), new Lit("ok")),
                    FlowNode.Halt.of("done"),
                    FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
                ))
                .build(),
            new FieldSpec("status", "string", false, "")
        );
        Snapshot snapshot = snapshot(schema, Map.of("status", ""));
        AtomicReference<EffectExecutionContext> received = new AtomicReference<>();

        HostRuntime host = new HostRuntime()
            .register("host.notify", new ContextAwareEffectHandler() {
                @Override
                public EffectResult handle(Map<String, Object> params, EffectExecutionContext context) {
                    received.set(context);
                    return EffectResult.of(List.of(Patch.set("status", "ok")));
                }
            });

        ComputeResult result = host.run(schema, snapshot, new Intent("notify", Map.of(), "intent-context"), 5);
        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertNotNull(received.get());
        assertEquals("intent-context", received.get().intentId());
    }

    private DomainSchema schema(String id, ActionSpec action, FieldSpec... fields) {
        DomainSchema.Builder temp = new DomainSchema.Builder(id, "1.0.0").addAction(action);
        for (FieldSpec field : fields) {
            temp.addDataField(field);
        }
        String hash = ValidationUtils.computeSchemaHash(temp.hash("").build());
        DomainSchema.Builder builder = new DomainSchema.Builder(id, "1.0.0").hash(hash).addAction(action);
        for (FieldSpec field : fields) {
            builder.addDataField(field);
        }
        return builder.build();
    }

    private Snapshot snapshot(DomainSchema schema, Map<String, Object> data) {
        return Snapshot.builder()
            .data(new HashMap<>(data))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();
    }

    private boolean replayTrace(List<HostRuntimeTraceEvent> events) {
        int activeRunners = 0;
        java.util.Deque<String> jobQueue = new java.util.ArrayDeque<>();
        for (HostRuntimeTraceEvent event : events) {
            if ("runner:start".equals(event.type())) {
                activeRunners += 1;
                if (activeRunners > 1) {
                    return false;
                }
                continue;
            }
            if ("runner:end".equals(event.type())) {
                activeRunners -= 1;
                if (activeRunners < 0) {
                    return false;
                }
                continue;
            }
            if ("job:start".equals(event.type())) {
                jobQueue.addLast(event.jobType());
                continue;
            }
            if ("job:end".equals(event.type())) {
                if (jobQueue.isEmpty()) {
                    return false;
                }
                String started = jobQueue.removeFirst();
                if (!started.equals(event.jobType())) {
                    return false;
                }
            }
        }
        return activeRunners == 0 && jobQueue.isEmpty();
    }
}
