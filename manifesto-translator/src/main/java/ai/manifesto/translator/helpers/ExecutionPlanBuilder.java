package ai.manifesto.translator.helpers;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.ExecutionPlan;
import ai.manifesto.translator.core.ExecutionStep;
import ai.manifesto.translator.core.IntentGraph;
import ai.manifesto.translator.core.IntentGraphNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * KR: intent graph를 단순 위상 순서 실행 플랜으로 변환합니다.
 * EN: Builds a simple topological-order execution plan from intent graph.
 */
public final class ExecutionPlanBuilder {
    public ExecutionPlan build(IntentGraph graph) {
        if (graph == null || graph.nodes() == null || graph.nodes().isEmpty()) {
            return new ExecutionPlan(List.of(), List.of());
        }
        List<IntentGraphNode> nodes = new ArrayList<>(graph.nodes());
        nodes.sort(Comparator.comparing(IntentGraphNode::nodeId));

        List<ExecutionStep> steps = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            IntentGraphNode node = nodes.get(i);
            steps.add(new ExecutionStep("step-" + i, node.action(), node.input(), i));
        }
        List<DependencyEdge> deps = graph.edges() == null ? List.of() : List.copyOf(graph.edges());
        return new ExecutionPlan(List.copyOf(steps), deps);
    }
}
