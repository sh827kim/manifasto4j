package ai.manifesto.translator.pipeline;

/**
 * KR: translator pipeline 실행 옵션입니다.
 * EN: Execution options for translator pipeline.
 */
public record TranslatorPipelineOptions(
    DiagnosticsAggregationPolicy diagnosticsPolicy,
    boolean sortPluginsByPriority
) {
    public static TranslatorPipelineOptions defaults() {
        return new TranslatorPipelineOptions(DiagnosticsAggregationPolicy.DEDUP, true);
    }
}
