package ai.manifesto.translator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: 번역 초안의 최소 유효성(도메인/액션/입력)을 검증하는 기본 verify 구현입니다.
 * EN: Default verify implementation that checks minimum validity for domain/action/input.
 */
public final class DefaultTranslatorVerifier implements TranslatorVerifier {
    @Override
    public TranslationDraft verify(TranslationRequest request, TranslationDraft draft) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(draft, "draft must not be null");

        List<String> diagnostics = new ArrayList<>();
        if (draft.diagnostics() != null) {
            diagnostics.addAll(draft.diagnostics());
        }

        String domainName = draft.domainName() == null ? "" : draft.domainName().trim();
        if (domainName.isBlank()) {
            diagnostics.add("TRV001: domainName must not be blank");
        }

        String actionName = draft.actionName() == null ? "" : draft.actionName().trim();
        if (actionName.isBlank() || "unknown".equals(actionName)) {
            diagnostics.add("TRV002: actionName is unresolved");
        }

        Map<String, Object> input = new LinkedHashMap<>();
        if (draft.input() != null) {
            input.putAll(draft.input());
        }
        input.putIfAbsent("text", "");
        input.putIfAbsent("messageCount", request.messages() == null ? 0 : request.messages().size());

        Map<String, Object> meta = new LinkedHashMap<>();
        if (draft.meta() != null) {
            meta.putAll(draft.meta());
        }
        meta.putIfAbsent("verified", Boolean.TRUE);

        return new TranslationDraft(domainName, actionName, input, meta, List.copyOf(diagnostics));
    }
}
