package ai.manifesto.translator;

import java.util.List;

/**
 * KR: 특정 LLM/채팅 SDK 메시지 타입과 Translator 표준 메시지 간 변환 계약입니다.
 * EN: Conversion contract between framework-specific chat message types and translator-standard messages.
 *
 * 구현 필수 규칙 / Required implementation rules:
 * 1) 역할(role) 매핑을 보존해야 합니다. (system/user/assistant/tool 등)
 * 2) 본문(content) 손실 없이 양방향 변환해야 합니다.
 * 3) framework 메타데이터는 attributes에 안정적으로 매핑해야 합니다.
 * 4) 지원하지 않는 필드는 예외 대신 attributes 확장 키로 보관해야 합니다.
 */
public interface TranslatorMessageAdapter<TExternalMessage> {
    List<TranslatorMessage> toTranslatorMessages(List<TExternalMessage> externalMessages);

    List<TExternalMessage> toExternalMessages(List<TranslatorMessage> translatorMessages);
}
