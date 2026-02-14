package ai.manifesto.translator.targets.manifesto;

import ai.manifesto.translator.core.DependencyEdge;

import java.util.List;

/**
 * KR: IntentGraph로부터 유도된 Manifesto 실행 계획입니다.
 * EN: Manifesto execution plan derived from IntentGraph.
 */
public record InvocationPlan(
    List<InvocationStep> steps,
    List<DependencyEdge> dependencyEdges,
    List<String> abstractNodes
) {}
