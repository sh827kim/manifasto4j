package ai.manifesto.intentir.schema;

import java.util.List;
import java.util.Map;

/**
 * KR: 사건/행동 의미를 구성하는 predicate 노드입니다.
 * EN: Predicate node composing event/action semantics.
 */
public record IntentIrPredicate(
    String id,
    String name,
    List<String> arguments,
    Map<String, Object> features
) {}
