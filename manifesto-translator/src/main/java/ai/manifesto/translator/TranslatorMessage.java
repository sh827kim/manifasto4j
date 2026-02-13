package ai.manifesto.translator;

import java.util.Map;

/**
 * KR: 번역 파이프라인의 표준 메시지 단위입니다.
 * EN: Standard message unit used by the translator pipeline.
 */
public record TranslatorMessage(
    String role,
    String content,
    Map<String, Object> attributes
) {}
