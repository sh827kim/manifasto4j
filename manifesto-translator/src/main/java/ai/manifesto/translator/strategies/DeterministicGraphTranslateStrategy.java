package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: 청크를 순차 실행 그래프로 변환하는 deterministic translate 전략입니다.
 * EN: Deterministic translate strategy converting chunks into sequential execution graph.
 */
public final class DeterministicGraphTranslateStrategy implements TranslateStrategy {
    @Override
    public IntentGraph translate(List<Chunk> chunks, TranslateOptions options) {
        if (chunks == null || chunks.isEmpty()) {
            return new IntentGraph(List.of(), List.of(), Map.of("empty", true));
        }
        List<IntentGraphNode> nodes = new ArrayList<>();
        List<DependencyEdge> edges = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = chunks.get(i);
            Map<String, Object> input = new LinkedHashMap<>();
            input.put("text", chunk.text());
            input.put("chunkId", chunk.id());
            nodes.add(new IntentGraphNode("node-" + i, "chunk.process", Map.copyOf(input), ResolutionStatus.RESOLVED));
            if (i > 0) {
                edges.add(new DependencyEdge("node-" + (i - 1), "node-" + i));
            }
        }
        return new IntentGraph(List.copyOf(nodes), List.copyOf(edges), Map.of("translated", true));
    }
}
