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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
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
