package ai.manifesto.translator.helpers;

import ai.manifesto.translator.core.DiagnosticLevel;
import ai.manifesto.translator.core.GraphValidationResult;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.TranslatorDiagnostic;
import ai.manifesto.translator.invariants.CausalIntegrityChecker;
import ai.manifesto.translator.invariants.CompletenessChecker;
import ai.manifesto.translator.invariants.ReferentialIdentityChecker;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: intent graph의 기본 유효성을 헬퍼 + invariant로 검증합니다.
 * EN: Validates base intent graph consistency using helpers and invariants.
 */
public final class TranslatorGraphValidator {
    public GraphValidationResult validate(IntentGraph graph) {
        List<TranslatorDiagnostic> diagnostics = new ArrayList<>();
        if (graph == null) {
            diagnostics.add(new TranslatorDiagnostic("GRV001", DiagnosticLevel.ERROR, "graph must not be null"));
            return new GraphValidationResult(false, List.copyOf(diagnostics));
        }
        if (new CausalIntegrityChecker().hasCycle(graph)) {
            diagnostics.add(new TranslatorDiagnostic("INV101", DiagnosticLevel.ERROR, "causal cycle detected"));
        }
        if (!new CompletenessChecker().isComplete(graph)) {
            diagnostics.add(new TranslatorDiagnostic("INV201", DiagnosticLevel.WARNING, "graph has abstract/partial nodes"));
        }
        if (!new ReferentialIdentityChecker().isValid(graph)) {
            diagnostics.add(new TranslatorDiagnostic("INV301", DiagnosticLevel.ERROR, "node identity duplicated"));
        }
        return new GraphValidationResult(
            diagnostics.stream().noneMatch(d -> d.level() == DiagnosticLevel.ERROR),
            List.copyOf(diagnostics)
        );
    }
}
