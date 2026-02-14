package ai.manifesto.codegen.runtime;

/**
 * KR: runner 상세 실행 옵션입니다.
 * EN: Detailed execution options for codegen runner.
 */
public record CodegenExecutionOptions(
    String sourceId,
    boolean stamp,
    boolean prependGeneratedHeader,
    CodegenPluginOptions pluginOptions
) {
    public static CodegenExecutionOptions defaults() {
        return new CodegenExecutionOptions("unknown", false, true, CodegenPluginOptions.defaults());
    }
}
