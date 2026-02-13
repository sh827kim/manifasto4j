package ai.manifesto.intentir;

/**
 * KR: 외부 입력 Intent를 실행 경계에서 사용할 수 있는 Intent IR로 정규화하는 계약입니다.
 * EN: Contract for normalizing external intents into executable Intent IR.
 */
public interface IntentIrNormalizer {
    IntentIrDocument normalize(IntentIrDocument source);
}
