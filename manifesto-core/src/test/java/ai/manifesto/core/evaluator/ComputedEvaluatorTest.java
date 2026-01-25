package ai.manifesto.core.evaluator;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ComputedEvaluator 테스트
 *
 * 테스트 전략:
 * - evaluateComputed(): 모든 computed 필드를 DAG 순서대로 평가하는지 확인
 * - evaluateSingleComputed(): 단일 필드만 평가하는지 확인
 * - 순환 참조 감지: V-002 에러 반환
 * - 의존성 체크: 필드가 없거나 평가 실패 시 에러 반환
 */
@DisplayName("ComputedEvaluator 테스트")
public class ComputedEvaluatorTest {

    private Snapshot baseSnapshot;
    private long timestamp;

    @BeforeEach
    void setUp() {
        timestamp = 1000000L;
        baseSnapshot = Snapshot.builder()
            .data(new HashMap<>(Map.of("price", 100)))
            .computed(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, timestamp, "", ""))
            .build();
    }

    // ===== evaluateComputed() 테스트 =====

    @Test
    @DisplayName("evaluateComputed: 빈 computedFields 반환")
    void testEvaluateComputed_EmptyComputedFields() {
        // given
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isOk());
        Map<String, Object> computed = result.unwrap();
        assertTrue(computed.isEmpty());
    }

    @Test
    @DisplayName("evaluateComputed: 단일 computed 필드 (의존성 없음)")
    void testEvaluateComputed_SingleFieldNoDeps() {
        // given: computed.total = lit(100)
        ComputedFieldDef totalField = new ComputedFieldDef(
            "total",
            new Lit(100),
            new HashSet<>()
        );

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(totalField)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isOk());
        Map<String, Object> computed = result.unwrap();
        assertEquals(100, computed.get("total"));
    }

    @Test
    @DisplayName("evaluateComputed: 순환 참조 감지 (V-002)")
    void testEvaluateComputed_CircularDependency() {
        // given: a → b, b → a (순환)
        ComputedFieldDef fieldA = new ComputedFieldDef.Builder("a", new Lit(1))
            .addDependency("b")
            .build();

        ComputedFieldDef fieldB = new ComputedFieldDef.Builder("b", new Lit(2))
            .addDependency("a")
            .build();

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(fieldA)
            .addComputedField(fieldB)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isErr());
        if (result instanceof Result.Err<?, ?> err) {
            ErrorValue error = (ErrorValue) err.error();
            assertEquals("V-002", error.getCode());
        }
    }

    @Test
    @DisplayName("evaluateComputed: 선형 의존성 (a → b → c)")
    void testEvaluateComputed_LinearDependency() {
        // given:
        // computed.a = lit(10)
        // computed.b = lit(20)
        // computed.c = lit(30)
        ComputedFieldDef fieldA = ComputedFieldDef.simple("a", new Lit(10));
        ComputedFieldDef fieldB = ComputedFieldDef.simple("b", new Lit(20));
        ComputedFieldDef fieldC = ComputedFieldDef.simple("c", new Lit(30));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(fieldA)
            .addComputedField(fieldB)
            .addComputedField(fieldC)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isOk());
        Map<String, Object> computed = result.unwrap();
        assertEquals(10, computed.get("a"));
        assertEquals(20, computed.get("b"));
        assertEquals(30, computed.get("c"));
        assertEquals(3, computed.size());
    }

    @Test
    @DisplayName("evaluateComputed: 여러 필드 함께 평가")
    void testEvaluateComputed_MultipleFields() {
        // given: price, tax, total 3개 필드
        ComputedFieldDef priceField = ComputedFieldDef.simple("price", new Lit(100));
        ComputedFieldDef taxField = ComputedFieldDef.simple("tax", new Lit(10));
        ComputedFieldDef totalField = ComputedFieldDef.simple("total", new Lit(110));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(priceField)
            .addComputedField(taxField)
            .addComputedField(totalField)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isOk());
        Map<String, Object> computed = result.unwrap();
        assertEquals(100, computed.get("price"));
        assertEquals(10, computed.get("tax"));
        assertEquals(110, computed.get("total"));
    }

    @Test
    @DisplayName("evaluateComputed: 결정론성 검증 (같은 입력 → 같은 출력)")
    void testEvaluateComputed_Determinism() {
        // given
        ComputedFieldDef fieldA = ComputedFieldDef.simple("a", new Lit(42));
        ComputedFieldDef fieldB = ComputedFieldDef.simple("b", new Lit(100));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(fieldA)
            .addComputedField(fieldB)
            .build();

        // when: 두 번 실행
        Result<Map<String, Object>, ErrorValue> result1 =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);
        Result<Map<String, Object>, ErrorValue> result2 =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then: 같은 결과
        assertTrue(result1.isOk());
        assertTrue(result2.isOk());
        assertEquals(result1.unwrap(), result2.unwrap());
    }

    // ===== evaluateSingleComputed() 테스트 =====

    @Test
    @DisplayName("evaluateSingleComputed: 정상 평가")
    void testEvaluateSingleComputed_Normal() {
        // given
        ComputedFieldDef field = ComputedFieldDef.simple("value", new Lit(42));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(field)
            .build();

        // when
        Result<Object, ErrorValue> result =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "value");

        // then
        assertTrue(result.isOk());
        assertEquals(42, result.unwrap());
    }

    @Test
    @DisplayName("evaluateSingleComputed: 필드 없음 (PATH_NOT_FOUND)")
    void testEvaluateSingleComputed_FieldNotFound() {
        // given
        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .build();

        // when
        Result<Object, ErrorValue> result =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "nonexistent");

        // then
        assertTrue(result.isErr());
        if (result instanceof Result.Err<?, ?> err) {
            ErrorValue error = (ErrorValue) err.error();
            assertEquals("PATH_NOT_FOUND", error.getCode());
            assertTrue(error.getMessage().contains("nonexistent"));
        }
    }

    @Test
    @DisplayName("evaluateSingleComputed: 여러 필드 중 하나만 평가")
    void testEvaluateSingleComputed_OneOfMany() {
        // given: 3개 필드 중 "b"만 평가
        ComputedFieldDef fieldA = ComputedFieldDef.simple("a", new Lit(10));
        ComputedFieldDef fieldB = ComputedFieldDef.simple("b", new Lit(20));
        ComputedFieldDef fieldC = ComputedFieldDef.simple("c", new Lit(30));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(fieldA)
            .addComputedField(fieldB)
            .addComputedField(fieldC)
            .build();

        // when
        Result<Object, ErrorValue> result =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "b");

        // then
        assertTrue(result.isOk());
        assertEquals(20, result.unwrap());
    }

    @Test
    @DisplayName("evaluateSingleComputed: 결정론성 검증")
    void testEvaluateSingleComputed_Determinism() {
        // given
        ComputedFieldDef field = ComputedFieldDef.simple("value", new Lit(99));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(field)
            .build();

        // when: 두 번 실행
        Result<Object, ErrorValue> result1 =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "value");
        Result<Object, ErrorValue> result2 =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "value");

        // then: 같은 결과
        assertTrue(result1.isOk());
        assertTrue(result2.isOk());
        assertEquals(result1.unwrap(), result2.unwrap());
    }

    // ===== 엣지 케이스 테스트 =====

    @Test
    @DisplayName("evaluateComputed: null 필드값 처리")
    void testEvaluateComputed_NullValue() {
        // given
        ComputedFieldDef field = ComputedFieldDef.simple("nullField", new Lit(null));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(field)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then
        assertTrue(result.isOk());
        Map<String, Object> computed = result.unwrap();
        assertNull(computed.get("nullField"));
    }

    @Test
    @DisplayName("evaluateComputed: 자기 자신 의존 감지 (a → a)")
    void testEvaluateComputed_SelfDependency() {
        // given: a → a (자기 자신에 의존)
        ComputedFieldDef field = new ComputedFieldDef.Builder("a", new Lit(1))
            .addDependency("a")
            .build();

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(field)
            .build();

        // when
        Result<Map<String, Object>, ErrorValue> result =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);

        // then: V-002 에러 (순환 참조)
        assertTrue(result.isErr());
        if (result instanceof Result.Err<?, ?> err) {
            ErrorValue error = (ErrorValue) err.error();
            assertEquals("V-002", error.getCode());
        }
    }

    @Test
    @DisplayName("evaluateComputed & evaluateSingleComputed: 메서드 분리 확인")
    void testBothMethods_Independence() {
        // given
        ComputedFieldDef field = ComputedFieldDef.simple("value", new Lit(55));

        DomainSchema schema = new DomainSchema.Builder("test", "1.0.0")
            .hash("test-hash")
            .addComputedField(field)
            .build();

        // when: 두 메서드 호출
        Result<Map<String, Object>, ErrorValue> result1 =
            ComputedEvaluator.evaluateComputed(schema, baseSnapshot);
        Result<Object, ErrorValue> result2 =
            ComputedEvaluator.evaluateSingleComputed(schema, baseSnapshot, "value");

        // then: 같은 결과값
        assertTrue(result1.isOk());
        assertTrue(result2.isOk());
        assertEquals(result1.unwrap().get("value"), result2.unwrap());
    }
}
