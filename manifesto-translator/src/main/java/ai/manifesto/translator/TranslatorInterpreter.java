package ai.manifesto.translator;

/**
 * KR: 요청 메시지/컨텍스트를 Intent 초안으로 변환하는 interpret 단계 계약입니다.
 * EN: Interpret-stage contract that converts request messages/context into an intent draft.
 */
public interface TranslatorInterpreter {
    TranslationDraft interpret(TranslationRequest request);
}
