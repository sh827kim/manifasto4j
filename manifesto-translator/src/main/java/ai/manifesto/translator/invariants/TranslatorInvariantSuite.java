package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.IntentGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: translator graph invariant 체크를 일괄 실행하는 유틸리티입니다.
 * EN: Utility that runs translator graph invariant checks as a suite.
 */
public final class TranslatorInvariantSuite {
    private final CausalIntegrityChecker causalIntegrityChecker = new CausalIntegrityChecker();
    private final CompletenessChecker completenessChecker = new CompletenessChecker();
    private final StatefulnessChecker statefulnessChecker = new StatefulnessChecker();
    private final ReferentialIdentityChecker referentialIdentityChecker = new ReferentialIdentityChecker();
    private final AbstractDependencyChecker abstractDependencyChecker = new AbstractDependencyChecker();

    public List<String> check(IntentGraph graph) {
        List<String> diagnostics = new ArrayList<>();
        if (graph == null) {
            diagnostics.add("INV000: graph is null");
            return List.copyOf(diagnostics);
        }

        if (causalIntegrityChecker.hasCycle(graph)) {
            diagnostics.add("INV001: graph contains a dependency cycle");
        }
        if (!completenessChecker.isComplete(graph)) {
            diagnostics.add("INV002: graph has unresolved abstract gaps");
        }
        if (!statefulnessChecker.isStateful(graph)) {
            diagnostics.add("INV003: no stateful action detected in graph");
        }
        if (!referentialIdentityChecker.isValid(graph)) {
            diagnostics.add("INV004: graph references are unstable");
        }
        if (abstractDependencyChecker.hasAbstractDependency(graph)) {
            diagnostics.add("INV005: abstract node is used as executable dependency");
        }
        return List.copyOf(diagnostics);
    }
}
