package ai.manifesto.intentir.schema;

import ai.manifesto.intentir.IntentIrDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * KR: Intent-IR 문서/해석 모델의 구조적 유효성을 점검하는 validator입니다.
 * EN: Validator for structural validity of Intent-IR document/resolved model.
 */
public final class IntentIrSchemaValidator {
    public IntentIrSchemaValidationResult validateDocument(IntentIrDocument document) {
        Objects.requireNonNull(document, "document must not be null");
        List<String> diagnostics = new ArrayList<>();
        if (blank(document.schemaVersion())) {
            diagnostics.add("IRS001: schemaVersion must not be blank");
        } else if (!document.schemaVersion().matches("^\\d+\\.\\d+\\.\\d+$")) {
            diagnostics.add("IRS006: schemaVersion must follow semver (x.y.z)");
        }
        if (blank(document.domain())) {
            diagnostics.add("IRS002: domain must not be blank");
        } else if (!document.domain().matches("^[a-zA-Z0-9._-]+$")) {
            diagnostics.add("IRS007: domain contains invalid characters");
        }
        if (blank(document.action())) {
            diagnostics.add("IRS003: action must not be blank");
        } else if (!document.action().matches("^[a-zA-Z][a-zA-Z0-9._-]*$")) {
            diagnostics.add("IRS008: action contains invalid characters");
        }
        if (document.input() == null) {
            diagnostics.add("IRS004: input must not be null");
        } else {
            for (Map.Entry<String, Object> entry : document.input().entrySet()) {
                if (blank(entry.getKey())) {
                    diagnostics.add("IRS009: input key must not be blank");
                }
                if (entry.getValue() == null) {
                    diagnostics.add("IRS010: input value must not be null for key: " + entry.getKey());
                }
            }
        }
        if (document.meta() == null) {
            diagnostics.add("IRS005: meta must not be null");
        } else {
            for (Map.Entry<String, Object> entry : document.meta().entrySet()) {
                if (blank(entry.getKey())) {
                    diagnostics.add("IRS011: meta key must not be blank");
                }
            }
        }
        return new IntentIrSchemaValidationResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }

    public IntentIrSchemaValidationResult validateResolved(ResolvedIntentIr resolved) {
        Objects.requireNonNull(resolved, "resolved must not be null");
        List<String> diagnostics = new ArrayList<>();
        if (resolved.events() == null || resolved.events().isEmpty()) {
            diagnostics.add("IRS101: at least one event is required");
        }
        if (resolved.heads() == null || resolved.heads().isEmpty()) {
            diagnostics.add("IRS102: at least one head is required");
        }
        if (resolved.events() != null) {
            for (IntentIrEvent event : resolved.events()) {
                if (event == null) {
                    diagnostics.add("IRS103: event entry must not be null");
                    continue;
                }
                if (blank(event.action())) {
                    diagnostics.add("IRS104: event.action must not be blank");
                }
                Map<String, String> roles = event.roles();
                if (roles == null || roles.isEmpty()) {
                    diagnostics.add("IRS105: event.roles must not be empty");
                } else {
                    for (Map.Entry<String, String> roleEntry : roles.entrySet()) {
                        if (blank(roleEntry.getKey())) {
                            diagnostics.add("IRS106: event role key must not be blank");
                        }
                        if (blank(roleEntry.getValue())) {
                            diagnostics.add("IRS107: event role value must not be blank");
                        }
                    }
                }
            }
        }
        validateResolvedReferences(resolved, diagnostics);
        return new IntentIrSchemaValidationResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }

    private void validateResolvedReferences(ResolvedIntentIr resolved, List<String> diagnostics) {
        List<IntentIrTerm> terms = resolved.terms() == null ? List.of() : resolved.terms();
        List<String> termIds = terms.stream()
            .filter(Objects::nonNull)
            .map(IntentIrTerm::id)
            .filter(id -> id != null && !id.isBlank())
            .toList();

        if (resolved.predicates() == null) {
            return;
        }
        for (IntentIrPredicate predicate : resolved.predicates()) {
            if (predicate == null) {
                diagnostics.add("IRS108: predicate entry must not be null");
                continue;
            }
            if (blank(predicate.name())) {
                diagnostics.add("IRS109: predicate.name must not be blank");
            }
            List<String> arguments = predicate.arguments() == null ? List.of() : predicate.arguments();
            for (String arg : arguments) {
                if (blank(arg)) {
                    diagnostics.add("IRS110: predicate argument must not be blank");
                    continue;
                }
                if (!termIds.contains(arg)) {
                    diagnostics.add("IRS111: predicate argument references unknown term: " + arg);
                }
            }
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
