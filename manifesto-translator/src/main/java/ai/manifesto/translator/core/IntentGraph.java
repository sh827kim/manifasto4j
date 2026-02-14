package ai.manifesto.translator.core;

import java.util.List;
import java.util.Map;

/**
 * KR: 번역 결과를 실행 그래프로 표현한 모델입니다.
 * EN: Model representing translation output as an execution graph.
 */
public record IntentGraph(
    List<IntentGraphNode> nodes,
    List<DependencyEdge> edges,
    Map<String, Object> meta
) {}
