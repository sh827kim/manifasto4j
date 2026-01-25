package ai.manifesto.core.utils;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Result;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.schema.ComputedFieldDef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DagUtils 테스트
 *
 * DagUtils가 다양한 그래프 구조에서 올바르게 동작하는지 검증합니다:
 * - 선형 의존성 (a → b → c)
 * - 분기형 의존성 (a → [b, c])
 * - 순환 참조 감지
 * - 전이적 의존성 계산
 */
@DisplayName("DagUtils DAG 유틸리티 테스트")
class DagUtilsTest {

    // ============================================================
    // 테스트 헬퍼: ComputedFieldDef 생성
    // ============================================================

    /**
     * 지정된 이름과 의존성을 가진 ComputedFieldDef 생성
     */
    private ComputedFieldDef createField(String name, String... deps) {
        Set<String> dependencies = new HashSet<>(Arrays.asList(deps));
        return new ComputedFieldDef(name, Lit.of(42), dependencies);
    }

    /**
     * ComputedFieldDef Map 생성
     */
    private Map<String, ComputedFieldDef> createFieldMap(Object... items) {
        Map<String, ComputedFieldDef> map = new HashMap<>();
        for (int i = 0; i < items.length; i += 2) {
            String name = (String) items[i];
            @SuppressWarnings("unchecked")
            String[] deps = (String[]) items[i + 1];
            map.put(name, createField(name, deps));
        }
        return map;
    }

    // ============================================================
    // buildDependencyGraph() 테스트
    // ============================================================

    @Test
    @DisplayName("buildDependencyGraph: 단순 그래프 생성")
    void testBuildDependencyGraph_SimpleGraph() {
        // given
        Map<String, ComputedFieldDef> fields = createFieldMap(
                "a", new String[]{"b"},
                "b", new String[]{"c"},
                "c", new String[]{}
        );

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // then
        assertEquals(3, graph.getNodes().size());
        assertTrue(graph.getNodes().containsAll(Arrays.asList("a", "b", "c")));

        // 의존성 검증: a → [b], b → [c], c → []
        assertEquals(Collections.singletonList("b"), graph.getDepsFor("a"));
        assertEquals(Collections.singletonList("c"), graph.getDepsFor("b"));
        assertEquals(Collections.emptyList(), graph.getDepsFor("c"));
    }

    @Test
    @DisplayName("buildDependencyGraph: 비계산 필드 의존성 제외")
    void testBuildDependencyGraph_FilterNonComputedDeps() {
        // given
        Map<String, ComputedFieldDef> fields = createFieldMap(
                "computed.a", new String[]{"computed.b", "state.x", "data.y"},
                "computed.b", new String[]{}
        );

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // then - state.x, data.y는 computed 필드가 아니므로 제외됨
        assertEquals(
                Collections.singletonList("computed.b"),
                graph.getDepsFor("computed.a")
        );
    }

    @Test
    @DisplayName("buildDependencyGraph: 빈 필드 맵")
    void testBuildDependencyGraph_EmptyFields() {
        // given
        Map<String, ComputedFieldDef> fields = new HashMap<>();

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // then
        assertEquals(0, graph.getNodes().size());
        assertEquals(0, graph.getEdges().size());
    }

    @Test
    @DisplayName("buildDependencyGraph: 의존성이 없는 필드들")
    void testBuildDependencyGraph_NoDepencies() {
        // given
        Map<String, ComputedFieldDef> fields = createFieldMap(
                "a", new String[]{},
                "b", new String[]{},
                "c", new String[]{}
        );

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // then
        assertEquals(3, graph.getNodes().size());
        assertTrue(graph.getDepsFor("a").isEmpty());
        assertTrue(graph.getDepsFor("b").isEmpty());
        assertTrue(graph.getDepsFor("c").isEmpty());
    }

    // ============================================================
    // DependencyGraph 불변성 테스트
    // ============================================================

    @Test
    @DisplayName("DependencyGraph: getNodes() 방어적 복사 (외부 변경 방지)")
    void testDependencyGraph_DefensiveCopyNodes() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap("a", new String[]{}, "b", new String[]{})
        );

        // when - 반환받은 리스트 변경 시도
        List<String> nodes = graph.getNodes();
        assertThrows(UnsupportedOperationException.class, () -> nodes.add("c"));

        // then - 원본 그래프는 영향받지 않음
        assertEquals(2, graph.getNodes().size());
    }

    @Test
    @DisplayName("DependencyGraph: getEdges() 방어적 복사 (외부 변경 방지)")
    void testDependencyGraph_DefensiveCopyEdges() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap("a", new String[]{"b"}, "b", new String[]{})
        );

        // when - 반환받은 맵 변경 시도
        Map<String, List<String>> edges = graph.getEdges();
        assertThrows(UnsupportedOperationException.class, () -> edges.put("c", new ArrayList<>()));

        // then - 원본 그래프는 영향받지 않음
        assertEquals(2, graph.getEdges().size());
    }

    // ============================================================
    // topologicalSort() 테스트
    // ============================================================

    @Test
    @DisplayName("topologicalSort: 선형 DAG (a → b → c)")
    void testTopologicalSort_LinearDAG() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{"b"},
                        "b", new String[]{"c"},
                        "c", new String[]{}
                )
        );

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then - 성공 케이스
        assertTrue(result.isOk());
        List<String> sorted = result.unwrap();
        assertEquals(3, sorted.size());

        // 의존성 순서 검증: c가 b보다 먼저, b가 a보다 먼저
        assertTrue(sorted.indexOf("c") < sorted.indexOf("b"));
        assertTrue(sorted.indexOf("b") < sorted.indexOf("a"));
    }

    @Test
    @DisplayName("topologicalSort: 분기형 DAG (다중 의존성)")
    void testTopologicalSort_BranchingDAG() {
        // given
        // d가 a, b 의존 / e가 b, c 의존 / f가 d, e 의존
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{},
                        "b", new String[]{},
                        "c", new String[]{},
                        "d", new String[]{"a", "b"},
                        "e", new String[]{"b", "c"},
                        "f", new String[]{"d", "e"}
                )
        );

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then
        assertTrue(result.isOk());
        List<String> sorted = result.unwrap();
        assertEquals(6, sorted.size());

        // 의존성 순서 검증
        assertTrue(sorted.indexOf("a") < sorted.indexOf("d"));
        assertTrue(sorted.indexOf("b") < sorted.indexOf("d"));
        assertTrue(sorted.indexOf("b") < sorted.indexOf("e"));
        assertTrue(sorted.indexOf("c") < sorted.indexOf("e"));
        assertTrue(sorted.indexOf("d") < sorted.indexOf("f"));
        assertTrue(sorted.indexOf("e") < sorted.indexOf("f"));
    }

    @Test
    @DisplayName("topologicalSort: 순환 참조 감지 (a → b → c → a)")
    void testTopologicalSort_CircularDependency_Simple() {
        // given - 순환: a → b → c → a
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("a", createField("a", "b"));
        fields.put("b", createField("b", "c"));
        fields.put("c", createField("c", "a"));  // 순환!

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then - 실패 케이스
        assertTrue(result.isErr());

        if (result instanceof Result.Err<?, ?> err) {
            ErrorValue error = (ErrorValue) err.error();
            assertEquals("V-002", error.getCode());
            assertTrue(error.getMessage().contains("Cyclic dependency"));
        }
    }

    @Test
    @DisplayName("topologicalSort: 순환 참조 감지 (자기 자신 의존)")
    void testTopologicalSort_SelfDependency() {
        // given - 자기 자신을 의존: a → [a]
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("a", createField("a", "a"));  // 자기 자신 의존!

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then
        assertTrue(result.isErr());

        if (result instanceof Result.Err<?, ?> err) {
            ErrorValue error = (ErrorValue) err.error();
            assertEquals("V-002", error.getCode());
        }
    }

    @Test
    @DisplayName("topologicalSort: 빈 그래프")
    void testTopologicalSort_EmptyGraph() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(new HashMap<>());

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then
        assertTrue(result.isOk());
        assertTrue(result.unwrap().isEmpty());
    }

    @Test
    @DisplayName("topologicalSort: 단일 노드 (의존성 없음)")
    void testTopologicalSort_SingleNode() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap("a", new String[]{})
        );

        // when
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then
        assertTrue(result.isOk());
        assertEquals(Collections.singletonList("a"), result.unwrap());
    }

    // ============================================================
    // detectCycles() 테스트
    // ============================================================

    @Test
    @DisplayName("detectCycles: 순환 없음 (DAG)")
    void testDetectCycles_NoCycles() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{"b"},
                        "b", new String[]{"c"},
                        "c", new String[]{}
                )
        );

        // when
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        // then
        assertTrue(cycles.isEmpty());
    }

    @Test
    @DisplayName("detectCycles: 간단한 순환 (a → b → a)")
    void testDetectCycles_SimpleCycle() {
        // given - 순환: a → b → a
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("a", createField("a", "b"));
        fields.put("b", createField("b", "a"));

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // when
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        // then
        assertFalse(cycles.isEmpty());
        assertEquals(1, cycles.size());

        // 순환 경로 검증: a → b → a 또는 b → a → b
        List<String> cycle = cycles.get(0);
        assertTrue(cycle.size() >= 2, "순환 경로는 최소 2개 이상의 노드를 포함해야 함");
        assertEquals(cycle.get(0), cycle.get(cycle.size() - 1),
                "순환 경로는 같은 노드로 시작과 종료되어야 함");
    }

    @Test
    @DisplayName("detectCycles: 3-사이클 (a → b → c → a)")
    void testDetectCycles_ThreeCycle() {
        // given - 순환: a → b → c → a
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("a", createField("a", "b"));
        fields.put("b", createField("b", "c"));
        fields.put("c", createField("c", "a"));

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // when
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        // then
        assertFalse(cycles.isEmpty());
        List<String> cycle = cycles.get(0);

        // 순환 노드 검증
        assertTrue(cycle.contains("a"));
        assertTrue(cycle.contains("b"));
        assertTrue(cycle.contains("c"));
    }

    @Test
    @DisplayName("detectCycles: 자기 자신 순환")
    void testDetectCycles_SelfCycle() {
        // given
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("a", createField("a", "a"));

        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);

        // when
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        // then
        assertFalse(cycles.isEmpty());
        List<String> cycle = cycles.get(0);
        assertEquals(2, cycle.size());
        assertEquals("a", cycle.get(0));
        assertEquals("a", cycle.get(1));
    }

    @Test
    @DisplayName("detectCycles: 빈 그래프")
    void testDetectCycles_EmptyGraph() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(new HashMap<>());

        // when
        List<List<String>> cycles = DagUtils.detectCycles(graph);

        // then
        assertTrue(cycles.isEmpty());
    }

    // ============================================================
    // getTransitiveDeps() 테스트
    // ============================================================

    @Test
    @DisplayName("getTransitiveDeps: 직접 의존성")
    void testGetTransitiveDeps_DirectDependencies() {
        // given
        // a → [b, c], b → [d], c → [], d → []
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{"b", "c"},
                        "b", new String[]{"d"},
                        "c", new String[]{},
                        "d", new String[]{}
                )
        );

        // when
        Set<String> transitiveDeps = DagUtils.getTransitiveDeps(graph, "a");

        // then - a의 전이적 의존성: b, c, d
        assertEquals(3, transitiveDeps.size());
        assertTrue(transitiveDeps.containsAll(Arrays.asList("b", "c", "d")));
    }

    @Test
    @DisplayName("getTransitiveDeps: 전이적 의존성 (긴 체인)")
    void testGetTransitiveDeps_LongChain() {
        // given
        // a → b → c → d → e
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{"b"},
                        "b", new String[]{"c"},
                        "c", new String[]{"d"},
                        "d", new String[]{"e"},
                        "e", new String[]{}
                )
        );

        // when
        Set<String> transitiveDeps = DagUtils.getTransitiveDeps(graph, "a");

        // then - a의 모든 전이적 의존성
        assertEquals(4, transitiveDeps.size());
        assertTrue(transitiveDeps.containsAll(Arrays.asList("b", "c", "d", "e")));
    }

    @Test
    @DisplayName("getTransitiveDeps: 의존성 없음")
    void testGetTransitiveDeps_NoDependencies() {
        // given
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{},
                        "b", new String[]{},
                        "c", new String[]{}
                )
        );

        // when
        Set<String> transitiveDeps = DagUtils.getTransitiveDeps(graph, "a");

        // then
        assertTrue(transitiveDeps.isEmpty());
    }

    @Test
    @DisplayName("getTransitiveDeps: 복잡한 DAG (다중 경로)")
    void testGetTransitiveDeps_ComplexDAG() {
        // given
        // f → [d, e]
        // d → [a, b]
        // e → [b, c]
        // a, b, c → []
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "f", new String[]{"d", "e"},
                        "d", new String[]{"a", "b"},
                        "e", new String[]{"b", "c"},
                        "a", new String[]{},
                        "b", new String[]{},
                        "c", new String[]{}
                )
        );

        // when
        Set<String> transitiveDeps = DagUtils.getTransitiveDeps(graph, "f");

        // then - f의 모든 전이적 의존성: d, e, a, b, c
        assertEquals(5, transitiveDeps.size());
        assertTrue(transitiveDeps.containsAll(Arrays.asList("d", "e", "a", "b", "c")));
    }

    @Test
    @DisplayName("getTransitiveDeps: 중복 제거")
    void testGetTransitiveDeps_DeduplicationWorksCorrectly() {
        // given
        // a → [b, c]
        // b → [d]
        // c → [d]  (d는 b와 c 둘 다에서 접근 가능)
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(
                createFieldMap(
                        "a", new String[]{"b", "c"},
                        "b", new String[]{"d"},
                        "c", new String[]{"d"},
                        "d", new String[]{}
                )
        );

        // when
        Set<String> transitiveDeps = DagUtils.getTransitiveDeps(graph, "a");

        // then - d는 한 번만 포함
        assertEquals(3, transitiveDeps.size());
        assertTrue(transitiveDeps.containsAll(Arrays.asList("b", "c", "d")));
    }

    // ============================================================
    // 통합 테스트
    // ============================================================

    @Test
    @DisplayName("통합: buildDependencyGraph + topologicalSort (정상 케이스)")
    void testIntegration_BuildAndSort_Success() {
        // given
        Map<String, ComputedFieldDef> fields = createFieldMap(
                "total", new String[]{"subtotal", "tax"},
                "subtotal", new String[]{"items"},
                "tax", new String[]{"subtotal"},
                "items", new String[]{}
        );

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);
        Result<List<String>, ErrorValue> result = DagUtils.topologicalSort(graph);

        // then
        assertTrue(result.isOk());
        List<String> sorted = result.unwrap();
        assertEquals(4, sorted.size());

        // 의존성 순서 확인
        assertTrue(sorted.indexOf("items") < sorted.indexOf("subtotal"));
        assertTrue(sorted.indexOf("subtotal") < sorted.indexOf("tax"));
        assertTrue(sorted.indexOf("subtotal") < sorted.indexOf("total"));
        assertTrue(sorted.indexOf("tax") < sorted.indexOf("total"));
    }

    @Test
    @DisplayName("통합: buildDependencyGraph + detectCycles (순환 검증)")
    void testIntegration_BuildAndDetect_Circular() {
        // given - 문제: tax가 subtotal을 의존, subtotal이 tax를 의존
        Map<String, ComputedFieldDef> fields = new HashMap<>();
        fields.put("total", createField("total", "subtotal", "tax"));
        fields.put("subtotal", createField("subtotal", "tax"));  // 문제!
        fields.put("tax", createField("tax", "subtotal"));       // 순환!
        fields.put("items", createField("items"));

        // when
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(fields);
        List<List<String>> cycles = DagUtils.detectCycles(graph);
        Result<List<String>, ErrorValue> sortResult = DagUtils.topologicalSort(graph);

        // then
        assertFalse(cycles.isEmpty(), "순환이 감지되어야 함");
        assertTrue(sortResult.isErr(), "위상 정렬이 실패해야 함");
    }

    @Test
    @DisplayName("일관성 검증: topologicalSort 결과는 항상 결정론적")
    void testDeterminism_TopologicalSort() {
        // given
        Map<String, ComputedFieldDef> fields = createFieldMap(
                "a", new String[]{"b", "c"},
                "b", new String[]{"d"},
                "c", new String[]{"d"},
                "d", new String[]{}
        );

        DagUtils.DependencyGraph graph1 = DagUtils.buildDependencyGraph(fields);
        DagUtils.DependencyGraph graph2 = DagUtils.buildDependencyGraph(fields);

        // when - 두 번 실행
        Result<List<String>, ErrorValue> result1 = DagUtils.topologicalSort(graph1);
        Result<List<String>, ErrorValue> result2 = DagUtils.topologicalSort(graph2);

        // then - 같은 입력이므로 같은 결과 (순서도 동일)
        assertTrue(result1.isOk());
        assertTrue(result2.isOk());
        assertEquals(result1.unwrap(), result2.unwrap(),
                "같은 입력에 대해 topologicalSort는 항상 같은 결과를 반환해야 함");
    }
}
