package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.arithmetic.Add;
import ai.manifesto.core.expr.arithmetic.Mul;
import ai.manifesto.core.expr.comparison.Gt;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Compute.java의 ComputedEvaluator 통합 테스트
 *
 * Phase 5C-Step 1: Compute.java에 ComputedEvaluator를 통합하여
 * Computed 필드가 availability 체크 전에 평가되는지 검증
 */
class ComputeComputedFieldTest {

    private DomainSchema schema;
    private Snapshot snapshot;
    private Intent intent;

    @BeforeEach
    void setUp() {
        // 기본 스키마 설정: computed.total, computed.isValid 포함
        Map<String, ComputedFieldDef> computedFields = new HashMap<>();

        // computed.total = 100
        computedFields.put("computed.total", ComputedFieldDef.simple("computed.total", new Lit(100)));

        // computed.isValid = total > 50
        computedFields.put("computed.isValid", new ComputedFieldDef.Builder(
            "computed.isValid",
            new Gt(new Get("computed.total"), new Lit(50))
        )
            .addDependency("computed.total")
            .build());

        // action.test: available 조건에서 computed 필드 참조
        // available = isValid (computed.isValid를 참조)
        ActionSpec testAction = new ActionSpec.Builder("test")
            .available(new Get("computed.isValid"))
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema.Builder schemaBuilder = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash");
        for (ComputedFieldDef field : computedFields.values()) {
            schemaBuilder.addComputedField(field);
        }
        schema = schemaBuilder.addAction(testAction).build();

        // 기본 스냅샷
        snapshot = Snapshot.builder()
            .data(new HashMap<>())
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        // 기본 intent
        intent = new Intent("test", new HashMap<>(), "test-intent-1");
    }

    /**
     * Test 1: Computed 필드가 availability 체크 전에 평가되는지 확인
     *
     * 시나리오:
     * 1. 초기 snapshot의 computed는 비어있음
     * 2. Compute.computeSync()에서 Step 1이 실행되어 computed.total과 computed.isValid 계산
     * 3. Step 5에서 availability 체크할 때 computed 필드를 참조 가능해야 함
     */
    @Test
    void testComputeSync_ComputedFieldsEvaluatedBeforeAvailabilityCheck() throws Exception {
        // given: snapshot의 computed가 비어있는 상태
        assertTrue(snapshot.getComputed().isEmpty(), "Initial computed should be empty");

        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            schema,
            snapshot,
            intent,
            5
        );

        // then: 계산이 완료되고 computed 필드가 평가됨
        assertNotNull(result, "Result should not be null");
        assertEquals(ComputeStatus.HALTED, result.getStatus(), "Status should be HALTED");

        Snapshot resultSnapshot = result.getSnapshot();
        assertNotNull(resultSnapshot.getComputed(), "Computed should not be null");
        assertEquals(100, resultSnapshot.getComputed().get("computed.total"), "computed.total should be 100");
        assertTrue((Boolean) resultSnapshot.getComputed().get("computed.isValid"), "computed.isValid should be true");
    }

    /**
     * Test 2: Computed 필드가 availability에서 성공적으로 참조됨
     *
     * 시나리오:
     * 1. computed.isValid = true인 action
     * 2. Compute.computeSync()에서 computed 필드 평가 후 availability 체크
     * 3. availability 조건이 true이므로 계속 진행
     */
    @Test
    void testComputeSync_ComputedFieldReferencedInAvailability() throws Exception {
        // given: available = computed.isValid (true)
        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            schema,
            snapshot,
            intent,
            5
        );

        // then: availability 체크 성공, COMPLETE 반환
        assertEquals(ComputeStatus.HALTED, result.getStatus());
    }

    /**
     * Test 3: Computed 필드 평가 실패 (순환 참조) 시 에러 반환
     *
     * 시나리오:
     * 1. computed.a는 computed.b를 참조
     * 2. computed.b는 computed.a를 참조 (순환)
     * 3. Step 1에서 순환 참조 감지, V-002 에러
     * 4. ComputeResult.error() 반환
     */
    @Test
    void testComputeSync_CircularComputedDependency_ReturnsError() throws Exception {
        // given: 순환 참조가 있는 computed 필드
        Map<String, ComputedFieldDef> circularComputed = new HashMap<>();

        // 순환 참조: a -> b -> a
        Get exprA = new Get("computed.b");
        Get exprB = new Get("computed.a");

        circularComputed.put("computed.a", new ComputedFieldDef.Builder("computed.a", exprA)
            .addDependency("computed.b")
            .build());
        circularComputed.put("computed.b", new ComputedFieldDef.Builder("computed.b", exprB)
            .addDependency("computed.a")
            .build());

        ActionSpec dummyAction = new ActionSpec.Builder("test")
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema.Builder circularBuilder = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash");
        for (ComputedFieldDef field : circularComputed.values()) {
            circularBuilder.addComputedField(field);
        }
        DomainSchema circularSchema = circularBuilder.addAction(dummyAction).build();

        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            circularSchema,
            snapshot,
            intent,
            5
        );

        // then: 순환 참조 에러 반환
        assertEquals(ComputeStatus.ERROR, result.getStatus(),
            "Status should be ERROR for circular dependency");
    }

    /**
     * Test 4: 결정론성 검증 (Determinism Test)
     *
     * 같은 입력으로 두 번 실행하면 같은 결과를 반환해야 함
     */
    @Test
    void testComputeSync_DeterministicWithComputedFields() throws Exception {
        // given: 같은 snapshot과 intent

        // when: 첫 번째 실행
        ComputeResult result1 = Compute.computeSync(schema, snapshot, intent, 5);

        // when: 두 번째 실행 (같은 입력)
        ComputeResult result2 = Compute.computeSync(schema, snapshot, intent, 5);

        // then: 같은 결과 반환
        assertEquals(result1.getStatus(), result2.getStatus());
        assertEquals(
            result1.getSnapshot().getComputed().get("computed.total"),
            result2.getSnapshot().getComputed().get("computed.total"),
            "computed.total should be identical"
        );
        assertEquals(
            result1.getSnapshot().getComputed().get("computed.isValid"),
            result2.getSnapshot().getComputed().get("computed.isValid"),
            "computed.isValid should be identical"
        );
    }

    /**
     * Test 5: Computed 필드가 원본 Snapshot을 변경하지 않음
     *
     * 시나리오:
     * 1. 원본 snapshot의 computed는 비어있음
     * 2. Compute.computeSync() 실행
     * 3. 원본 snapshot은 여전히 비어있어야 함 (불변성)
     */
    @Test
    void testComputeSync_OriginalSnapshotNotMutated() throws Exception {
        // given: 원본 snapshot의 computed가 비어있음
        int originalComputedSize = snapshot.getComputed().size();

        // when: computeSync 실행
        Compute.computeSync(schema, snapshot, intent, 5);

        // then: 원본 snapshot 미변경
        assertEquals(originalComputedSize, snapshot.getComputed().size(),
            "Original snapshot's computed should not be mutated");
    }

    /**
     * Test 6: Input Snapshot에 computed 필드가 포함됨
     *
     * 시나리오:
     * 1. Step 1에서 computed 평가
     * 2. Step 6에서 currentSnapshot.withInput()으로 input 추가
     * 3. 결과 snapshot에는 computed와 input 모두 포함
     */
    @Test
    void testComputeSync_InputSnapshotIncludesComputedFields() throws Exception {
        // given: 입력 데이터 있는 intent
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("count", 10);
        Intent intentWithInput = new Intent("test", inputData, "intent-2");

        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            schema,
            snapshot,
            intentWithInput,
            5
        );

        // then: 결과 snapshot에 computed와 input 모두 포함
        Snapshot resultSnapshot = result.getSnapshot();
        assertNotNull(resultSnapshot.getComputed().get("computed.total"), "computed.total should exist");
        assertNotNull(resultSnapshot.getInput().get("count"), "input.count should exist");
        assertEquals(10, resultSnapshot.getInput().get("count"));
    }

    /**
     * Test 7: 복잡한 Computed 필드 (연쇄 참조)
     *
     * 시나리오:
     * computed.a = 10
     * computed.b = computed.a + 20 (= 30)
     * computed.c = computed.b * 2 (= 60)
     */
    @Test
    void testComputeSync_ChainedComputedDependencies() throws Exception {
        // given: 연쇄 참조가 있는 computed 필드
        Map<String, ComputedFieldDef> chainedComputed = new HashMap<>();

        chainedComputed.put("computed.a", ComputedFieldDef.simple("computed.a", new Lit(10)));
        chainedComputed.put("computed.b", new ComputedFieldDef.Builder(
            "computed.b",
            new Add(new Get("computed.a"), new Lit(20))
        )
            .addDependency("computed.a")
            .build());
        chainedComputed.put("computed.c", new ComputedFieldDef.Builder(
            "computed.c",
            new Mul(new Get("computed.b"), new Lit(2))
        )
            .addDependency("computed.b")
            .build());

        ActionSpec action = new ActionSpec.Builder("test")
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema.Builder chainedBuilder = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash");
        for (ComputedFieldDef field : chainedComputed.values()) {
            chainedBuilder.addComputedField(field);
        }
        DomainSchema chainedSchema = chainedBuilder.addAction(action).build();

        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            chainedSchema,
            snapshot,
            intent,
            5
        );

        // then: 모든 computed 필드가 올바르게 계산됨
        Snapshot resultSnapshot = result.getSnapshot();
        assertEquals(10, resultSnapshot.getComputed().get("computed.a"));
        assertEquals(30, resultSnapshot.getComputed().get("computed.b"));
        assertEquals(60, resultSnapshot.getComputed().get("computed.c"));
    }

    /**
     * Test 8: Availability 체크 실패 시 원본 Snapshot 반환
     *
     * 시나리오:
     * 1. computed.isValid = false인 action
     * 2. Step 5에서 availability 체크 실패
     * 3. ComputeResult.error()에서 원본 snapshot 반환
     */
    @Test
    void testComputeSync_AvailabilityCheckFails_ReturnsOriginalSnapshot() throws Exception {
        // given: available 조건이 false인 action
        ActionSpec failingAction = new ActionSpec.Builder("test")
            .available(new Lit(false))
            .flow(FlowNode.Halt.of(null))
            .build();

        DomainSchema failSchema = new DomainSchema.Builder("test", "1.0.0")
            .hash("hash")
            .addAction(failingAction)
            .build();

        // when: computeSync 실행
        ComputeResult result = Compute.computeSync(
            failSchema,
            snapshot,
            intent,
            5
        );

        // then: ERROR 상태, 원본 snapshot 반환
        assertEquals(ComputeStatus.ERROR, result.getStatus());
        assertNotSame(snapshot, result.getSnapshot(),
            "Should return new snapshot on availability check failure");
    }
}
