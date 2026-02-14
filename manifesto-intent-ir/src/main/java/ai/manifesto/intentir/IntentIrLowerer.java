package ai.manifesto.intentir;

/**
 * KR: Intent-IR 문서를 실행 경계 payload로 낮추는 계약입니다.
 * EN: Contract for lowering an Intent-IR document into executable boundary payload.
 */
public interface IntentIrLowerer {
    IntentIrLowerResult lower(IntentIrDocument document);
}
