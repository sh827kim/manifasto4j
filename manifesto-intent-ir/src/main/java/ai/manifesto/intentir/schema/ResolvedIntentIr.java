package ai.manifesto.intentir.schema;

import java.util.List;

/**
 * KR: head/term/predicate/event를 묶은 해석 완료 Intent-IR 모델입니다.
 * EN: Resolved Intent-IR model combining head/term/predicate/event nodes.
 */
public record ResolvedIntentIr(
    List<IntentIrHead> heads,
    List<IntentIrTerm> terms,
    List<IntentIrPredicate> predicates,
    List<IntentIrEvent> events
) {}
