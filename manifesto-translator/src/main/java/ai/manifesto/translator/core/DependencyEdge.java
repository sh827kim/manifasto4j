package ai.manifesto.translator.core;

/**
 * KR: intent graph 노드 간 선행 의존성을 표현합니다.
 * EN: Represents precedence dependency between intent graph nodes.
 */
public record DependencyEdge(
    String fromNodeId,
    String toNodeId
) {}
