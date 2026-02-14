package ai.manifesto.intentir;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: context/actionHint 규칙으로 미해결 action을 보정하는 기본 Resolver 구현입니다.
 * EN: Default Resolver implementation that repairs unresolved actions using context/actionHint rules.
 */
public final class DefaultIntentIrResolver implements IntentIrResolver {
    private final Map<String, Set<String>> allowedActionsByDomain;
    private final IntentIrNormalizer normalizer;

    public DefaultIntentIrResolver(Map<String, Set<String>> allowedActionsByDomain) {
        this(allowedActionsByDomain, new DefaultIntentIrNormalizer());
    }

    public DefaultIntentIrResolver(Map<String, Set<String>> allowedActionsByDomain, IntentIrNormalizer normalizer) {
        this.allowedActionsByDomain = Objects.requireNonNull(allowedActionsByDomain, "allowedActionsByDomain must not be null");
        this.normalizer = Objects.requireNonNull(normalizer, "normalizer must not be null");
    }

    @Override
    public IntentIrResolveResult resolve(IntentIrDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        List<String> diagnostics = new ArrayList<>();

        IntentIrDocument normalized = normalizer.normalize(document);
        String domain = normalized.domain();
        String action = normalized.action();

        if ("unknown".equalsIgnoreCase(action) || action.isBlank()) {
            String hinted = extractActionHint(normalized.meta());
            if (hinted != null && isAllowed(domain, hinted)) {
                action = hinted;
                diagnostics.add("RSV001: action resolved from meta.actionHint");
            }
        }

        if ("unknown".equalsIgnoreCase(action) || action.isBlank()) {
            Set<String> allowed = allowedActionsByDomain.get(domain);
            if (allowed != null && !allowed.isEmpty()) {
                action = allowed.stream().sorted().findFirst().orElse(action);
                diagnostics.add("RSV002: action fallback resolved from lexicon default");
            }
        }

        if (action == null || action.isBlank() || "unknown".equalsIgnoreCase(action)) {
            diagnostics.add("RSV003: action unresolved");
        } else if (!isAllowed(domain, action)) {
            diagnostics.add("RSV004: resolved action is not allowed by lexicon");
        }

        Map<String, Object> meta = new LinkedHashMap<>(normalized.meta());
        meta.put("resolved", diagnostics.stream().noneMatch(code -> code.startsWith("RSV003") || code.startsWith("RSV004")));

        IntentIrDocument resolved = new IntentIrDocument(
            normalized.schemaVersion(),
            domain,
            action,
            normalized.input(),
            meta
        );
        return new IntentIrResolveResult(resolved, List.copyOf(diagnostics));
    }

    private boolean isAllowed(String domain, String action) {
        Set<String> allowed = allowedActionsByDomain.get(domain);
        return allowed != null && allowed.contains(action);
    }

    private String extractActionHint(Map<String, Object> meta) {
        if (meta == null) {
            return null;
        }
        Object actionHint = meta.get("actionHint");
        if (actionHint == null) {
            return null;
        }
        String text = String.valueOf(actionHint).trim();
        return text.isBlank() ? null : text;
    }
}
