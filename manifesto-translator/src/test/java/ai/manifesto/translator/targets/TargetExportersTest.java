package ai.manifesto.translator.targets;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.core.ResolutionStatus;
import ai.manifesto.translator.targets.json.JsonOutput;
import ai.manifesto.translator.targets.json.JsonTargetExporter;
import ai.manifesto.translator.targets.manifesto.LoweringFailureKind;
import ai.manifesto.translator.targets.manifesto.ManifestoBundle;
import ai.manifesto.translator.targets.manifesto.ManifestoExportContext;
import ai.manifesto.translator.targets.manifesto.ManifestoTargetExporter;
import ai.manifesto.translator.targets.openapi.OpenApiSpec;
import ai.manifesto.translator.targets.openapi.OpenApiTargetExporter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TargetExportersTest {

    @Test
    void jsonExporterProducesDeterministicNodesAndEdges() {
        IntentGraph graph = new IntentGraph(
            List.of(
                new IntentGraphNode("n2", "task.close", Map.of("id", 10), ResolutionStatus.RESOLVED),
                new IntentGraphNode("n1", "task.create", Map.of("title", "T"), ResolutionStatus.RESOLVED)
            ),
            List.of(new DependencyEdge("n1", "n2")),
            Map.of()
        );

        JsonOutput output = new JsonTargetExporter().export(new ExportInput(graph, List.of(), null), null);
        assertEquals(2, output.nodes().size());
        assertEquals("n1", output.nodes().get(0).id());
        assertEquals(List.of("n1"), output.nodes().get(1).dependencies());
        assertEquals(1, output.edges().size());
        assertEquals("n1", output.edges().get(0).from());
    }

    @Test
    void manifestoExporterBuildsInvocationMetaAndCandidates() {
        IntentGraph graph = new IntentGraph(
            List.of(
                new IntentGraphNode("n1", "task.create", Map.of("title", "T"), ResolutionStatus.RESOLVED),
                new IntentGraphNode("n2", "", Map.of(), ResolutionStatus.RESOLVED),
                new IntentGraphNode("n3", "task.close", Map.of("id", 1), ResolutionStatus.ABSTRACT)
            ),
            List.of(
                new DependencyEdge("n1", "n2"),
                new DependencyEdge("n2", "n3")
            ),
            Map.of()
        );

        ManifestoBundle bundle = new ManifestoTargetExporter().export(
            new ExportInput(graph, List.of(), null),
            ManifestoExportContext.defaults()
        );

        assertEquals(3, bundle.meta().nodeCount());
        assertTrue(bundle.meta().deferredCount() + bundle.meta().failedCount() >= 1);
        assertFalse(bundle.invocationPlan().abstractNodes().isEmpty());
        assertTrue(bundle.extensionCandidates().stream().anyMatch(c -> "MEL_CANDIDATE".equals(c.kind())));
    }

    @Test
    void openApiExporterBuildsStableActionPaths() {
        IntentGraph graph = new IntentGraph(
            List.of(
                new IntentGraphNode("n1", "Task.Create", Map.of(), ResolutionStatus.RESOLVED),
                new IntentGraphNode("n2", "task-close", Map.of(), ResolutionStatus.RESOLVED)
            ),
            List.of(),
            Map.of()
        );

        OpenApiSpec spec = new OpenApiTargetExporter().export(new ExportInput(graph, List.of(), null), null);
        assertEquals("3.0.3", spec.openapi());
        assertTrue(spec.paths().containsKey("/actions/task_create"));
        assertTrue(spec.paths().containsKey("/actions/task_close"));
    }

    @Test
    void manifestoExporterUsesDetailedFailureTaxonomy() {
        IntentGraph graph = new IntentGraph(
            List.of(new IntentGraphNode("n1", "", Map.of("title", "T"), ResolutionStatus.RESOLVED)),
            List.of(),
            Map.of()
        );

        ManifestoExportContext unresolvedActionContext = new ManifestoExportContext(
            new ai.manifesto.intentir.DefaultIntentIrLexicon(Map.of("todo", java.util.Set.of("createTask"))),
            new ai.manifesto.intentir.DefaultIntentIrResolver(Map.of("todo", java.util.Set.of("createTask"))),
            new ai.manifesto.intentir.DefaultIntentIrLowerer(),
            "todo",
            true
        );
        ManifestoBundle unresolvedBundle = new ManifestoTargetExporter().export(
            new ExportInput(graph, List.of(), null),
            unresolvedActionContext
        );
        assertEquals("failed", unresolvedBundle.invocationPlan().steps().get(0).lowering().status());
        assertEquals(
            LoweringFailureKind.UNRESOLVED_ACTION,
            unresolvedBundle.invocationPlan().steps().get(0).lowering().failure().kind()
        );

        ManifestoExportContext resolverFailureContext = new ManifestoExportContext(
            new ai.manifesto.intentir.DefaultIntentIrLexicon(Map.of("todo", java.util.Set.of("createTask"))),
            document -> { throw new IllegalStateException("resolver boom"); },
            new ai.manifesto.intentir.DefaultIntentIrLowerer(),
            "todo",
            false
        );
        ManifestoBundle resolverFailureBundle = new ManifestoTargetExporter().export(
            new ExportInput(new IntentGraph(
                List.of(new IntentGraphNode("n2", "createTask", Map.of("title", "T"), ResolutionStatus.RESOLVED)),
                List.of(),
                Map.of()
            ), List.of(), null),
            resolverFailureContext
        );
        assertEquals(
            LoweringFailureKind.RESOLVER_FAILURE,
            resolverFailureBundle.invocationPlan().steps().get(0).lowering().failure().kind()
        );
    }
}
