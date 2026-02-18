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
import ai.manifesto.world.schema.ActorKind;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AppParitySurfaceTest {

    @Test
    void policyCanRejectAction() throws Exception {
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

        ActionHandle handle = app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals(ActionPhase.REJECTED, handle.getPhase());
        assertTrue(handle.getActionResult() instanceof RejectedActionResult);
        assertEquals("blocked", ((RejectedActionResult) handle.getActionResult()).reason());
    }

    @Test
    void pluginLifecycleAndWorldStoreHooksAreInvoked() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);
        HostRuntime host = new HostRuntime().register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        AtomicInteger initCount = new AtomicInteger();
        AtomicInteger beforeCount = new AtomicInteger();
        AtomicInteger afterCount = new AtomicInteger();
        AtomicInteger disposeCount = new AtomicInteger();

        AppPlugin plugin = new AppPlugin() {
            @Override
            public void onInit(App app) {
                initCount.incrementAndGet();
            }

            @Override
            public void beforeAct(Intent intent, Snapshot snapshot) {
                beforeCount.incrementAndGet();
            }

            @Override
            public void afterAct(Intent intent, ActionHandle handle, Snapshot snapshot) {
                afterCount.incrementAndGet();
            }

            @Override
            public void onDispose(App app) {
                disposeCount.incrementAndGet();
            }
        };

        InMemoryAppWorldStore worldStore = new InMemoryAppWorldStore();
        App app = new DefaultApp(
            schema,
            snapshot,
            host,
            null,
            null,
            null,
            null,
            new AllowAllPolicyService(),
            worldStore
        );
        app.addPlugin(plugin);
        app.ready();

        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        app.dispose();

        assertEquals(1, initCount.get());
        assertEquals(1, beforeCount.get());
        assertEquals(1, afterCount.get());
        assertEquals(1, disposeCount.get());
        assertTrue(worldStore.listBranchNames().isEmpty());
    }

    @Test
    void schemaCompatibilityUtilitiesAndRecoveryErrors() {
        SchemaCompatibilityResult ok = SchemaCompatibilityUtils.validate("h1", "h1");
        assertTrue(ok.compatible());

        SchemaCompatibilityResult fail = SchemaCompatibilityUtils.validate("h1", "h2");
        assertFalse(fail.compatible());
        assertEquals("schema_hash_mismatch", fail.reason());

        SchemaMismatchOnResumeException mismatch = new SchemaMismatchOnResumeException("h1", "h2");
        assertEquals("APP-SCHEMA-MISMATCH-ON-RESUME", mismatch.getCode());

        BranchHeadNotFoundException missing = new BranchHeadNotFoundException("feature/a");
        assertEquals("APP-BRANCH-HEAD-NOT-FOUND", missing.getCode());
        assertEquals("feature/a", missing.getBranchName());
    }

    @Test
    void memoryFacadeSupportsBackfillRecallAndMaintenance() {
        InMemoryMemoryFacade memory = new InMemoryMemoryFacade();
        memory.backfill(List.of(
            new StoredMemoryRecord("k/a", "v1", 1L),
            new StoredMemoryRecord("k/b", "v2", 2L),
            new StoredMemoryRecord("x/c", "v3", 3L)
        ), new BackfillConfig(false));

        RecallResult result = memory.recall(new RecallRequest("k/", 10));
        assertEquals(2, result.records().size());

        memory.maintain(new MemoryMaintenanceOptions(1));
        RecallResult afterPrune = memory.recall(new RecallRequest("", 10));
        assertEquals(1, afterPrune.records().size());
    }

    @Test
    void worldStorePersistsBranchStateForWorldApp() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);
        HostRuntime host = new HostRuntime().register("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))));

        DefaultApp app = (DefaultApp) AppFactory.createWorldApp(
            schema,
            snapshot,
            host,
            "human-1",
            ActorKind.HUMAN
        );
        app.ready();
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));

        AppWorldStore store = app.getWorldStore();
        assertNotNull(store);
        assertTrue(store.listBranchNames().contains("main"));
        assertNotNull(store.load("main"));
    }

    @Test
    void appRefAndConfigContractsAreAvailable() {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);
        HostRuntime host = new HostRuntime();

        App app = AppFactory.createApp(new AppConfig(
            schema,
            snapshot,
            host,
            null,
            null,
            null,
            null,
            new AllowAllPolicyService(),
            null
        ));
        AppRef appRef = AppRefImpl.create(app);

        assertEquals(AppStatus.CREATED, appRef.getStatus());
        assertEquals(schema, appRef.getSchema());
        assertNotNull(appRef.getSnapshot());
    }

    @Test
    void sdkStyleCreateAppSupportsInitialDataAndEffects() throws Exception {
        DomainSchema schema = buildSchema();

        App app = AppFactory.createApp(
            schema,
            Map.of("status", "boot"),
            Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
        );
        app.ready();

        assertEquals("boot", app.getSnapshot().getData().get("status"));
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals("ok", app.getSnapshot().getData().get("status"));
    }

    @Test
    void sdkStyleCreateTestAppOverloadIsUsable() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createTestApp(
            schema,
            Map.of("status", "seed"),
            Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
        );
        app.ready();
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals("ok", app.getSnapshot().getData().get("status"));
    }

    @Test
    void appConfigSdkModeWorksWithoutLegacySnapshotAndHostFields() throws Exception {
        DomainSchema schema = buildSchema();
        App app = AppFactory.createApp(
            AppConfig.sdk(
                schema,
                Map.of("status", "seed"),
                Map.of("host.notify", params -> EffectResult.of(List.of(Patch.set("status", "ok"))))
            )
        );
        app.ready();
        app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals("ok", app.getSnapshot().getData().get("status"));
    }

    @Test
    void defaultAppThrowsTypedLifecycleAndBranchExceptions() {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);
        HostRuntime host = new HostRuntime();
        DefaultApp app = new DefaultApp(schema, snapshot, host);

        assertThrows(AppNotReadyException.class, () -> app.act(new Intent("notify", Map.of(), "intent-1")));

        app.dispose();
        assertThrows(AppDisposedException.class, app::ready);
        assertThrows(WorldIntegrationDisabledException.class, () -> app.switchBranch(ai.manifesto.world.schema.WorldId.of("w1")));
    }

    private DomainSchema buildSchema() {
        FlowNode flow = FlowNode.Seq.of(
            FlowNode.Effect.of("host.notify", Map.of()),
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder("urn:test:app:parity", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder("urn:test:app:parity", "1.0.0")
            .hash(hash)
            .addAction(action)
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
