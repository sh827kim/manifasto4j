package ai.manifesto.translator.targets.openapi;

import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;
import ai.manifesto.translator.targets.ExportInput;
import ai.manifesto.translator.targets.TargetExporter;

import java.util.Map;
import java.util.TreeMap;

/**
 * KR: IntentGraph를 최소 OpenAPI 문서 형태로 변환하는 exporter입니다.
 * EN: Exporter that converts IntentGraph into a minimal OpenAPI document.
 */
public final class OpenApiTargetExporter implements TargetExporter<OpenApiSpec, Void> {
    @Override
    public String id() {
        return "openapi";
    }

    @Override
    public OpenApiSpec export(ExportInput input, Void context) {
        IntentGraph graph = input.graph();
        Map<String, OpenApiSpec.PathItem> paths = new TreeMap<>();

        if (graph != null && graph.nodes() != null) {
            for (IntentGraphNode node : graph.nodes()) {
                String action = node.action() == null || node.action().isBlank() ? "unknown" : node.action();
                String sanitizedAction = sanitizeAction(action);
                String path = "/actions/" + sanitizedAction;
                paths.putIfAbsent(path, new OpenApiSpec.PathItem(new OpenApiSpec.Operation(
                    "invoke_" + sanitizedAction,
                    "Invoke action " + action
                )));
            }
        }

        return new OpenApiSpec(
            "3.0.3",
            new OpenApiSpec.Info("Manifesto Translator Export", "0.1.0"),
            Map.copyOf(paths)
        );
    }

    private String sanitizeAction(String action) {
        return action
            .replace('.', '_')
            .replace('-', '_')
            .replace(' ', '_')
            .toLowerCase();
    }
}
