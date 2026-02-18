package ai.manifesto.runtime;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Patch;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.EffectResult;
import ai.manifesto.host.HostRuntime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppComplianceExpansionTest {

    @Test
    void rejectedActionDoesNotPublishAdditionalSubscriptionUpdate() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);
        HostRuntime host = new HostRuntime();

        DefaultApp app = new DefaultApp(
            schema,
            snapshot,
            host,
            null,
            null,
            null,
            null,
            (intent, current) -> AppPolicyService.PolicyDecision.reject("blocked"),
            null
        );
        app.ready();

        AtomicInteger updates = new AtomicInteger();
        app.subscribe(s -> s.getData().get("status"), value -> updates.incrementAndGet());
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));

        assertEquals(1, updates.get(), "rejected action must not publish snapshot mutation");
        assertEquals("", app.getSnapshot().getData().get("status"));
    }

    @Test
    void systemFacadeUsesSystemRuntimeKindBoundary() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createApp(
            schema,
            Map.of("status", "seed"),
            Map.of(
                "host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))),
                "host.system.notify", params -> EffectResult.of(List.of(Patch.set("status", "system-ok")))
            )
        );
        app.ready();

        ActionHandle handle = app.getSystemFacade().act("notify", Map.of());

        assertEquals(RuntimeKind.SYSTEM, handle.getRuntimeKind());
        assertEquals("system-ok", app.getSnapshot().getData().get("status"));
    }

    @Test
    void actionUpdateTimestampsAreMonotonicAndPhasesAreOrdered() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createApp(
            schema,
            Map.of(),
            Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
        );
        app.ready();

        ActionHandle handle = app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        List<ActionUpdate> updates = new ArrayList<>(handle.getUpdates());

        assertTrue(updates.size() >= 2);
        assertEquals(ActionPhase.PREPARING, updates.get(0).phase());
        long prev = Long.MIN_VALUE;
        for (ActionUpdate update : updates) {
            assertTrue(update.timestampMillis() >= prev, "timestamps must be monotonic");
            prev = update.timestampMillis();
        }
    }

    private DomainSchema buildSchema() {
        FlowNode notifyFlow = FlowNode.Seq.of(
            FlowNode.Effect.of("host.notify", Map.of()),
            FlowNode.Halt.of("done")
        );
        FlowNode systemNotifyFlow = FlowNode.Seq.of(
            FlowNode.Effect.of("host.system.notify", Map.of()),
            FlowNode.Halt.of("done")
        );
        ActionSpec notify = new ActionSpec.Builder("notify").flow(notifyFlow).build();
        ActionSpec systemNotify = new ActionSpec.Builder("system.notify").flow(systemNotifyFlow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder("urn:test:compliance:expansion", "1.0.0")
            .addAction(notify)
            .addAction(systemNotify)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder("urn:test:compliance:expansion", "1.0.0")
            .hash(hash)
            .addAction(notify)
            .addAction(systemNotify)
            .addDataField(new FieldSpec("status", "string", false, ""))
            .build();
    }

    private Snapshot buildSnapshot(DomainSchema schema) {
        return Snapshot.builder()
            .data(new HashMap<>(Map.of("status", "")))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();
    }
}
