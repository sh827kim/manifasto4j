package ai.manifesto.core.trace;

import ai.manifesto.core.TraceGraph;
import ai.manifesto.core.TraceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: TraceReplay는 Core trace 계층에서 trace replay 역할을 수행하는 구현 타입입니다.
 * EN: TraceReplay is an implementation type performing trace replay roles in the Core trace layer.
 */
public final class TraceReplay {

    private TraceReplay() {
        // Utility class
    }

    /**
     * TraceGraph 구조 일관성 검증
     */
    public static List<String> validateGraph(TraceGraph graph) {
        Objects.requireNonNull(graph, "graph is required");

        List<String> errors = new ArrayList<>();
        TraceNode root = graph.getRoot();
        Map<String, TraceNode> nodes = graph.getNodes();

        if (root == null) {
            errors.add("TRACE_ERROR: root node is required");
            return errors;
        }

        if (nodes == null || nodes.isEmpty()) {
            errors.add("TRACE_ERROR: nodes map is required");
            return errors;
        }

        if (!nodes.containsKey(root.getId())) {
            errors.add("TRACE_ERROR: root id not found in nodes map: " + root.getId());
        }

        for (Map.Entry<String, TraceNode> entry : nodes.entrySet()) {
            TraceNode node = entry.getValue();
            if (node == null) {
                errors.add("TRACE_ERROR: node is null for id: " + entry.getKey());
                continue;
            }
            if (!entry.getKey().equals(node.getId())) {
                errors.add("TRACE_ERROR: node id mismatch: key=" + entry.getKey()
                    + ", node.id=" + node.getId());
            }
            for (TraceNode child : node.getChildren()) {
                if (child == null) {
                    errors.add("TRACE_ERROR: child node is null at parent: " + node.getId());
                    continue;
                }
                if (!nodes.containsKey(child.getId())) {
                    errors.add("TRACE_ERROR: child id not found in nodes map: " + child.getId());
                }
            }
        }

        return errors;
    }

    public static boolean isValid(TraceGraph graph) {
        return validateGraph(graph).isEmpty();
    }
}
