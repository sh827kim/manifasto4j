package ai.manifesto.translator.targets;

/**
 * KR: 번역 그래프를 target 산출물로 내보내는 출력 포트 계약입니다.
 * EN: Output port contract that exports translation graphs into target artifacts.
 */
public interface TargetExporter<TOut, TCtx> {
    String id();

    TOut export(ExportInput input, TCtx context);
}
