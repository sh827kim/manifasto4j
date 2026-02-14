package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;

/**
 * KR: stateful action 네이밍 규칙을 검사합니다.
 * EN: Checks naming convention for stateful actions.
 */
public final class StatefulnessChecker {
    public boolean isStateful(IntentGraph graph) {
        if (graph == null || graph.nodes() == null) {
            return false;
        }
        for (IntentGraphNode node : graph.nodes()) {
            if (node != null && node.action() != null && node.action().startsWith("state.")) {
                return true;
            }
        }
        return false;
    }
}
