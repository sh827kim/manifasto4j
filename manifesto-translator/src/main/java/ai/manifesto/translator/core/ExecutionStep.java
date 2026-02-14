package ai.manifesto.translator.core;

import java.util.Map;

/**
 * KR: 정렬된 실행 플랜의 단일 스텝입니다.
 * EN: Single step in an ordered execution plan.
 */
public record ExecutionStep(
    String stepId,
    String action,
    Map<String, Object> input,
    int order
) {}
