package ai.manifesto.intentir;

import java.util.ArrayList;
import java.util.Collection;
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

                validateThetaAndSelectional(policy, action, input, diagnostics);
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

    @SuppressWarnings("unchecked")
    private void validateThetaAndSelectional(
        IntentIrLexiconPolicy policy,
        String action,
        Map<String, Object> input,
        List<String> diagnostics
    ) {
        if (action == null || action.isBlank()) {
            return;
        }
        Map<String, Set<String>> requiredRolesByAction = policy.requiredRolesByAction() == null
            ? Map.of()
            : policy.requiredRolesByAction();
        Map<String, Set<String>> selectionalRestrictionsByRole = policy.selectionalRestrictionsByRole() == null
            ? Map.of()
            : policy.selectionalRestrictionsByRole();

        Object rolesValue = input.get("roles");
        Map<String, Object> roles = rolesValue instanceof Map<?, ?> roleMap
            ? (Map<String, Object>) roleMap
            : Map.of();

        Set<String> requiredRoles = requiredRolesByAction.getOrDefault(action, Set.of());
        for (String requiredRole : requiredRoles) {
            if (!roles.containsKey(requiredRole)) {
                diagnostics.add("LXC007: required theta role is missing for action " + action + ": " + requiredRole);
            }
        }

        for (Map.Entry<String, Set<String>> restriction : selectionalRestrictionsByRole.entrySet()) {
            String role = restriction.getKey();
            if (!roles.containsKey(role)) {
                continue;
            }
            String roleType = extractRoleType(roles.get(role));
            Set<String> allowedTypes = restriction.getValue() == null ? Set.of() : restriction.getValue();
            if (roleType == null || roleType.isBlank()) {
                diagnostics.add("LXC008: selectional restriction cannot be checked (missing role type): " + role);
                continue;
            }
            if (!allowedTypes.isEmpty() && !allowedTypes.contains(roleType)) {
                diagnostics.add("LXC009: selectional restriction violated for role " + role + ": " + roleType);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String extractRoleType(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return text.isBlank() ? null : text.trim();
        }
        if (value instanceof Map<?, ?> map) {
            Object type = map.get("type");
            if (type == null) {
                type = map.get("entityType");
            }
            if (type == null) {
                type = map.get("class");
            }
            return type == null ? null : String.valueOf(type).trim();
        }
        if (value instanceof Collection<?> collection && !collection.isEmpty()) {
            Object first = collection.iterator().next();
            return extractRoleType(first);
        }
        return String.valueOf(value).trim();
    }
}
