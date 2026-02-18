package ai.manifesto.runtime;

import ai.manifesto.core.*;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.host.HostRuntime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("App 최소 동작 테스트")
class AppTest {

    @Test
    @DisplayName("act 실행 후 subscribe 알림")
    void testActAndSubscribe() throws Exception {
        FlowNode effectFlow = FlowNode.Seq.of(
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Halt.of("done")
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

        HostRuntime host = new HostRuntime();

        App app = AppFactory.createApp(schema, snapshot, host);
        app.ready();

        AtomicReference<Object> latest = new AtomicReference<>();
        app.subscribe(s -> s.getData().get("status"), latest::set);

        Intent intent = new Intent("notify", new HashMap<>(), UUID.randomUUID().toString());
        ActionHandle handle = app.act(intent);

        assertTrue(
            handle.getStatus() == ComputeStatus.HALTED || handle.getStatus() == ComputeStatus.COMPLETE
        );
        assertEquals(ActionPhase.COMPLETED, handle.getPhase());
        assertTrue(handle.getUpdates().size() >= 3);
        assertEquals(ActionPhase.PREPARING, handle.getUpdates().get(0).phase());
        assertEquals(ActionPhase.EXECUTING, handle.getUpdates().get(1).phase());
        assertEquals("ok", latest.get());
    }

    @Test
    @DisplayName("session/ hook API 동작")
    void testSessionAndHookApis() throws Exception {
        FlowNode effectFlow = FlowNode.Seq.of(
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("notify")
            .flow(effectFlow)
            .build();
        DomainSchema schema = buildSchemaWithHash(
            "urn:test",
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

        HostRuntime host = new HostRuntime();
        InMemoryAppSnapshotStore store = new InMemoryAppSnapshotStore();
        App app = AppFactory.createApp(schema, snapshot, host, "session-a", store);

        AtomicInteger readyCount = new AtomicInteger(0);
        AtomicInteger beforeCount = new AtomicInteger(0);
        AtomicInteger afterCount = new AtomicInteger(0);
        List<ActionPhase> phases = new java.util.ArrayList<>();
        AppHook hook = new AppHook() {
            @Override
            public void onReady(Snapshot snapshot) {
                readyCount.incrementAndGet();
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot snapshot) {
                beforeCount.incrementAndGet();
            }

            @Override
            public void onActionUpdate(Intent intent, ActionUpdate update, Snapshot snapshot) {
                phases.add(update.phase());
            }

            @Override
            public void onAfterAct(Intent intent, ActionHandle handle, Snapshot snapshot) {
                afterCount.incrementAndGet();
            }
        };
        app.addHook(hook);

        app.ready();
        ActionHandle handle = app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));

        assertTrue(app.hasSessionPersistence());
        assertEquals("session-a", app.getSessionId());
        assertEquals(1, readyCount.get());
        assertEquals(1, beforeCount.get());
        assertEquals(1, afterCount.get());
        assertEquals(ActionPhase.COMPLETED, handle.getPhase());
        assertTrue(phases.contains(ActionPhase.PREPARING));
        assertTrue(phases.contains(ActionPhase.EXECUTING));
        assertTrue(phases.contains(ActionPhase.COMPLETED));
    }

    @Test
    @DisplayName("ActionHandle await/timeout/cancel 및 ActionResult")
    void testActionHandleAsyncApis() throws Exception {
        App app = createNotifyApp();
        app.ready();

        ActionHandle handle = app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        ComputeResult awaited = handle.await(Duration.ofMillis(50));

        assertNotNull(awaited);
        assertNotNull(handle.getActionResult());
        assertEquals("completed", handle.getActionResult().status());
        assertFalse(handle.cancel("already_done"));
        assertFalse(handle.isCancelled());
    }

    @Test
    @DisplayName("System/Memory facade 동작")
    void testSystemAndMemoryFacade() throws Exception {
        App app = createNotifyApp();
        app.ready();

        MemoryFacade memory = app.getMemoryFacade();
        assertTrue(memory.isEnabled());
        memory.ingest("memo-key", "memo-value");
        assertEquals("memo-value", memory.recall("memo-key").orElse(null));

        ActionHandle systemHandle = app.getSystemFacade().act("noop", Map.of("k", "v"));
        assertNotNull(systemHandle.getActionResult());
        assertEquals(RuntimeKind.SYSTEM, systemHandle.getRuntimeKind());
    }

    @Test
    @DisplayName("Hook 우선순위/예외 격리")
    void testHookPriorityAndErrorIsolation() throws Exception {
        App app = createNotifyApp();
        List<String> order = new java.util.ArrayList<>();

        app.addHook(new AppHook() {
            @Override
            public int priority() {
                return 100;
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot snapshot) {
                order.add("high");
            }
        });

        app.addHook(new AppHook() {
            @Override
            public int priority() {
                return 50;
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot snapshot) {
                throw new IllegalStateException("hook error");
            }
        });

        app.addHook(new AppHook() {
            @Override
            public int priority() {
                return 10;
            }

            @Override
            public void onBeforeAct(Intent intent, Snapshot snapshot) {
                order.add("low");
            }
        });

        app.ready();
        ActionHandle handle = app.act(new Intent("notify", Map.of(), UUID.randomUUID().toString()));
        assertEquals(ActionPhase.COMPLETED, handle.getPhase());
        assertEquals(List.of("high", "low"), order);
    }

    @Test
    @DisplayName("AppSession actor/context 바인딩")
    void testAppSessionBinding() throws Exception {
        App app = createNotifyApp();
        app.ready();

        AppSession session = app.createSession("actor-a", Map.of("tenantId", "t-1"));
        ActionHandle handle = session.act("notify", Map.of());

        assertEquals(ActionPhase.COMPLETED, handle.getPhase());
        assertEquals("actor-a", session.actorId());
        assertEquals("t-1", session.context().get("tenantId"));
    }

    private App createNotifyApp() {
        FlowNode effectFlow = FlowNode.Seq.of(
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done"),
            FlowNode.Halt.of("done")
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
        return AppFactory.createApp(schema, snapshot, new HostRuntime(), "session-test", new InMemoryAppSnapshotStore());
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
