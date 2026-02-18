package ai.manifesto.runtime;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppMemoryContractTest {

    @Test
    void memoryProviderAndVerifierCanFreezeContextAndExposeFailureMarker() {
        Map<String, StoredMemoryRecord> storage = new ConcurrentHashMap<>();
        MemoryProvider provider = new InMemoryProvider(storage);
        MemoryVerifier verifier = (key, value) -> key.startsWith("blocked/")
            ? MemoryVerificationResult.freeze("blocked_key")
            : MemoryVerificationResult.accept();

        InMemoryMemoryFacade memory = new InMemoryMemoryFacade(provider, verifier, false);
        memory.ingest("allowed/a", "v1");
        memory.ingest("blocked/a", "v2");

        RecallResult result = memory.recall(new RecallRequest("allowed/", 10));
        assertEquals(1, result.records().size());
        assertTrue(result.contextFrozen());
        assertEquals("blocked_key", result.failureMarker());

        memory.unfreezeContext();
        assertTrue(storage.containsKey("allowed/a"));

        RecallResult frozenByRequest = memory.recall(new RecallRequest("allowed/", 10, true, "ctx-1"));
        assertTrue(frozenByRequest.contextFrozen());
        assertEquals("ctx-1", frozenByRequest.contextToken());
    }

    @Test
    void appConfigCanInjectPluggableMemoryProviderAndVerifier() throws Exception {
        DomainSchema schema = buildSchema();
        Snapshot snapshot = buildSnapshot(schema);

        Map<String, StoredMemoryRecord> storage = new ConcurrentHashMap<>();
        MemoryProvider provider = new InMemoryProvider(storage);
        MemoryVerifier verifier = (key, value) -> key.startsWith("deny/")
            ? MemoryVerificationResult.freeze("deny_key")
            : MemoryVerificationResult.accept();

        App app = AppFactory.createApp(new AppConfig(
            schema,
            snapshot,
            new HostRuntime(),
            Map.of(),
            Map.of(),
            null,
            null,
            null,
            null,
            null,
            null,
            provider,
            verifier,
            false
        ));
        app.ready();

        MemoryFacade memory = app.getMemoryFacade();
        memory.ingest("allow/a", "v1");
        memory.ingest("deny/a", "v2");

        assertEquals(Optional.of("v1"), memory.recall("allow/a"));
        assertTrue(memory.isContextFrozen());
        assertEquals("deny_key", memory.getLastFailureMarker());
        assertNotNull(storage.get("allow/a"));
    }

    private DomainSchema buildSchema() {
        FlowNode flow = FlowNode.Seq.of(
            FlowNode.Patch.set("status", new Lit("ok")),
            FlowNode.Halt.of("done")
        );
        ActionSpec action = new ActionSpec.Builder("notify").flow(flow).build();

        DomainSchema.Builder tempBuilder = new DomainSchema.Builder("urn:test:memory:contract", "1.0.0")
            .addAction(action)
            .addDataField(new FieldSpec("status", "string", false, ""));
        DomainSchema tempSchema = tempBuilder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(tempSchema);

        return new DomainSchema.Builder("urn:test:memory:contract", "1.0.0")
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

    private static final class InMemoryProvider implements MemoryProvider {
        private final Map<String, StoredMemoryRecord> storage;

        private InMemoryProvider(Map<String, StoredMemoryRecord> storage) {
            this.storage = storage;
        }

        @Override
        public void save(StoredMemoryRecord record) {
            storage.put(record.key(), record);
        }

        @Override
        public Optional<StoredMemoryRecord> load(String key) {
            return Optional.ofNullable(storage.get(key));
        }

        @Override
        public List<StoredMemoryRecord> list() {
            return List.copyOf(storage.values());
        }

        @Override
        public void remove(String key) {
            storage.remove(key);
        }
    }
}
