package ai.manifesto.translator.invariants;

import ai.manifesto.translator.core.DependencyEdge;
import ai.manifesto.translator.core.IntentGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * KR: dependency graph 순환 여부를 검사합니다.
 * EN: Checks cycle existence in dependency graph.
 */
public final class CausalIntegrityChecker {
    public boolean hasCycle(IntentGraph graph) {
        if (graph == null || graph.edges() == null || graph.edges().isEmpty()) {
            return false;
        }
        Map<String, Set<String>> adj = new HashMap<>();
        for (DependencyEdge e : graph.edges()) {
            adj.computeIfAbsent(e.fromNodeId(), k -> new HashSet<>()).add(e.toNodeId());
        }
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String node : adj.keySet()) {
            if (dfs(node, adj, visiting, visited)) {
                return true;
            }
        }
        return false;
    }

    private boolean dfs(String node, Map<String, Set<String>> adj, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) {
            return false;
        }
        if (!visiting.add(node)) {
            return true;
        }
        for (String next : adj.getOrDefault(node, Set.of())) {
            if (dfs(next, adj, visiting, visited)) {
                return true;
            }
        }
        visiting.remove(node);
        visited.add(node);
        return false;
    }
}
