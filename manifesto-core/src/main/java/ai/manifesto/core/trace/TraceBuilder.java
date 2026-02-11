package ai.manifesto.core.trace;

import ai.manifesto.core.Intent;
import ai.manifesto.core.TraceGraph;
import ai.manifesto.core.TraceNode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KR: TraceBuilder는 Core trace 계층에서 trace builder 역할을 수행하는 구현 타입입니다.
 * EN: TraceBuilder is an implementation type performing trace builder roles in the Core trace layer.
 */
public final class TraceBuilder {

    private TraceBuilder() {
        // Utility class
    }

    /**
     * TraceNode 트리를 평탄화하여 id -> TraceNode 맵을 생성한다.
     */
    public static Map<String, TraceNode> collectNodes(TraceNode root) {
        Map<String, TraceNode> nodes = new LinkedHashMap<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private static void collectNodes(TraceNode node, Map<String, TraceNode> nodes) {
        if (node == null) {
            return;
        }
        nodes.put(node.getId(), node);
        for (TraceNode child : node.getChildren()) {
            collectNodes(child, nodes);
        }
    }

    /**
     * TraceGraph 생성
     */
    public static TraceGraph buildGraph(
        TraceNode root,
        Intent intent,
        long baseVersion,
        long resultVersion,
        long duration,
        TraceGraph.TraceTermination terminatedBy
    ) {
        return TraceGraph.builder()
            .root(root)
            .nodes(collectNodes(root))
            .intent(intent)
            .baseVersion(baseVersion)
            .resultVersion(resultVersion)
            .duration(duration)
            .terminatedBy(terminatedBy)
            .build();
    }
}
