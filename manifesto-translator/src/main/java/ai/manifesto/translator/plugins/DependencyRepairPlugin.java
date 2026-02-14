package ai.manifesto.translator.plugins;

import ai.manifesto.translator.TranslationDraft;
import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.TranslatorPipelinePlugin;
import ai.manifesto.translator.TranslatorPluginType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * KR: draft meta에 dependency 관련 기본 정책을 보정하는 플러그인입니다.
 * EN: Plugin that repairs draft meta with default dependency policy settings.
 */
public final class DependencyRepairPlugin implements TranslatorPipelinePlugin {
    @Override
    public TranslatorPluginType type() {
        return TranslatorPluginType.TRANSFORMER;
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public TranslationDraft afterVerify(TranslationRequest request, TranslationDraft verifiedDraft, List<String> diagnostics) {
        if (verifiedDraft == null) {
            return null;
        }
        Map<String, Object> nextMeta = new LinkedHashMap<>(verifiedDraft.meta() == null ? Map.of() : verifiedDraft.meta());
        if (!nextMeta.containsKey("dependencyMode")) {
            nextMeta.put("dependencyMode", "auto");
            diagnostics.add("PLG-DEP-001: dependencyMode defaulted to auto");
        }
        if (!nextMeta.containsKey("dependencyRepairApplied")) {
            nextMeta.put("dependencyRepairApplied", true);
        }

        return new TranslationDraft(
            verifiedDraft.domainName(),
            verifiedDraft.actionName(),
            verifiedDraft.input(),
            Map.copyOf(nextMeta),
            verifiedDraft.diagnostics()
        );
    }
}
