package ai.manifesto.intentir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: 도메인별 허용 action 사전을 기반으로 Intent IR 유효성을 검사하는 기본 Lexicon 구현입니다.
 * EN: Default Lexicon implementation that validates Intent IR using domain-to-allowed-actions dictionaries.
 */
public final class DefaultIntentIrLexicon implements IntentIrLexicon {
    private final Map<String, Set<String>> allowedActionsByDomain;

    public DefaultIntentIrLexicon(Map<String, Set<String>> allowedActionsByDomain) {
        this.allowedActionsByDomain = Objects.requireNonNull(allowedActionsByDomain, "allowedActionsByDomain must not be null");
    }

    @Override
    public IntentIrLexiconCheckResult check(IntentIrDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        List<String> diagnostics = new ArrayList<>();

        String domain = safe(document.domain());
        String action = safe(document.action());
        if (domain.isBlank()) {
            diagnostics.add("LXC001: domain must not be blank");
        }
        if (action.isBlank()) {
            diagnostics.add("LXC002: action must not be blank");
        }

        if (!domain.isBlank()) {
            Set<String> allowedActions = allowedActionsByDomain.get(domain);
            if (allowedActions == null) {
                diagnostics.add("LXC003: domain is not registered in lexicon");
            } else if (!action.isBlank() && !allowedActions.contains(action)) {
                diagnostics.add("LXC004: action is not allowed for domain");
            }
        }

        return new IntentIrLexiconCheckResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
