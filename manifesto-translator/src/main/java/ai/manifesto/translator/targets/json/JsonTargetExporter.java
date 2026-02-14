package ai.manifesto.translator.targets.json;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.targets.ExportInput;
import ai.manifesto.translator.targets.TargetExporter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * KR: IntentGraph를 JSON 구조로 내보내는 exporter입니다.
 * EN: Exporter that converts IntentGraph into JSON-friendly output structures.
 */
public final class JsonTargetExporter implements TargetExporter<JsonOutput, Void> {
    @Override
    public String id() {
        return "json";
    }

    @Override
    public JsonOutput export(ExportInput input, Void context) {
        IntentGraph graph = input.graph();
        if (graph == null || graph.nodes() == null) {
            return new JsonOutput(List.of(), List.of());
        }

        List<DependencyEdge> edges = graph.edges() == null ? List.of() : List.copyOf(graph.edges());
        Map<String, List<String>> incomingDependencies = edges.stream()
            .collect(Collectors.groupingBy(DependencyEdge::toNodeId,
                Collectors.mapping(DependencyEdge::fromNodeId, Collectors.toList())));

        List<JsonNodeExport> nodes = graph.nodes().stream()
            .sorted(Comparator.comparing(IntentGraphNode::nodeId))
            .map(node -> new JsonNodeExport(
                node.nodeId(),
                node.action(),
                node.resolutionStatus() == null ? "unknown" : node.resolutionStatus().name().toLowerCase(),
                incomingDependencies.getOrDefault(node.nodeId(), List.of()).stream().sorted().toList()
            ))
            .toList();

        List<JsonEdgeExport> exportedEdges = edges.stream()
            .sorted(Comparator.comparing(DependencyEdge::fromNodeId).thenComparing(DependencyEdge::toNodeId))
            .map(edge -> new JsonEdgeExport(edge.fromNodeId(), edge.toNodeId()))
            .toList();

        return new JsonOutput(nodes, exportedEdges);
    }
}
