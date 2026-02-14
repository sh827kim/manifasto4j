package ai.manifesto.translator.plugins;

import ai.manifesto.translator.TranslationDraft;
import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.TranslatorPipelinePlugin;
import ai.manifesto.translator.TranslatorPluginType;

import java.util.List;
import java.util.Map;

/**
 * KR: 입력 텍스트/메시지 기반 최소 커버리지를 점검하는 플러그인입니다.
 * EN: Plugin that checks minimal coverage based on input text and message metadata.
 */
public final class CoverageCheckerPlugin implements TranslatorPipelinePlugin {
    @Override
    public TranslatorPluginType type() {
        return TranslatorPluginType.INSPECTOR;
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public TranslationDraft afterInterpret(TranslationRequest request, TranslationDraft draft, List<String> diagnostics) {
        String text = draft == null || draft.input() == null ? "" : String.valueOf(draft.input().getOrDefault("text", ""));
        int messageCount = extractMessageCount(draft == null ? null : draft.input());

        if (messageCount <= 0) {
            diagnostics.add("PLG-COV-001: messageCount is zero");
        }
        if (text.isBlank()) {
            diagnostics.add("PLG-COV-002: input text is blank");
        }
        if (text.length() < 5) {
            diagnostics.add("PLG-COV-003: input text is too short for stable translation");
        }
        return draft;
    }

    private int extractMessageCount(Map<String, Object> input) {
        if (input == null) {
            return 0;
        }
        Object value = input.get("messageCount");
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }
}
