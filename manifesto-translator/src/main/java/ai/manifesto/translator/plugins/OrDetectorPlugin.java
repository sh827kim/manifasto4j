package ai.manifesto.translator.plugins;

import ai.manifesto.translator.TranslationRequest;
import ai.manifesto.translator.TranslatorMessage;
import ai.manifesto.translator.TranslatorPipelinePlugin;
import ai.manifesto.translator.TranslatorPluginType;

import java.util.List;

/**
 * KR: 사용자 발화의 OR 패턴을 감지해 모호성 진단을 추가하는 플러그인입니다.
 * EN: Plugin that detects OR patterns in user utterances and adds ambiguity diagnostics.
 */
public final class OrDetectorPlugin implements TranslatorPipelinePlugin {
    @Override
    public TranslatorPluginType type() {
        return TranslatorPluginType.INSPECTOR;
    }

    @Override
    public int priority() {
        return 40;
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
            String content = message.content().toLowerCase();
            if (content.contains(" or ")) {
                diagnostics.add("PLG-OR-001: disjunctive intent detected ('or')");
                return;
            }
        }
    }
}
