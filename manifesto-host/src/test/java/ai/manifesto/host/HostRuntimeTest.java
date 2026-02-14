package ai.manifesto.host;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.comparison.Eq;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.host.runtime.HostRuntimeTraceEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HostRuntime compute-effect loop 테스트")
class HostRuntimeTest {

    @Test
    @DisplayName("Effect 처리 후 Patch 적용")
    void testEffectLoopAppliesPatch() throws Exception {
        FlowNode effectFlow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );

        ActionSpec effectAction = new ActionSpec.Builder("notify")
            .flow(effectFlow)
            .build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(
                List.of(Patch.set("status", "ok"))
            ));

        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());
        ComputeResult result = host.run(schema, snapshot, intent, 5);

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertEquals("ok", result.getSnapshot().getData().get("status"));
        @SuppressWarnings("unchecked")
        Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
        assertNotNull(hostState);
        assertEquals(intent.getIntentId(), hostState.get("currentIntentId"));
    }

    @Test
    @DisplayName("Effect 루프가 수렴하지 않으면 가드에 의해 종료")
    void testEffectLoopGuardStopsNonConvergingExecution() throws Exception {
        FlowNode effectOnlyFlow = FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")));
        ActionSpec effectAction = new ActionSpec.Builder("notify").flow(effectOnlyFlow).build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:guard",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of()));

        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());
        ComputeResult result = host.run(
            schema,
            snapshot,
            intent,
            HostRuntimeOptions.builder()
                .timeoutSeconds(1)
                .maxIterations(3)
                .build()
        );

        assertEquals(ComputeStatus.ERROR, result.getStatus());
    }

    @Test
    @DisplayName("등록되지 않은 Effect handler가 있으면 pending 상태를 유지한다")
    void testReturnsPendingWhenHandlerMissing() throws Exception {
        FlowNode effectFlow = FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")));
        ActionSpec effectAction = new ActionSpec.Builder("notify").flow(effectFlow).build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:missing-handler",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime();
        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());

        ComputeResult result = host.run(schema, snapshot, intent, 5);

        assertEquals(ComputeStatus.PENDING, result.getStatus());
        assertFalse(result.getRequirements().isEmpty());
    }

    @Test
    @DisplayName("Effect 실패 시 $host.lastError/errors를 기록하고 ERROR로 종료")
    void testRecordsHostErrorWhenEffectFails() throws Exception {
        FlowNode effectFlow = FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")));
        ActionSpec effectAction = new ActionSpec.Builder("notify").flow(effectFlow).build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:effect-error",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> {
                throw new RuntimeException("boom");
            });

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", new HashMap<>(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().maxEffectRetries(1).build()
        );

        assertEquals(ComputeStatus.ERROR, result.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> hostState = (Map<String, Object>) result.getSnapshot().getData().get("$host");
        assertNotNull(hostState);
        assertNotNull(hostState.get("lastError"));
        assertTrue(hostState.get("errors") instanceof List<?>);
    }

    @Test
    @DisplayName("Effect 재시도 정책으로 일시 실패 후 성공할 수 있다")
    void testEffectRetryCanRecoverFromTransientFailure() throws Exception {
        FlowNode effectFlow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );
        ActionSpec effectAction = new ActionSpec.Builder("notify").flow(effectFlow).build();

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:effect-retry",
            "1.0.0",
            new ActionSpec[] { effectAction },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );

        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        AtomicInteger attempts = new AtomicInteger(0);
        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> {
                if (attempts.getAndIncrement() == 0) {
                    throw new RuntimeException("transient");
                }
                return EffectResult.of(List.of(Patch.set("status", "ok")));
            });

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", new HashMap<>(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().maxEffectRetries(1).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertEquals("ok", result.getSnapshot().getData().get("status"));
    }

    @Test
    @DisplayName("trace sink를 설정하면 runner/job 이벤트가 수집된다")
    void testTraceSinkCollectsRunnerAndJobEvents() throws Exception {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.notify", Map.of("message", new Lit("hi")))
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();
        DomainSchema schema = buildSchemaWithHash(
            "urn:test:trace-sink",
            "1.0.0",
            new ActionSpec[] { action },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime()
            .register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));
        List<HostRuntimeTraceEvent> events = new java.util.ArrayList<>();
        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("notify", new HashMap<>(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().traceSink(events::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertFalse(events.isEmpty());
        assertTrue(events.stream().anyMatch(e -> "runner:start".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "job:start".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "job:end".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "continue:enqueue".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "runner:end".equals(e.type())));
    }

    @Test
    @DisplayName("HCTS 다단계 reinjection 시 trace/liveness invariant를 만족한다")
    void testTraceInvariantsForChainedReinjection() throws Exception {
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

        DomainSchema schema = buildSchemaWithHash(
            "urn:test:trace-chain",
            "1.0.0",
            new ActionSpec[] { action },
            new FieldSpec[] {
                new FieldSpec("step1", "boolean", false, null),
                new FieldSpec("step2", "boolean", false, null),
                new FieldSpec("step3", "boolean", false, null)
            }
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
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        HostRuntime host = new HostRuntime()
            .register("host.step1", params -> EffectResult.of(List.of(Patch.set("step1", true))))
            .register("host.step2", params -> EffectResult.of(List.of(Patch.set("step2", true))))
            .register("host.step3", params -> EffectResult.of(List.of(Patch.set("step3", true))));

        List<HostRuntimeTraceEvent> events = new java.util.ArrayList<>();
        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("chain", new HashMap<>(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().traceSink(events::add).build()
        );

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertEquals(true, result.getSnapshot().getData().get("step1"));
        assertEquals(true, result.getSnapshot().getData().get("step2"));
        assertEquals(true, result.getSnapshot().getData().get("step3"));

        long continueCount = events.stream().filter(e -> "continue:enqueue".equals(e.type())).count();
        long recheckCount = events.stream().filter(e -> "runner:recheck".equals(e.type())).count();
        long jobStartCount = events.stream().filter(e -> "job:start".equals(e.type())).count();
        long jobEndCount = events.stream().filter(e -> "job:end".equals(e.type())).count();
        long runnerStartCount = events.stream().filter(e -> "runner:start".equals(e.type())).count();
        long runnerEndCount = events.stream().filter(e -> "runner:end".equals(e.type())).count();

        assertTrue(continueCount >= 3, "continue:enqueue should happen for each effect cycle");
        assertTrue(recheckCount >= 1, "runner:recheck should be observed");
        assertEquals(jobStartCount, jobEndCount, "job start/end counts must match");
        assertEquals(runnerStartCount, runnerEndCount, "runner start/end counts must match");
        assertTrue(hasSingleRunnerInvariant(events), "runner:start must not overlap without runner:end");
    }

    @Test
    @DisplayName("context-aware effect handler는 execution context를 전달받는다")
    void testContextAwareEffectHandlerReceivesExecutionContext() throws Exception {
        FlowNode flow = FlowNode.If.of(
            new Eq(new Get("status"), new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Effect.of("host.context", Map.of("message", new Lit("ctx")))
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();
        DomainSchema schema = buildSchemaWithHash(
            "urn:test:context-aware",
            "1.0.0",
            new ActionSpec[] { action },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        List<EffectExecutionContext> capturedContexts = new java.util.ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.context", (ContextAwareEffectHandler) (params, context) -> {
                capturedContexts.add(context);
                return EffectResult.of(List.of(Patch.set("status", "ok")));
            });

        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());
        ComputeResult result = host.run(schema, snapshot, intent, HostRuntimeOptions.builder().build());

        assertEquals(ComputeStatus.HALTED, result.getStatus());
        assertEquals(1, capturedContexts.size());
        EffectExecutionContext context = capturedContexts.get(0);
        assertEquals(intent.getIntentId(), context.intentId());
        assertEquals("host.context", context.requirementType());
        assertTrue(context.attempt() >= 1);
    }

    @Test
    @DisplayName("effect trace에는 attempt/retry/failure 이벤트가 남는다")
    void testEffectTraceEventsForRetriesAndFailure() throws Exception {
        FlowNode effectFlow = FlowNode.Effect.of("host.retry", Map.of());
        ActionSpec action = new ActionSpec.Builder("retry").flow(effectFlow).build();
        DomainSchema schema = buildSchemaWithHash(
            "urn:test:effect-trace",
            "1.0.0",
            new ActionSpec[] { action },
            new FieldSpec[] { new FieldSpec("status", "string", false, "") }
        );
        Snapshot snapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();

        List<HostRuntimeTraceEvent> events = new java.util.ArrayList<>();
        HostRuntime host = new HostRuntime()
            .register("host.retry", params -> {
                throw new RuntimeException("always fail");
            });

        ComputeResult result = host.run(
            schema,
            snapshot,
            new Intent("retry", new HashMap<>(), UUID.randomUUID().toString()),
            HostRuntimeOptions.builder().maxEffectRetries(1).traceSink(events::add).build()
        );

        assertEquals(ComputeStatus.ERROR, result.getStatus());
        assertTrue(events.stream().anyMatch(e -> "effect:attempt".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "effect:retry".equals(e.type())));
        assertTrue(events.stream().anyMatch(e -> "effect:failure".equals(e.type())));
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

    private DomainSchema buildSchemaWithHash(
        String id,
        String version,
        ActionSpec[] actions,
        FieldSpec[] dataFields
    ) {
        DomainSchema.Builder tempBuilder = new DomainSchema.Builder(id, version);
        for (ActionSpec action : actions) {
            tempBuilder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            tempBuilder.addDataField(field);
        }
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version)
            .hash(hash);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        return builder.build();
    }
}
