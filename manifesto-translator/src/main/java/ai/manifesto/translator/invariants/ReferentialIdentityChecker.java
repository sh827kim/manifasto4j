package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;

import java.util.HashSet;
import java.util.Set;

/**
 * KR: nodeId 중복 여부를 검사합니다.
 * EN: Checks duplicate node identifiers.
 */
public final class ReferentialIdentityChecker {
    public boolean isValid(IntentGraph graph) {
        if (graph == null || graph.nodes() == null) {
            return false;
        }
        Set<String> ids = new HashSet<>();
        for (IntentGraphNode node : graph.nodes()) {
            if (node == null || node.nodeId() == null || node.nodeId().isBlank()) {
                return false;
            }
            if (!ids.add(node.nodeId())) {
                return false;
            }
        }
        return true;
    }
}
