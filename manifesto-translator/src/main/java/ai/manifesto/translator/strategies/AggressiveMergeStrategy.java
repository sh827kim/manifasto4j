package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: 여러 그래프의 노드/엣지를 결합하는 공격적 merge 전략입니다.
 * EN: Aggressive merge strategy combining nodes/edges from all graphs.
 */
public final class AggressiveMergeStrategy implements MergeStrategy {
    @Override
    public IntentGraph merge(List<IntentGraph> graphs, MergeOptions options) {
        if (graphs == null || graphs.isEmpty()) {
            return new IntentGraph(List.of(), List.of(), Map.of("merged", "none"));
        }
        List<IntentGraphNode> nodes = new ArrayList<>();
        List<DependencyEdge> edges = new ArrayList<>();
        for (IntentGraph graph : graphs) {
            if (graph == null) {
                continue;
            }
            if (graph.nodes() != null) {
                nodes.addAll(graph.nodes());
            }
            if (graph.edges() != null) {
                edges.addAll(graph.edges());
            }
        }
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("merged", "aggressive");
        meta.put("graphCount", graphs.size());
        return new IntentGraph(List.copyOf(nodes), List.copyOf(edges), Map.copyOf(meta));
    }
}
