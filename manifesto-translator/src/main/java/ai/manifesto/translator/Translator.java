package ai.manifesto.translator;

/**
 * KR: 자연어/대화 입력을 Intent IR로 변환하는 Translator 엔진 계약입니다.
 * EN: Translator engine contract that converts NL/chat inputs into Intent IR.
 */
public interface Translator {
    TranslationResult translate(TranslationRequest request);
}
