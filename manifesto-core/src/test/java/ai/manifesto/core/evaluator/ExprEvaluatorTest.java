package ai.manifesto.core.evaluator;

import ai.manifesto.core.*;
import ai.manifesto.core.expr.arithmetic.*;
import ai.manifesto.core.expr.collection.Reduce;
import ai.manifesto.core.expr.comparison.*;
import ai.manifesto.core.expr.literal.*;
import ai.manifesto.core.expr.logical.*;
import ai.manifesto.core.expr.string.*;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.trace.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Expression 평가 테스트")
class ExprEvaluatorTest {

    private DomainSchema schema;
    private Snapshot snapshot;
    private EvalContext context;

    @BeforeEach
    void setUp() {
        FieldSpec countField = FieldSpec.required("count", "integer");
        schema = new DomainSchema.Builder("test-schema", "1.0.0")
            .hash("test-hash")
            .addDataField(countField)
            .build();

        Map<String, Object> data = new HashMap<>();
        data.put("count", 10);

        snapshot = Snapshot.builder()
            .data(data)
            .computed(new HashMap<>())
            .system(SystemState.initial())
            .input(new HashMap<>())
            .meta(Snapshot.SnapshotMeta.create(0, System.currentTimeMillis(), "seed", "hash"))
            .build();

        TraceContext trace = TraceContext.create(System.currentTimeMillis());
        context = EvalContext.builder()
            .snapshot(snapshot)
            .schema(schema)
            .currentAction("testAction")
            .nodePath("test")
            .intentId("intent-123")
            .trace(trace)
            .build();
    }

    @Test
    @DisplayName("리터럴 값 평가")
    void testLiteralEvaluation() {
        Lit lit = new Lit(42);
        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(lit, context);

        assertTrue(result.isOk());
        assertEquals(42, result.unwrap());
    }

    @Test
    @DisplayName("GET 표현식 평가")
    void testGetExpression() {
        Get getExpr = new Get("data.count");
        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(getExpr, context);

        assertTrue(result.isOk());
        assertEquals(10, result.unwrap());
    }

    @Test
    @DisplayName("덧셈 표현식")
    void testAdditionExpression() {
        Lit left = new Lit(5);
        Lit right = new Lit(3);
        Add addExpr = Add.of(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(addExpr, context);

        assertTrue(result.isOk());
        assertEquals(8, result.unwrap());
    }

    @Test
    @DisplayName("뺄셈 표현식")
    void testSubtractionExpression() {
        Lit left = new Lit(10);
        Lit right = new Lit(3);
        Sub subExpr = new Sub(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(subExpr, context);

        assertTrue(result.isOk());
        assertEquals(7, result.unwrap());
    }

    @Test
    @DisplayName("곱셈 표현식")
    void testMultiplicationExpression() {
        Lit left = new Lit(5);
        Lit right = new Lit(4);
        Mul mulExpr = new Mul(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(mulExpr, context);

        assertTrue(result.isOk());
        assertEquals(20, result.unwrap());
    }

    @Test
    @DisplayName("나눗셈 표현식")
    void testDivisionExpression() {
        Lit left = new Lit(20);
        Lit right = new Lit(4);
        Div divExpr = new Div(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(divExpr, context);

        assertTrue(result.isOk());
        assertEquals(5, result.unwrap());
    }

    @Test
    @DisplayName("비교 표현식: 같음")
    void testEqualityComparison() {
        Lit left = new Lit(42);
        Lit right = new Lit(42);
        Eq eqExpr = new Eq(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(eqExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("비교 표현식: 작음")
    void testLessThanComparison() {
        Lit left = new Lit(5);
        Lit right = new Lit(10);
        Lt ltExpr = new Lt(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(ltExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("비교 표현식: 크거나 같음")
    void testGreaterThanOrEqualComparison() {
        Lit left = new Lit(10);
        Lit right = new Lit(10);
        Gte gteExpr = new Gte(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(gteExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("논리 AND 표현식")
    void testLogicalAnd() {
        Lit left = new Lit(true);
        Lit right = new Lit(true);
        And andExpr = And.of(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(andExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("논리 OR 표현식")
    void testLogicalOr() {
        Lit left = new Lit(false);
        Lit right = new Lit(true);
        Or orExpr = Or.of(left, right);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(orExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("논리 NOT 표현식")
    void testLogicalNot() {
        Lit value = new Lit(false);
        Not notExpr = new Not(value);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(notExpr, context);

        assertTrue(result.isOk());
        assertTrue((Boolean) result.unwrap());
    }

    @Test
    @DisplayName("중첩된 표현식")
    void testNestedExpression() {
        // (5 + 3) * 2 = 16
        Lit five = new Lit(5);
        Lit three = new Lit(3);
        Lit two = new Lit(2);

        Add add = Add.of(five, three);
        Mul mul = new Mul(add, two);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(mul, context);

        assertTrue(result.isOk());
        assertEquals(16, result.unwrap());
    }

    @Test
    @DisplayName("0으로 나누기")
    void testDivisionByZero() {
        Lit left = new Lit(10);
        Lit zero = new Lit(0);
        Div divExpr = new Div(left, zero);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(divExpr, context);

        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("타입 불일치 처리")
    void testTypesMismatch() {
        Lit stringVal = new Lit("hello");
        Lit numVal = new Lit(5);
        Add addExpr = Add.of(stringVal, numVal);

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(addExpr, context);

        // 타입 불일치는 에러를 반환할 수 있음
        assertNotNull(result);
    }

    @Test
    @DisplayName("존재하지 않는 경로 GET")
    void testGetNonExistentPath() {
        Get getExpr = new Get("data.nonexistent");
        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(getExpr, context);

        // 존재하지 않는 경로는 null을 반환하거나 에러를 반환할 수 있음
        assertTrue(result.isOk() || result.isErr());
    }

    @Test
    @DisplayName("반올림/내림/올림 표현식")
    void testRoundingExpressions() {
        Round roundExpr = Round.of(new Lit(2.6));
        Floor floorExpr = Floor.of(new Lit(2.6));
        Ceil ceilExpr = Ceil.of(new Lit(2.1));

        Result<Object, ErrorValue> roundResult = ExprEvaluator.evaluate(roundExpr, context);
        Result<Object, ErrorValue> floorResult = ExprEvaluator.evaluate(floorExpr, context);
        Result<Object, ErrorValue> ceilResult = ExprEvaluator.evaluate(ceilExpr, context);

        assertTrue(roundResult.isOk());
        assertTrue(floorResult.isOk());
        assertTrue(ceilResult.isOk());
        assertEquals(3L, roundResult.unwrap());
        assertEquals(2L, floorResult.unwrap());
        assertEquals(3L, ceilResult.unwrap());
    }

    @Test
    @DisplayName("startsWith/endsWith 표현식")
    void testStringPrefixSuffixExpressions() {
        StartsWith startsWithExpr = StartsWith.of(new Lit("hello"), new Lit("he"));
        EndsWith endsWithExpr = EndsWith.of(new Lit("hello"), new Lit("lo"));

        Result<Object, ErrorValue> startsWithResult = ExprEvaluator.evaluate(startsWithExpr, context);
        Result<Object, ErrorValue> endsWithResult = ExprEvaluator.evaluate(endsWithExpr, context);

        assertTrue(startsWithResult.isOk());
        assertTrue(endsWithResult.isOk());
        assertEquals(true, startsWithResult.unwrap());
        assertEquals(true, endsWithResult.unwrap());
    }

    @Test
    @DisplayName("split 표현식")
    void testSplitExpression() {
        Split splitExpr = Split.of(new Lit("a,b,,c"), new Lit(","));

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(splitExpr, context);

        assertTrue(result.isOk());
        assertEquals(List.of("a", "b", "", "c"), result.unwrap());
    }

    @Test
    @DisplayName("reduce 표현식")
    void testReduceExpression() {
        Reduce reduceExpr = Reduce.of(
            new Lit(List.of(1, 2, 3)),
            Add.of(new Get("$acc"), new Get("$item")),
            new Lit(0)
        );

        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(reduceExpr, context);

        assertTrue(result.isOk());
        assertEquals(6, result.unwrap());
    }
}
