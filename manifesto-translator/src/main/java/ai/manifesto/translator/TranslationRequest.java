package ai.manifesto.translator;

import java.util.List;
import java.util.Map;

/**
 * KR: 자연어 입력을 Intent IR로 변환하기 위한 요청 모델입니다.
 * EN: Request model for converting natural language inputs into Intent IR.
 */
public record TranslationRequest(
    String domainName,
    String actionHint,
    List<TranslatorMessage> messages,
    Map<String, Object> context
) {}
