package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.IntentGraph;

import java.util.List;

/**
 * KR: 첫 그래프를 기준으로 유지하는 보수적 merge 전략입니다.
 * EN: Conservative merge strategy that keeps the first graph as canonical output.
 */
public final class ConservativeMergeStrategy implements MergeStrategy {
    @Override
    public IntentGraph merge(List<IntentGraph> graphs, MergeOptions options) {
        if (graphs == null || graphs.isEmpty()) {
            return new IntentGraph(List.of(), List.of(), java.util.Map.of("merged", "none"));
        }
        return graphs.get(0);
    }
}
