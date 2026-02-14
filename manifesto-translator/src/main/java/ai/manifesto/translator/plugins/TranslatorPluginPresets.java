package ai.manifesto.translator.plugins;

import ai.manifesto.translator.TranslatorPipelinePlugin;

import java.util.List;

/**
 * KR: translator pipeline plugin 기본 조합 프리셋입니다.
 * EN: Preset bundles for translator pipeline plugins.
 */
public final class TranslatorPluginPresets {
    private TranslatorPluginPresets() {
    }

    public static List<TranslatorPipelinePlugin> conformanceSet() {
        return List.of(
            new OrDetectorPlugin(),
            new TaskEnumerationPlugin(),
            new CoverageCheckerPlugin(),
            new DependencyRepairPlugin()
        );
    }
}
