package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * KR: ABSTRACT 노드로 향하는 의존성이 존재하는지 검사합니다.
 * EN: Checks dependencies targeting ABSTRACT nodes.
 */
public final class AbstractDependencyChecker {
    public boolean hasAbstractDependency(IntentGraph graph) {
        if (graph == null || graph.nodes() == null || graph.edges() == null) {
            return false;
        }
        Map<String, ResolutionStatus> statusByNode = new HashMap<>();
        for (IntentGraphNode node : graph.nodes()) {
            if (node != null) {
                statusByNode.put(node.nodeId(), node.resolutionStatus());
            }
        }
        for (DependencyEdge edge : graph.edges()) {
            if (edge == null) {
                continue;
            }
            if (statusByNode.get(edge.toNodeId()) == ResolutionStatus.ABSTRACT) {
                return true;
            }
        }
        return false;
    }
}
