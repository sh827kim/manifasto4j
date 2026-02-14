package ai.manifesto.translator.plugins;

import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.TranslatorMessage;
import ai.manifesto.translator.TranslatorPipelinePlugin;
import ai.manifesto.translator.TranslatorPluginType;

import java.util.List;

/**
 * KR: 목록형 사용자 입력을 감지해 task enumeration 진단을 추가하는 플러그인입니다.
 * EN: Plugin that detects list-style user inputs and emits task-enumeration diagnostics.
 */
public final class TaskEnumerationPlugin implements TranslatorPipelinePlugin {
    @Override
    public TranslatorPluginType type() {
        return TranslatorPluginType.INSPECTOR;
    }

    @Override
    public int priority() {
        return 35;
    }

    @Override
    public void beforeInterpret(TranslationRequest request, List<String> diagnostics) {
        if (request == null || request.messages() == null) {
            return;
        }
        for (TranslatorMessage message : request.messages()) {
            if (message == null || message.content() == null) {
                continue;
            }
            String[] lines = message.content().split("\\R");
            int enumerated = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.matches("^[-*]\\s+.+") || trimmed.matches("^\\d+[.)]\\s+.+")) {
                    enumerated++;
                }
            }
            if (enumerated >= 2) {
                diagnostics.add("PLG-TASK-001: enumerated tasks detected: " + enumerated);
                return;
            }
        }
    }
}
