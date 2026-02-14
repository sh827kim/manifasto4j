package ai.manifesto.intentir;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: 도메인별 허용 action 사전을 기반으로 Intent IR 유효성을 검사하는 기본 Lexicon 구현입니다.
 * EN: Default Lexicon implementation that validates Intent IR using domain-to-allowed-actions dictionaries.
 */
public final class DefaultIntentIrLexicon implements IntentIrLexicon {
    private final Map<String, IntentIrLexiconPolicy> policiesByDomain;

    public DefaultIntentIrLexicon(Map<String, Set<String>> allowedActionsByDomain) {
        Objects.requireNonNull(allowedActionsByDomain, "allowedActionsByDomain must not be null");
        Map<String, IntentIrLexiconPolicy> policies = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> entry : allowedActionsByDomain.entrySet()) {
            policies.put(
                normalize(entry.getKey()),
                new IntentIrLexiconPolicy(entry.getValue(), Set.of(), Set.of())
            );
        }
        this.policiesByDomain = Map.copyOf(policies);
    }

    public DefaultIntentIrLexicon(Map<String, IntentIrLexiconPolicy> policiesByDomain, boolean directPolicies) {
        Objects.requireNonNull(policiesByDomain, "policiesByDomain must not be null");
        Map<String, IntentIrLexiconPolicy> policies = new LinkedHashMap<>();
        for (Map.Entry<String, IntentIrLexiconPolicy> entry : policiesByDomain.entrySet()) {
            policies.put(normalize(entry.getKey()), entry.getValue());
        }
        this.policiesByDomain = Map.copyOf(policies);
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
            IntentIrLexiconPolicy policy = policiesByDomain.get(normalize(domain));
            if (policy == null) {
                diagnostics.add("LXC003: domain is not registered in lexicon");
            } else {
                Set<String> allowedActions = policy.allowedActions() == null ? Set.of() : policy.allowedActions();
                if (!action.isBlank() && !allowedActions.isEmpty() && !allowedActions.contains(action)) {
                    diagnostics.add("LXC004: action is not allowed for domain");
                }

                Set<String> requiredInput = policy.requiredInputKeys() == null ? Set.of() : policy.requiredInputKeys();
                Map<String, Object> input = document.input() == null ? Map.of() : document.input();
                for (String inputKey : requiredInput) {
                    if (!input.containsKey(inputKey)) {
                        diagnostics.add("LXC005: required input key is missing: " + inputKey);
                    }
                }

                Set<String> requiredMeta = policy.requiredMetaKeys() == null ? Set.of() : policy.requiredMetaKeys();
                Map<String, Object> meta = document.meta() == null ? Map.of() : document.meta();
                for (String metaKey : requiredMeta) {
                    if (!meta.containsKey(metaKey)) {
                        diagnostics.add("LXC006: required meta key is missing: " + metaKey);
                    }
                }
            }
        }

        return new IntentIrLexiconCheckResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
