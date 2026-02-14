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
        }
        if (blank(document.domain())) {
            diagnostics.add("IRS002: domain must not be blank");
        }
        if (blank(document.action())) {
            diagnostics.add("IRS003: action must not be blank");
        }
        if (document.input() == null) {
            diagnostics.add("IRS004: input must not be null");
        }
        if (document.meta() == null) {
            diagnostics.add("IRS005: meta must not be null");
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
                }
            }
        }
        return new IntentIrSchemaValidationResult(diagnostics.isEmpty(), List.copyOf(diagnostics));
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
