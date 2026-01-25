package ai.manifesto.core.evaluator;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Result;
import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.trace.TraceContext;
import ai.manifesto.core.utils.DagUtils;

import java.util.*;

/**
 * ComputedEvaluator - Computed 필드 평가 엔진
 *
 * 역할:
 * - Computed 필드들을 DAG 순서대로 평가
 * - 순환 참조 감지 (V-002 에러)
 * - 의존성 있는 필드는 의존 대상이 먼저 계산된 후 평가
 * - 이전 계산 결과를 다음 필드 평가에서 참조 가능하도록 tempSnapshot 업데이트
 *
 * 특징:
 * - Pure & Total: 예외 던지지 않음, 항상 Result 반환
 * - 결정론적: 같은 입력 → 같은 출력
 * - 불변: Snapshot 원본 변경하지 않고 새로운 computed Map 반환
 *
 * 사용 예시:
 * {@code
 * Result<Map<String, Object>, ErrorValue> result =
 *     ComputedEvaluator.evaluateComputed(schema, snapshot);
 *
 * if (result.isErr()) {
 *     // V-002: 순환 참조, 또는 다른 평가 에러
 *     ErrorValue error = result.getError();
 * } else {
 *     Map<String, Object> computed = result.unwrap();
 *     Snapshot newSnapshot = snapshot.withComputed(computed);
 * }
 * }
 */
public class ComputedEvaluator {

    private ComputedEvaluator() {
        // 정적 메서드만 제공
    }

    /**
     * 모든 Computed 필드를 DAG 순서대로 평가
     *
     * 알고리즘:
     * 1. DAG 구축: DagUtils.buildDependencyGraph()로 computed 필드 의존성 그래프 생성
     * 2. 위상 정렬: DagUtils.topologicalSort()로 계산 순서 결정
     * 3. 순서대로 평가: 각 필드를 ExprEvaluator로 평가
     * 4. Snapshot 업데이트: 다음 필드가 이전 결과를 참조할 수 있도록 tempSnapshot 업데이트
     *
     * 순환 참조 예시 (실패):
     * {@code
     * computed.a = get("computed.b")
     * computed.b = get("computed.a")
     * // topologicalSort() → V-002 에러 반환
     * }
     *
     * 의존성 예시 (성공):
     * {@code
     * computed.price = lit(100)
     * computed.tax = get("computed.price") * 0.1      // 10
     * computed.total = get("computed.price") + get("computed.tax")  // 110
     * // 결과: { "price": 100, "tax": 10, "total": 110 }
     * }
     *
     * @param schema 도메인 스키마 (computed 필드 정의 포함)
     * @param snapshot 현재 스냅샷 (data, system 등 포함)
     * @return Result<Map<String, Object>, ErrorValue>
     *         - Ok: 계산된 모든 computed 값 (fieldName → value)
     *         - Err: 순환 참조(V-002) 또는 표현식 평가 에러
     */
    public static Result<Map<String, Object>, ErrorValue> evaluateComputed(
        DomainSchema schema,
        Snapshot snapshot
    ) {
        // TraceContext 생성 (결정론적 UUID 생성 등에 사용)
        TraceContext trace = TraceContext.create(snapshot.getMeta().getTimestamp());

        // 1. Computed 필드 조회
        Map<String, ComputedFieldDef> computedFields = schema.getComputedFields();

        // 빈 경우 빈 Map 반환
        if (computedFields.isEmpty()) {
            return Result.ok(new HashMap<>());
        }

        // 2. DAG 구축
        DagUtils.DependencyGraph graph = DagUtils.buildDependencyGraph(computedFields);

        // 3. 위상 정렬 (순환 참조 감지)
        Result<List<String>, ErrorValue> sortResult = DagUtils.topologicalSort(graph);
        if (sortResult instanceof Result.Err<?, ?> err) {
            // 순환 참조 감지됨 (V-002 에러)
            return Result.err((ErrorValue) err.error());
        }

        List<String> sortedFields = sortResult.unwrap();

        // 4. 위상 정렬 순서대로 각 필드 평가
        Map<String, Object> computed = new HashMap<>();
        Snapshot tempSnapshot = snapshot.withComputed(computed);

        for (String fieldName : sortedFields) {
            ComputedFieldDef fieldDef = computedFields.get(fieldName);
            if (fieldDef == null) {
                // 이미 DAG에 있으므로 정상적으로는 이 경우 발생하지 않음
                continue;
            }

            // EvalContext 생성 (평가에 필요한 모든 정보 포함)
            EvalContext ctx = EvalContext.builder()
                .snapshot(tempSnapshot)           // 현재까지 계산된 값들 포함
                .schema(schema)
                .currentAction(null)
                .nodePath(fieldName)              // 현재 필드명 (추적용)
                .intentId(null)
                .trace(trace)                     // 결정론적 UUID 생성 등에 사용
                .build();

            // 표현식 평가
            Result<Object, ErrorValue> result = ExprEvaluator.evaluate(
                fieldDef.getExpression(),
                ctx
            );

            // 평가 실패 시 에러 반환
            if (result instanceof Result.Err<?, ?> err) {
                return Result.err((ErrorValue) err.error());
            }

            // 계산된 값 저장
            Object value = result.unwrap();
            computed.put(fieldName, value);

            // 임시 Snapshot 업데이트
            // 목적: 다음 필드 평가 시 이 필드의 값을 참조할 수 있도록
            // 예: computed.tax는 computed.price를 참조할 수 있어야 함
            tempSnapshot = tempSnapshot.withComputed(new HashMap<>(computed));
        }

        return Result.ok(computed);
    }

    /**
     * 단일 Computed 필드만 평가
     *
     * 용도:
     * - 특정 computed 필드만 필요한 경우
     * - 의존성을 무시하고 현재 snapshot 기준으로 평가
     * - 다른 필드들이 이미 계산된 상태에서 하나만 재계산
     *
     * 특징:
     * - 의존성 체크 없음 (순환 참조 가능성 있음)
     * - 다른 computed 필드를 참조하면 snapshot.computed에서 가져옴
     * - 없으면 null 반환
     *
     * 사용 예시:
     * {@code
     * // snapshot.computed에 "price"는 있지만 "tax"는 없는 상태
     * Result<Object, ErrorValue> result =
     *     ComputedEvaluator.evaluateSingleComputed(schema, snapshot, "tax");
     *
     * if (result.isOk()) {
     *     Object taxValue = result.unwrap();
     * }
     * }
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 스냅샷
     * @param fieldName 평가할 computed 필드 이름
     * @return Result<Object, ErrorValue>
     *         - Ok: 계산된 값
     *         - Err: 필드 없음(PATH_NOT_FOUND) 또는 평가 에러
     */
    public static Result<Object, ErrorValue> evaluateSingleComputed(
        DomainSchema schema,
        Snapshot snapshot,
        String fieldName
    ) {
        TraceContext trace = TraceContext.create(snapshot.getMeta().getTimestamp());

        // 필드 조회
        ComputedFieldDef fieldDef = schema.getComputedField(fieldName);
        if (fieldDef == null) {
            // 필드 없음 에러
            return Result.err(ErrorValue.create(
                "PATH_NOT_FOUND",
                "Computed field not found: " + fieldName,
                null,
                fieldName,
                trace.getTimestamp()
            ));
        }

        // EvalContext 생성
        EvalContext ctx = EvalContext.builder()
            .snapshot(snapshot)
            .schema(schema)
            .currentAction(null)
            .nodePath(fieldName)
            .intentId(null)
            .trace(trace)
            .build();

        // 표현식 평가
        return ExprEvaluator.evaluate(fieldDef.getExpression(), ctx);
    }
}
