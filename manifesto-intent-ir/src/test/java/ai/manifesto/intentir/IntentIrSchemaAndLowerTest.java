package ai.manifesto.intentir;

import ai.manifesto.intentir.schema.IntentIrEvent;
import ai.manifesto.intentir.schema.IntentIrHead;
import ai.manifesto.intentir.schema.IntentIrPredicate;
import ai.manifesto.intentir.schema.IntentIrSchemaValidator;
import ai.manifesto.intentir.schema.IntentIrTerm;
import ai.manifesto.intentir.schema.ResolvedIntentIr;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
