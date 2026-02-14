package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.IntentGraph;

import java.util.List;

/**
 * KR: 복수 intent graph를 하나로 합치는 전략 계약입니다.
 * EN: Strategy contract for merging multiple intent graphs into one.
 */
public interface MergeStrategy {
    IntentGraph merge(List<IntentGraph> graphs, MergeOptions options);
}
