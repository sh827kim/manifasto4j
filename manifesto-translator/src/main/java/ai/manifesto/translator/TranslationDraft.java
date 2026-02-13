package ai.manifesto.translator;

import java.util.List;
import java.util.Map;

/**
 * KR: interpret 단계에서 생성되는 중간 번역 초안 모델입니다.
 * EN: Intermediate translation draft model produced by the interpret stage.
 */
public record TranslationDraft(
    String domainName,
    String actionName,
    Map<String, Object> input,
    Map<String, Object> meta,
    List<String> diagnostics
) {}
