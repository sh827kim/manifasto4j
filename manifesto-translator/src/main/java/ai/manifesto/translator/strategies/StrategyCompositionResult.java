package ai.manifesto.translator.strategies;

import ai.manifesto.translator.core.ExecutionPlan;
import ai.manifesto.translator.core.IntentGraph;

/**
 * KR: strategy 조합 실행 결과(graph + execution plan)입니다.
 * EN: Result of strategy composition (graph + execution plan).
 */
public record StrategyCompositionResult(
    IntentGraph graph,
    ExecutionPlan executionPlan
) {}
