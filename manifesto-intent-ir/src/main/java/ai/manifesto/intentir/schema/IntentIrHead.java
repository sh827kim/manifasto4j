package ai.manifesto.intentir.schema;

import java.util.Map;

/**
 * KR: Intent-IR 문서의 기능적 head 노드 표현입니다.
 * EN: Functional head node representation in an Intent-IR document.
 */
public record IntentIrHead(
    String id,
    String type,
    Map<String, Object> features
) {}
