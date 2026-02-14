package ai.manifesto.translator.core;

import java.util.List;

/**
 * KR: intent graph를 실행 가능한 순서로 정렬한 결과입니다.
 * EN: Ordered execution result lowered from an intent graph.
 */
public record ExecutionPlan(
    List<ExecutionStep> steps,
    List<DependencyEdge> dependencies
) {}
