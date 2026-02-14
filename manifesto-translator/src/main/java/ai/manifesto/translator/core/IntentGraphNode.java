package ai.manifesto.translator.core;

import java.util.Map;

/**
 * KR: 실행 단위 intent graph 노드입니다.
 * EN: Executable unit node in an intent graph.
 */
public record IntentGraphNode(
    String nodeId,
    String action,
    Map<String, Object> input,
    ResolutionStatus resolutionStatus
) {}
