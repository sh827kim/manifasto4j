package ai.manifesto.app;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.HostRuntime;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppRegressionExpansionTest {

    @Test
    void sessionSnapshotIsRecoveredOnReady() throws Exception {
        DomainSchema schema = buildSchema();
        InMemoryAppSnapshotStore store = new InMemoryAppSnapshotStore();

        Snapshot seed = buildSnapshot(schema, "");
        App first = AppFactory.createApp(schema, seed, new HostRuntime(), "session-r1", store);
        first.ready();
        first.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals("ok", first.getSnapshot().getData().get("status"));

        Snapshot coldStart = buildSnapshot(schema, "cold");
        App second = AppFactory.createApp(schema, coldStart, new HostRuntime(), "session-r1", store);
        second.ready();
        assertEquals("ok", second.getSnapshot().getData().get("status"));
    }

    @Test
    void hookFailFastPropagatesHookError() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema, "");
        DefaultApp app = new DefaultApp(schema, snapshot, new HostRuntime());
        app.addHook(new AppHook() {
            @Override
            public AppHookErrorMode errorMode() {
                return AppHookErrorMode.FAIL_FAST;
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot current) {
                throw new IllegalStateException("fail-fast-hook");
            }
        });
        app.ready();

        assertThrows(IllegalStateException.class, () -> app.act(new Intent("notify", Map.of(), "intent-fail-fast")));
    }

    @Test
    void hookSupportsFilterExcludesUnmatchedEvents() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema, "");
        DefaultApp app = new DefaultApp(schema, snapshot, new HostRuntime());
        AtomicInteger beforeActCount = new AtomicInteger();
        AtomicInteger updateCount = new AtomicInteger();

        app.addHook(new AppHook() {
            @Override
            public boolean supports(AppHookEventType eventType) {
                return eventType == AppHookEventType.BEFORE_ACT;
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot current) {
                beforeActCount.incrementAndGet();
            }

            @Override
            public void onActionUpdate(Intent intent, ActionUpdate update, Snapshot current) {
                updateCount.incrementAndGet();
            }
        });

        app.ready();
        app.act(new Intent("notify", Map.of(), "intent-filter"));

        assertEquals(1, beforeActCount.get());
        assertEquals(0, updateCount.get());
    }

    @Test
    void switchingUnknownBranchAliasThrowsTypedError() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema, "");
        DefaultApp app = new DefaultApp(schema, snapshot, new HostRuntime());
        app.ready();

        assertThrows(BranchNotFoundException.class, () -> app.switchBranch("missing-branch"));
    }

    private DomainSchema buildSchema() {
        ActionSpec action = new ActionSpec.Builder("notify")
            .flow(FlowNode.Seq.of(
                FlowNode.Patch.set("status", new Lit("ok")),
                FlowNode.Halt.of("done")
            ))
            .build();

        DomainSchema.Builder temp = new DomainSchema.Builder("urn:test:app:regression", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        String hash = ValidationUtils.computeSchemaHash(temp.hash("").build());
        return new DomainSchema.Builder("urn:test:app:regression", "1.0.0")
            .hash(hash)
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""))
            .build();
    }

    private Snapshot buildSnapshot(DomainSchema schema, String status) {
        return Snapshot.builder()
            .data(new HashMap<>(Map.of("status", status)))
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", schema.getHash()))
            .build();
    }
}
