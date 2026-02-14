package ai.manifesto.intentir.schema;

import java.util.Map;

/**
 * KR: 엔티티/개체를 나타내는 term 노드입니다.
 * EN: Term node representing an entity participant.
 */
public record IntentIrTerm(
    String id,
    String label,
    Map<String, Object> attributes
) {}
