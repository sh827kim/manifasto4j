package ai.manifesto.intentir;

/**
 * KR: 부분적으로만 채워진 Intent IR 문서를 실행 가능한 형태로 해석/보정하는 계약입니다.
 * EN: Contract for resolving/repairing partially specified Intent IR documents into executable form.
 */
public interface IntentIrResolver {
    IntentIrResolveResult resolve(IntentIrDocument document);
}
