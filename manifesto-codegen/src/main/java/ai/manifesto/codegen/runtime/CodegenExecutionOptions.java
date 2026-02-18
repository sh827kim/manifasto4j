package ai.manifesto.codegen.runtime;

/**
 * KR: runner 상세 실행 옵션입니다.
 * EN: Detailed execution options for codegen runner.
 */
public record CodegenExecutionOptions(
    String sourceId,
    boolean stamp,
    boolean prependGeneratedHeader,
    CodegenPluginOptions pluginOptions,
    String outDir,
    boolean cleanOutDir,
    boolean flushToDisk
) {
    public CodegenExecutionOptions(
        String sourceId,
        boolean stamp,
        boolean prependGeneratedHeader,
        CodegenPluginOptions pluginOptions
    ) {
        this(sourceId, stamp, prependGeneratedHeader, pluginOptions, null, true, false);
    }

    public static CodegenExecutionOptions defaults() {
        return new CodegenExecutionOptions("unknown", false, true, CodegenPluginOptions.defaults(), null, true, false);
    }

    public CodegenExecutionOptions withOutputDirectory(String outDir, boolean cleanOutDir) {
        return new CodegenExecutionOptions(
            sourceId,
            stamp,
            prependGeneratedHeader,
            pluginOptions,
            outDir,
            cleanOutDir,
            true
        );
    }
}
