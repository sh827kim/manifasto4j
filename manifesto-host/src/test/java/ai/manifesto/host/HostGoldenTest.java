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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Host Golden Tests")
class HostGoldenTest {
    private final HostGoldenVectorHarness harness = new HostGoldenVectorHarness();
    private static final List<String> SCENARIO_FIXTURES = List.of(
        "golden/host/scenarios/todo-workflow.json",
        "golden/host/scenarios/trace-snapshot.json",
        "golden/host/scenarios/complex-effects.json",
        "golden/host/scenarios/determinism.json"
    );

    @Test
    @DisplayName("Host 골든 시나리오(deteminism/trace-snapshot/complex-effects/todo-workflow)가 기대값과 일치")
    void hostGoldenCases() throws Exception {
        List<Map<String, Object>> vectors = harness.loadVectors(SCENARIO_FIXTURES);
        assertFalse(vectors.isEmpty(), "Golden scenario vectors should not be empty");

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            assertNotNull(expected, "Expected golden data missing for: " + name);

            GoldenCaseResult result = switch (name) {
                case "todo-workflow-effect-applied" -> runEffectAppliedCase();
                case "todo-workflow-missing-handler" -> runMissingHandlerCase();
                case "todo-workflow-effect-failure" -> runEffectFailureRecordedCase();
                case "trace-snapshot-invariants" -> runTraceInvariantCase();
                case "complex-effects-chained-reinjection" -> runTraceInvariantChainedCase();
                case "determinism-repeatable" -> runDeterminismCase();
                default -> throw new IllegalArgumentException("Unknown host golden case: " + name);
            };

            Map<String, Object> actual = normalize(result, expected);
            harness.assertJsonEquals(expected, actual, "Golden mismatch: " + name);
        }
    }

    private GoldenCaseResult runEffectAppliedCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:1");
        Snapshot snapshot = createSnapshot(schema);
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-1");
        return new GoldenCaseResult(host.run(schema, snapshot, intent, 5), List.of());
    }

    private GoldenCaseResult runMissingHandlerCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:2");
        Snapshot snapshot = createSnapshot(schema);
        HostRuntime host = new HostRuntime();
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-2");
        return new GoldenCaseResult(host.run(schema, snapshot, intent, 5), List.of());
    }

    private GoldenCaseResult runEffectFailureRecordedCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:3");
        Snapshot snapshot = createSnapshot(schema);
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> {
                throw new RuntimeException("boom");
            });
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-3");
        return new GoldenCaseResult(host.run(
            schema,
            snapshot,
            intent,
            HostRuntimeOptions.builder().maxEffectRetries(1).build()
        ), List.of());
    }

    private GoldenCaseResult runTraceInvariantCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:4");
        Snapshot snapshot = createSnapshot(schema);
        java.util.List<HostRuntimeTraceEvent> events = new java.util.ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        Intent intent = new Intent("notify", Map.of(), "intent-host-golden-4");
        ComputeResult result = host.run(
            schema,
            snapshot,
            intent,
            HostRuntimeOptions.builder().traceSink(events::add).build()
        );
        return new GoldenCaseResult(result, events);
    }

    private GoldenCaseResult runTraceInvariantChainedCase() throws Exception {
        FlowNode flow = FlowNode.Seq.of(List.of(
            FlowNode.If.of(
                new ai.manifesto.core.expr.type.IsNull(new Get("step1")),
                FlowNode.Effect.of("host.step1", Map.of())
            ),
            FlowNode.If.of(
                new ai.manifesto.core.expr.logical.And(List.of(
                    new Get("step1"),
                    new ai.manifesto.core.expr.type.IsNull(new Get("step2"))
                )),
                FlowNode.Effect.of("host.step2", Map.of())
            ),
            FlowNode.If.of(
                new ai.manifesto.core.expr.logical.And(List.of(
                    new Get("step2"),
                    new ai.manifesto.core.expr.type.IsNull(new Get("step3"))
                )),
                FlowNode.Effect.of("host.step3", Map.of())
            ),
            FlowNode.If.of(
                new Eq(new Get("step3"), new Lit(true)),
                FlowNode.Halt.of("done")
            )
        ));
        ActionSpec action = new ActionSpec.Builder("chain").flow(flow).build();

        DomainSchema schema = createSchemaWithFields(
            "urn:test:host:golden:5",
            action,
            List.of(
                new FieldSpec("step1", "boolean", false, null),
                new FieldSpec("step2", "boolean", false, null),
                new FieldSpec("step3", "boolean", false, null)
            )
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("step1", null);
        data.put("step2", null);
        data.put("step3", null);
        Snapshot snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();

        java.util.List<HostRuntimeTraceEvent> events = new java.util.ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.step1", params -> EffectResult.of(List.of(Patch.set("step1", true))))
            .register("host.step2", params -> EffectResult.of(List.of(Patch.set("step2", true))))
            .register("host.step3", params -> EffectResult.of(List.of(Patch.set("step3", true))));
        Intent intent = new Intent("chain", Map.of(), "intent-host-golden-5");
        ComputeResult result = host.run(
            schema,
            snapshot,
            intent,
            HostRuntimeOptions.builder().traceSink(events::add).build()
        );
        return new GoldenCaseResult(result, events);
    }

    private GoldenCaseResult runDeterminismCase() throws Exception {
        DomainSchema schema = createNotifySchema("urn:test:host:golden:determinism");
        Snapshot leftSnapshot = createSnapshot(schema);
        Snapshot rightSnapshot = createSnapshot(schema);

        List<HostRuntimeTraceEvent> leftEvents = new java.util.ArrayList<>();
        List<HostRuntimeTraceEvent> rightEvents = new java.util.ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        Intent leftIntent = new Intent("notify", Map.of(), "intent-host-golden-determinism");
        Intent rightIntent = new Intent("notify", Map.of(), "intent-host-golden-determinism");
        ComputeResult left = host.run(schema, leftSnapshot, leftIntent, HostRuntimeOptions.builder().traceSink(leftEvents::add).build());
        ComputeResult right = host.run(schema, rightSnapshot, rightIntent, HostRuntimeOptions.builder().traceSink(rightEvents::add).build());
        return new GoldenCaseResult(left, leftEvents, right, rightEvents);
    }

    private DomainSchema createNotifySchema(String schemaId) {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(schemaId, "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder(schemaId, "1.0.0")
            .hash(hash)
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""))
            .build();
    }

    private DomainSchema createSchemaWithFields(String schemaId, ActionSpec action, List<FieldSpec> fields) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(schemaId, "1.0.0")
            .addAction(action);
        for (FieldSpec field : fields) {
            tempBuilder.addDataField(field);
        }
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(schemaId, "1.0.0")
            .hash(hash)
            .addAction(action);
        for (FieldSpec field : fields) {
            builder.addDataField(field);
        }
        return builder.build();
    }

    private Snapshot createSnapshot(DomainSchema schema) {
        return Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, 1L, "seed", schema.getHash()))
            .build();
    }

    private Map<String, Object> normalize(GoldenCaseResult run, Map<String, Object> expected) {
        ComputeResult result = run.result();
        Map<String, Object> out = new LinkedHashMap<>();
        if (expected.containsKey("status")) {
            out.put("status", result.getStatus().name());
        }
        if (expected.containsKey("dataStatus")) {
            out.put("dataStatus", result.getSnapshot().getData().get("status"));
        }
        if (expected.containsKey("hostCurrentIntentId")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
            Object currentIntentId = hostState != null ? hostState.get("currentIntentId") : null;
            out.put("hostCurrentIntentId", currentIntentId);
        }
        if (expected.containsKey("hasRequirements")) {
            out.put("hasRequirements", !result.getRequirements().isEmpty());
        }
        if (expected.containsKey("hostLastErrorCode")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
            @SuppressWarnings("unchecked")
            Map<String, Object> lastError = hostState == null
                ? null
                : (Map<String, Object>) hostState.get("lastError");
            out.put("hostLastErrorCode", lastError == null ? null : lastError.get("code"));
        }
        if (expected.containsKey("hostErrorsCount")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
            int count = 0;
            if (hostState != null && hostState.get("errors") instanceof List<?> list) {
                count = list.size();
            }
            out.put("hostErrorsCount", count);
        }
        if (expected.containsKey("traceRunnerKickCount")) {
            long count = run.traceEvents().stream().filter(e -> "runner:kick".equals(e.type())).count();
            out.put("traceRunnerKickCount", (int) count);
        }
        if (expected.containsKey("traceHasContinueEnqueue")) {
            boolean hasEvent = run.traceEvents().stream().anyMatch(e -> "continue:enqueue".equals(e.type()));
            out.put("traceHasContinueEnqueue", hasEvent);
        }
        if (expected.containsKey("traceHasRunnerRecheck")) {
            boolean hasEvent = run.traceEvents().stream().anyMatch(e -> "runner:recheck".equals(e.type()));
            out.put("traceHasRunnerRecheck", hasEvent);
        }
        if (expected.containsKey("traceJobStartEqualsEnd")) {
            long jobStart = run.traceEvents().stream().filter(e -> "job:start".equals(e.type())).count();
            long jobEnd = run.traceEvents().stream().filter(e -> "job:end".equals(e.type())).count();
            out.put("traceJobStartEqualsEnd", jobStart == jobEnd);
        }
        if (expected.containsKey("traceContinueEnqueueCount")) {
            long count = run.traceEvents().stream().filter(e -> "continue:enqueue".equals(e.type())).count();
            out.put("traceContinueEnqueueCount", (int) count);
        }
        if (expected.containsKey("traceRunnerStartEqualsEnd")) {
            long runnerStart = run.traceEvents().stream().filter(e -> "runner:start".equals(e.type())).count();
            long runnerEnd = run.traceEvents().stream().filter(e -> "runner:end".equals(e.type())).count();
            out.put("traceRunnerStartEqualsEnd", runnerStart == runnerEnd);
        }
        if (expected.containsKey("traceSingleRunnerInvariant")) {
            out.put("traceSingleRunnerInvariant", hasSingleRunnerInvariant(run.traceEvents()));
        }
        if (expected.containsKey("step3Done")) {
            out.put("step3Done", result.getSnapshot().getData().get("step3"));
        }
        if (expected.containsKey("determinismStatusEqual")) {
            out.put("determinismStatusEqual", run.secondResult() != null
                && result.getStatus() == run.secondResult().getStatus());
        }
        if (expected.containsKey("determinismDataEqual")) {
            Object left = result.getSnapshot().getData();
            Object right = run.secondResult() == null ? null : run.secondResult().getSnapshot().getData();
            out.put("determinismDataEqual", left != null && left.equals(right));
        }
        if (expected.containsKey("determinismTraceTypesEqual")) {
            List<String> leftTypes = run.traceEvents().stream().map(HostRuntimeTraceEvent::type).toList();
            List<String> rightTypes = run.secondTraceEvents() == null
                ? List.of()
                : run.secondTraceEvents().stream().map(HostRuntimeTraceEvent::type).toList();
            out.put("determinismTraceTypesEqual", leftTypes.equals(rightTypes));
        }
        return out;
    }

    private boolean hasSingleRunnerInvariant(List<HostRuntimeTraceEvent> events) {
        int active = 0;
        for (HostRuntimeTraceEvent event : events) {
            if ("runner:start".equals(event.type())) {
                active += 1;
                if (active > 1) {
                    return false;
                }
            }
            if ("runner:end".equals(event.type())) {
                active -= 1;
                if (active < 0) {
                    return false;
                }
            }
        }
        return active == 0;
    }

    private record GoldenCaseResult(
        ComputeResult result,
        List<HostRuntimeTraceEvent> traceEvents,
        ComputeResult secondResult,
        List<HostRuntimeTraceEvent> secondTraceEvents
    ) {
        private GoldenCaseResult(ComputeResult result, List<HostRuntimeTraceEvent> traceEvents) {
            this(result, traceEvents, null, null);
        }
    }
}
