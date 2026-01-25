package ai.manifesto.core.utils;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Result;
import ai.manifesto.core.schema.ComputedFieldDef;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DagUtils - Computed 필드의 의존성 그래프(DAG) 유틸리티
 *
 * DAG (Directed Acyclic Graph)는 방향성이 있는 비순환 그래프입니다.
 * Computed 필드들 간의 의존 관계를 표현하고, 순환 참조를 감지하며,
 * 위상 정렬을 통해 계산 순서를 결정합니다.
 *
 * 핵심 원칙:
 * - 결정론적: 같은 입력 → 같은 출력
 * - 불변성: 모든 그래프 구조는 불변
 * - 에러 처리: 예외 대신 Result<T, E> 사용
 *
 * 시간복잡도:
 * - buildDependencyGraph(): O(n)
 * - topologicalSort(): O(V + E) (Kahn's Algorithm)
 * - detectCycles(): O(V + E) (DFS)
 * - getTransitiveDeps(): O(V + E) (BFS)
 */
public class DagUtils {

    /**
     * 인스턴스 생성 불가 (정적 메서드만 제공)
     */
    private DagUtils() {
        // Utility class
    }

    /**
     * DependencyGraph - Computed 필드들 간의 의존 관계 그래프
     *
     * 불변 구조:
     * - nodes: 모든 computed 필드 이름 목록 (변경 불가)
     * - edges: 각 필드가 의존하는 다른 필드들의 맵 (변경 불가)
     */
    public static final class DependencyGraph {
        private final List<String> nodes;                      // 모든 computed 필드 이름
        private final Map<String, List<String>> edges;         // fieldName → dependencies

        /**
         * 의존성 그래프 생성
         *
         * @param nodes   모든 노드(computed 필드)의 이름 목록
         * @param edges   각 노드의 의존성 맵
         */
        public DependencyGraph(List<String> nodes, Map<String, List<String>> edges) {
            this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
            // 방어적 복사: edges의 모든 값도 불변으로 변환
            Map<String, List<String>> defensiveCopy = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                defensiveCopy.put(entry.getKey(),
                    Collections.unmodifiableList(new ArrayList<>(entry.getValue())));
            }
            this.edges = Collections.unmodifiableMap(defensiveCopy);
        }

        /**
         * 모든 노드 반환 (방어적 복사)
         */
        public List<String> getNodes() {
            return Collections.unmodifiableList(new ArrayList<>(nodes));
        }

        /**
         * 엣지 맵 반환 (방어적 복사)
         */
        public Map<String, List<String>> getEdges() {
            Map<String, List<String>> copy = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
                copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return Collections.unmodifiableMap(copy);
        }

        /**
         * 특정 노드의 직접 의존성 반환
         *
         * @param node 노드 이름
         * @return 의존 필드 리스트 (없으면 빈 리스트)
         */
        public List<String> getDepsFor(String node) {
            List<String> deps = edges.get(node);
            return deps != null ? new ArrayList<>(deps) : Collections.emptyList();
        }
    }

    /**
     * Computed 필드 정의에서 의존성 그래프를 구축
     *
     * 로직:
     * 1. 모든 computed 필드 이름을 nodes로 수집
     * 2. 각 필드의 의존성을 edges Map에 추가
     * 3. 중요: computed-to-computed 의존성만 추적
     *    (state 의존성이나 기타 의존성은 제외)
     *
     * 예시:
     * - computed.a → [state.x, computed.b]
     *   → 그래프에는 computed.b만 추가됨
     *
     * @param computedFields DomainSchema의 computedFields Map
     * @return DependencyGraph (불변)
     */
    public static DependencyGraph buildDependencyGraph(
            Map<String, ComputedFieldDef> computedFields) {

        // 모든 computed 필드 이름 수집
        List<String> nodes = new ArrayList<>(computedFields.keySet());

        // 각 필드의 의존성 처리
        Map<String, List<String>> edges = new HashMap<>();
        Set<String> computedFieldNames = computedFields.keySet();

        for (Map.Entry<String, ComputedFieldDef> entry : computedFields.entrySet()) {
            String fieldName = entry.getKey();
            ComputedFieldDef fieldDef = entry.getValue();

            // 이 필드의 모든 의존성 중 computed-to-computed만 필터링
            List<String> computedDeps = fieldDef.getDependencies()
                    .stream()
                    .filter(computedFieldNames::contains)
                    .collect(Collectors.toList());

            edges.put(fieldName, computedDeps);
        }

        return new DependencyGraph(nodes, edges);
    }

    /**
     * Kahn's Algorithm을 사용한 위상 정렬
     *
     * 위상 정렬(Topological Sort)은 DAG의 모든 노드를 선형 순서로 배열하되,
     * 각 노드가 자신의 의존성 노드들보다 뒤에 오도록 합니다.
     *
     * 알고리즘:
     * 1. In-degree 계산: 각 노드로 들어오는 엣지의 수
     * 2. 큐 초기화: in-degree가 0인 노드들 추가
     * 3. BFS 반복:
     *    - 큐에서 노드 꺼내 결과에 추가
     *    - 해당 노드를 의존하는 다른 노드들의 in-degree 감소
     *    - in-degree가 0이 되면 큐에 추가
     * 4. 순환 감지: 정렬된 노드 수 ≠ 전체 노드 수 → 순환 존재
     *
     * 시간복잡도: O(V + E)
     *
     * @param graph 의존성 그래프
     * @return Result<List<String>, ErrorValue>
     *         - Ok: 위상 정렬된 필드 순서
     *         - Err: 순환 참조 감지 (에러 코드: V-002)
     */
    public static Result<List<String>, ErrorValue> topologicalSort(
            DependencyGraph graph) {

        List<String> nodes = graph.getNodes();
        Map<String, List<String>> edges = graph.getEdges();

        // 초기화: in-degree 맵과 역방향 엣지(adjacency) 맵
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjacency = new HashMap<>();

        for (String node : nodes) {
            inDegree.put(node, 0);
            adjacency.put(node, new ArrayList<>());
        }

        // 역방향 엣지 구축: 누가 나를 의존하는가?
        // edges: a → [b, c]는 a가 b, c를 의존한다는 뜻
        // 역방향: b → [a], c → [a]는 b, c가 a에게 의존받는다는 뜻
        for (Map.Entry<String, List<String>> entry : edges.entrySet()) {
            String node = entry.getKey();
            List<String> deps = entry.getValue();

            for (String dep : deps) {
                if (adjacency.containsKey(dep)) {
                    adjacency.get(dep).add(node);  // dep을 node가 의존
                    inDegree.put(node, inDegree.get(node) + 1);
                }
            }
        }

        // 큐 초기화: in-degree가 0인 노드들 (의존성 없음)
        Queue<String> queue = new LinkedList<>();
        for (String node : nodes) {
            if (inDegree.get(node) == 0) {
                queue.add(node);
            }
        }

        // BFS를 통한 위상 정렬
        List<String> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            sorted.add(node);

            // 이 노드를 의존하는 다른 노드들의 in-degree 감소
            for (String dependent : adjacency.get(node)) {
                int newDegree = inDegree.get(dependent) - 1;
                inDegree.put(dependent, newDegree);

                if (newDegree == 0) {
                    queue.add(dependent);
                }
            }
        }

        // 순환 참조 감지: 정렬된 노드 수가 전체 노드 수와 다르면 순환 존재
        if (sorted.size() != nodes.size()) {
            List<String> remaining = nodes.stream()
                    .filter(n -> !sorted.contains(n))
                    .collect(Collectors.toList());

            // 에러 생성: V-002 (순환 의존성)
            ErrorValue error = ErrorValue.create(
                    "V-002",
                    "Cyclic dependency detected in computed fields: " + remaining,
                    null,
                    remaining.isEmpty() ? null : remaining.get(0),
                    System.currentTimeMillis()
            );
            return Result.err(error);
        }

        return Result.ok(sorted);
    }

    /**
     * DFS를 사용한 순환 참조 감지
     *
     * 알고리즘 (White-Gray-Black):
     * - White: 아직 방문하지 않은 노드
     * - Gray: 현재 탐색 경로에 있는 노드 (recursionStack에 있음)
     * - Black: 탐색 완료된 노드 (visited에 있고 recursionStack에 없음)
     *
     * DFS 재귀:
     * 1. 현재 노드 방문 시작 → recursionStack에 추가
     * 2. 의존 노드 탐색:
     *    - 미방문 → 재귀 호출
     *    - Gray 상태(recursionStack에 있음) → 순환 발견! (Back Edge)
     * 3. 탐색 완료 → recursionStack에서 제거, visited에 추가 (Black)
     *
     * 반환:
     * - 모든 순환 경로 리스트
     * - 예: [["computed.a", "computed.b", "computed.a"]]
     *   (a → b → a의 순환)
     *
     * @param graph 의존성 그래프
     * @return List<List<String>> 모든 순환 경로 (없으면 빈 리스트)
     */
    public static List<List<String>> detectCycles(DependencyGraph graph) {
        List<String> nodes = graph.getNodes();
        Map<String, List<String>> edges = graph.getEdges();

        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<List<String>> cycles = new ArrayList<>();

        /**
         * DFS 재귀 탐색
         *
         * @param node 현재 노드
         * @param path 현재까지의 경로 (추적용)
         * @return 순환이 발견되었는지 여부
         */
        class DFS {
            boolean visit(String node, List<String> path) {
                visited.add(node);
                recursionStack.add(node);

                List<String> deps = edges.get(node);
                if (deps != null) {
                    for (String dep : deps) {
                        if (!visited.contains(dep)) {
                            // 미방문 노드: 재귀 탐색
                            List<String> newPath = new ArrayList<>(path);
                            newPath.add(dep);
                            if (visit(dep, newPath)) {
                                return true;
                            }
                        } else if (recursionStack.contains(dep)) {
                            // Gray 노드: 순환 발견!
                            // 순환의 시작 위치 찾기
                            int cycleStart = path.indexOf(dep);
                            if (cycleStart != -1) {
                                // path에 dep이 있으면 그 위치부터 시작
                                List<String> cycle = new ArrayList<>(
                                        path.subList(cycleStart, path.size())
                                );
                                cycle.add(dep);  // 순환을 닫음
                                cycles.add(cycle);
                            } else {
                                // path에 없으면 현재 경로 전체 + dep
                                List<String> cycle = new ArrayList<>(path);
                                cycle.add(dep);
                                cycles.add(cycle);
                            }
                        }
                    }
                }

                recursionStack.remove(node);
                return false;
            }
        }

        DFS dfs = new DFS();

        // 모든 노드에서 DFS 시작
        for (String node : nodes) {
            if (!visited.contains(node)) {
                dfs.visit(node, new ArrayList<>(Arrays.asList(node)));
            }
        }

        return cycles;
    }

    /**
     * 특정 필드의 전이적 의존성 계산 (직접 + 간접)
     *
     * 전이적 의존성은 다음을 포함합니다:
     * - 직접 의존성: a → [b, c]에서 b, c
     * - 간접 의존성: b → [d]일 때 d도 포함
     *
     * 알고리즘 (BFS):
     * 1. 해당 필드의 직접 의존성을 큐에 추가
     * 2. BFS 순회:
     *    - 큐에서 dep 꺼내기
     *    - 결과 Set에 추가 (중복 제거)
     *    - dep의 의존성들도 큐에 추가
     * 3. 최종 Set 반환
     *
     * 예시:
     * ```
     * computed.c → [computed.b]
     * computed.b → [computed.a]
     * getTransitiveDeps(graph, "c") → {computed.b, computed.a}
     * ```
     *
     * @param graph 의존성 그래프
     * @param fieldName 필드 이름
     * @return Set<String> 모든 전이적 의존성 (빈 Set이 가능)
     */
    public static Set<String> getTransitiveDeps(DependencyGraph graph, String fieldName) {
        Map<String, List<String>> edges = graph.getEdges();
        Set<String> deps = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        // 직접 의존성 초기화
        List<String> directDeps = edges.get(fieldName);
        if (directDeps != null) {
            queue.addAll(directDeps);
        }

        // BFS를 통한 전이적 의존성 수집
        while (!queue.isEmpty()) {
            String dep = queue.poll();

            // 중복 제거: 이미 처리한 의존성은 스킵
            if (!deps.contains(dep)) {
                deps.add(dep);

                // 이 의존성의 의존성들도 큐에 추가
                List<String> transitiveDeps = edges.get(dep);
                if (transitiveDeps != null) {
                    for (String transitiveDep : transitiveDeps) {
                        if (!deps.contains(transitiveDep)) {
                            queue.add(transitiveDep);
                        }
                    }
                }
            }
        }

        return deps;
    }
}
