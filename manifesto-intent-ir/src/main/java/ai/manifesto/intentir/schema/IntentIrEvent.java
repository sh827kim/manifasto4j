package ai.manifesto.intentir.schema;

import java.util.Map;

/**
 * KR: 실행 가능한 intent event 노드입니다.
 * EN: Executable intent event node.
 */
public record IntentIrEvent(
    String id,
    String action,
    Map<String, String> roles
) {}
