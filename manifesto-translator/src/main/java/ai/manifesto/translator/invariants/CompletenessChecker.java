package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;

/**
 * KR: 모든 노드가 RESOLVED 상태인지 검사합니다.
 * EN: Checks whether all nodes are in RESOLVED status.
 */
public final class CompletenessChecker {
    public boolean isComplete(IntentGraph graph) {
        if (graph == null || graph.nodes() == null || graph.nodes().isEmpty()) {
            return false;
        }
        for (IntentGraphNode node : graph.nodes()) {
            if (node == null || node.resolutionStatus() != ResolutionStatus.RESOLVED) {
                return false;
            }
        }
        return true;
    }
}
