package ai.manifesto.intentir;

import ai.manifesto.intentir.schema.IntentIrEvent;
import ai.manifesto.intentir.schema.IntentIrHead;
import ai.manifesto.intentir.schema.IntentIrPredicate;
import ai.manifesto.intentir.schema.IntentIrSchemaValidator;
import ai.manifesto.intentir.schema.IntentIrTerm;
import ai.manifesto.intentir.schema.ResolvedIntentIr;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IntentIrSchemaAndLowerTest {

    @Test
    void schemaValidatorDetectsInvalidAndValidCases() {
        IntentIrSchemaValidator validator = new IntentIrSchemaValidator();
        IntentIrDocument invalid = new IntentIrDocument("", "todo", "", null, null);
        assertFalse(validator.validateDocument(invalid).valid());

        IntentIrDocument valid = new IntentIrDocument("1.0.0", "todo", "createTask", Map.of(), Map.of());
        assertTrue(validator.validateDocument(valid).valid());

        ResolvedIntentIr resolved = new ResolvedIntentIr(
            List.of(new IntentIrHead("h1", "force", Map.of())),
            List.of(new IntentIrTerm("x1", "task", Map.of())),
            List.of(new IntentIrPredicate("p1", "create", List.of("x1"), Map.of())),
            List.of(new IntentIrEvent("e1", "createTask", Map.of("theme", "x1")))
        );
        assertTrue(validator.validateResolved(resolved).valid());
    }

    @Test
    void lowererAddsExecutionBoundaryFields() {
        DefaultIntentIrLowerer lowerer = new DefaultIntentIrLowerer();
        IntentIrLowerResult lowered = lowerer.lower(new IntentIrDocument(
            "1.0.0",
            "todo",
            "createTask",
            Map.of("title", "milk"),
            Map.of("source", "chat")
        ));

        assertTrue(lowered.input().containsKey("_intentIr.domain"));
        assertTrue(lowered.meta().containsKey("lowered"));
        assertTrue(lowered.toIntent().getType().equals("createTask"));
    }

    @Test
    void schemaValidatorChecksSemverAndResolvedReferences() {
        IntentIrSchemaValidator validator = new IntentIrSchemaValidator();
        Map<String, Object> invalidInput = new LinkedHashMap<>();
        invalidInput.put("k", null);
        IntentIrDocument invalid = new IntentIrDocument("1.0", "todo!", "1-do", invalidInput, Map.of());
        var documentResult = validator.validateDocument(invalid);
        assertFalse(documentResult.valid());
        assertTrue(documentResult.diagnostics().stream().anyMatch(code -> code.startsWith("IRS006")));
        assertTrue(documentResult.diagnostics().stream().anyMatch(code -> code.startsWith("IRS007")));
        assertTrue(documentResult.diagnostics().stream().anyMatch(code -> code.startsWith("IRS008")));
        assertTrue(documentResult.diagnostics().stream().anyMatch(code -> code.startsWith("IRS010")));

        ResolvedIntentIr resolved = new ResolvedIntentIr(
            List.of(new IntentIrHead("h1", "force", Map.of())),
            List.of(new IntentIrTerm("x1", "task", Map.of())),
            List.of(new IntentIrPredicate("p1", "create", List.of("x2"), Map.of())),
            List.of(new IntentIrEvent("e1", "createTask", Map.of("theme", "x1")))
        );
        var resolvedResult = validator.validateResolved(resolved);
        assertFalse(resolvedResult.valid());
        assertTrue(resolvedResult.diagnostics().stream().anyMatch(code -> code.startsWith("IRS111")));
    }

    @Test
    void lowererEmitsDiagnosticsForUnknownOrBlankAction() {
        DefaultIntentIrLowerer lowerer = new DefaultIntentIrLowerer();
        IntentIrLowerResult lowered = lowerer.lower(new IntentIrDocument(
            "1.0.0",
            "todo",
            "unknown",
            Map.of(),
            Map.of()
        ));
        assertTrue(lowered.diagnostics().stream().anyMatch(code -> code.startsWith("LRW001")));
        assertTrue(lowered.meta().containsKey("loweredAt"));
        assertEquals(lowered.diagnostics().size(), lowered.meta().get("lowerDiagnosticsCount"));
    }
}
