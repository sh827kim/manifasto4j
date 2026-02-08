package ai.manifesto.world;

import ai.manifesto.core.Intent;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.SystemState;
import ai.manifesto.world.schema.ActorKind;
import ai.manifesto.world.schema.ActorRef;
import ai.manifesto.world.schema.AutoApprovePolicy;
import ai.manifesto.world.schema.FinalDecisionKind;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentInstance;
import ai.manifesto.world.schema.IntentMeta;
import ai.manifesto.world.schema.IntentOrigin;
import ai.manifesto.world.schema.IntentSource;
import ai.manifesto.world.schema.PolicyRuleDecision;
import ai.manifesto.world.schema.PolicyRulesPolicy;
import ai.manifesto.world.schema.ProposalStatus;
import ai.manifesto.world.schema.World;
import ai.manifesto.world.types.HostExecutionOptions;
import ai.manifesto.world.types.HostExecutionResult;
import ai.manifesto.world.types.HostExecutor;
import ai.manifesto.world.types.IntentKeys;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("World Golden Tests")
class WorldGoldenTest {
    private static final String SCHEMA_HASH = "schema-hash";
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("World 승인/거절 terminal 경로가 골든 기대값과 일치")
    void worldGoldenCases() throws Exception {
        List<Map<String, Object>> vectors = loadVectors("golden/world-e2e.json");
        assertFalse(vectors.isEmpty(), "Golden vectors should not be empty");

        for (Map<String, Object> vector : vectors) {
            String name = String.valueOf(vector.get("name"));
            @SuppressWarnings("unchecked")
            Map<String, Object> expected = (Map<String, Object>) vector.get("expected");
            assertNotNull(expected, "Expected golden data missing for: " + name);

            CaseOutcome outcome = switch (name) {
                case "approve-terminal-creates-world" -> runApproveCase();
                case "reject-terminal-without-result-world" -> runRejectCase();
                default -> throw new IllegalArgumentException("Unknown world golden case: " + name);
            };

            Map<String, Object> actual = normalize(outcome, expected);
            assertJsonEquals(expected, actual, "Golden mismatch: " + name);
        }
    }

    private CaseOutcome runApproveCase() {
        HostExecutor executor = new HostExecutor() {
            @Override
            public HostExecutionResult execute(
                String executionKey,
                Snapshot baseSnapshot,
                Intent intent,
                HostExecutionOptions options
            ) {
                return HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 1)));
            }
        };
        ManifestoWorld world = new ManifestoWorld(SCHEMA_HASH, executor, null);
        World genesis = world.createGenesis(createGenesisSnapshot());

        ActorRef actor = new ActorRef("human-approve", ActorKind.HUMAN);
        world.registerActor(actor, new AutoApprovePolicy());
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-world-golden-1", "event-world-golden-1");
        ProposalResult result = world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null);
        return new CaseOutcome(result, world.getStore().listWorlds().size());
    }

    private CaseOutcome runRejectCase() {
        HostExecutor executor = (executionKey, baseSnapshot, intent, options) ->
            HostExecutionResult.completed(baseSnapshot.withData(Map.of("count", 999)));
        ManifestoWorld world = new ManifestoWorld(SCHEMA_HASH, executor, null);
        World genesis = world.createGenesis(createGenesisSnapshot());

        ActorRef actor = new ActorRef("agent-reject", ActorKind.AGENT);
        world.registerActor(actor, new PolicyRulesPolicy(List.of(), PolicyRuleDecision.REJECT, null));
        IntentInstance intent = createIntentInstance(actor, "increment", "intent-world-golden-2", "event-world-golden-2");
        ProposalResult result = world.submitProposal(actor.getActorId(), intent, genesis.getWorldId(), null);
        return new CaseOutcome(result, world.getStore().listWorlds().size());
    }

    private Snapshot createGenesisSnapshot() {
        return Snapshot.builder()
            .data(Map.of("count", 0))
            .computed(Map.of())
            .system(SystemState.initial())
            .input(Map.of())
            .meta(Snapshot.SnapshotMeta.create(1, 1000L, "seed", SCHEMA_HASH))
            .build();
    }

    private IntentInstance createIntentInstance(ActorRef actor, String type, String intentId, String eventId) {
        IntentBody body = new IntentBody(type, Map.of(), null);
        String intentKey = IntentKeys.computeIntentKey(SCHEMA_HASH, body);
        return new IntentInstance(
            body,
            intentId,
            intentKey,
            new IntentMeta(new IntentOrigin("projection", new IntentSource("ui", eventId), actor))
        );
    }

    private Map<String, Object> normalize(CaseOutcome outcome, Map<String, Object> expected) {
        ProposalResult result = outcome.result();
        Map<String, Object> out = new LinkedHashMap<>();
        if (expected.containsKey("proposalStatus")) {
            ProposalStatus status = result.getProposal().getStatus();
            out.put("proposalStatus", status.name());
        }
        if (expected.containsKey("decisionKind")) {
            FinalDecisionKind kind = result.getDecision().getDecision().getKind();
            out.put("decisionKind", kind.name());
        }
        if (expected.containsKey("hasResultWorld")) {
            out.put("hasResultWorld", result.getResultWorld() != null);
        }
        if (expected.containsKey("worldCount")) {
            out.put("worldCount", outcome.worldCount());
        }
        return out;
    }

    private record CaseOutcome(ProposalResult result, int worldCount) {}

    private List<Map<String, Object>> loadVectors(String resourcePath) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalArgumentException("Missing resource: " + resourcePath);
            }
            return mapper.readValue(input, new TypeReference<>() {});
        }
    }

    private void assertJsonEquals(Object expected, Object actual, String message) throws Exception {
        JsonNode expectedNode = mapper.valueToTree(expected);
        JsonNode actualNode = mapper.valueToTree(actual);
        if (!expectedNode.equals(actualNode)) {
            throw new AssertionError(message + "\nExpected: " + expectedNode + "\nActual: " + actualNode);
        }
    }
}
