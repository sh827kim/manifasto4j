package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.evaluator.*;
import ai.manifesto.core.evaluator.ComputedEvaluator;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.trace.TraceBuilder;
import ai.manifesto.core.trace.TraceContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Compute - Manifesto의 핵심 계산 엔진
 *
 * compute(schema, snapshot, intent) → CompletableFuture<ComputeResult>
 *
 * 10단계 흐름 (1-7단계: 동기 검증, 8-10단계: 비동기 처리):
 * 1. Computed 필드들 평가 (DAG 순서)
 * 2. Schema에서 actionId에 해당하는 Action 스펙 조회 ✅ 구현
 * 3. Snapshot에서 intentId 확인 ✅ 구현
 * 4. Input 검증 (필드명 유효성) ✅ 구현
 * 5. Action의 available 조건 평가 ✅ 구현 (ExprEvaluator)
 * 6. 새로운 Snapshot 준비 (input 설정) ✅ 구현
 * 7. 평가 컨텍스트 생성 ✅ 구현
 * 8. Flow 평가 (비동기) - FlowEvaluator에서 처리
 * 9. Computed 값 재계산 - ExprEvaluator에서 처리
 * 10. System 상태 업데이트 및 Trace 생성 - Host에서 처리
 *
 * 핵심 원칙:
 * - 결정론적: 같은 입력 → 같은 출력
 * - 비동기: CompletableFuture로 Effect 처리
 * - 불변성: 원본 Snapshot은 변경 안 함
 * - 추적: 모든 단계가 TraceNode로 기록
 *
 * 상태: Phase 5 완성 ✅
 * - DomainSchema 통합 완료
 * - 1-7단계 전체 구현
 */
public class Compute {

    private Compute() {
        // 정적 메서드만 제공
    }

    /**
     * 비동기 계산 엔진
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 상태
     * @param intent 실행할 액션
     * @return 계산 결과를 담은 CompletableFuture
     */
    public static CompletableFuture<ComputeResult> compute(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent
    ) {
        return compute(schema, snapshot, intent, HostContext.forSnapshot(snapshot));
    }

    public static CompletableFuture<ComputeResult> compute(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        HostContext context
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        Objects.requireNonNull(intent, "intent is required");
        Objects.requireNonNull(context, "context is required");

        long startTime = context.getNow();

        try {
            // Step 1: Computed 필드 초기 평가 (availability 체크 전)
            Snapshot currentSnapshot = snapshot;
            Result<Map<String, Object>, ErrorValue> computedResult =
                ComputedEvaluator.evaluateComputed(schema, snapshot);

            if (computedResult instanceof Result.Err<?, ?> err) {
                return CompletableFuture.completedFuture(
                    createErrorResult(currentSnapshot, intent, (ErrorValue) err.error(), startTime, context)
                );
            }

            // Computed 값을 snapshot에 반영
            Map<String, Object> computedValues = computedResult.unwrap();
            currentSnapshot = currentSnapshot.withComputed(computedValues);

            // Step 2: Schema에서 actionType에 해당하는 Action 스펙 조회
            String actionType = intent.getType();
            ActionSpec action = schema.getAction(actionType);
            if (action == null) {
                return CompletableFuture.completedFuture(
                    createErrorResult(currentSnapshot, intent, "UNKNOWN_ACTION",
                        "Unknown action: " + actionType, startTime, context)
                );
            }

            // Step 3: intentId 확인
            String intentId = intent.getIntentId();
            if (intentId == null || intentId.isEmpty()) {
                return CompletableFuture.completedFuture(
                    createErrorResult(currentSnapshot, intent, "INVALID_INPUT",
                        "Intent must have a non-empty intentId", startTime, context)
                );
            }

            // Step 4: Input 검증
            Map<String, Object> intentInput = intent.getInput() != null
                ? intent.getInput()
                : new HashMap<>();
            String inputError = validateInput(action, intentInput);
            if (inputError != null) {
                return CompletableFuture.completedFuture(
                    createErrorResult(currentSnapshot, intent, "INVALID_INPUT", inputError, startTime, context)
                );
            }

            // Step 5: Action의 available 조건 평가
            if (action.getAvailable() != null) {
                TraceContext tempTrace = TraceContext.create(startTime);
                EvalContext tempCtx = EvalContext.builder()
                    .snapshot(currentSnapshot)
                    .schema(schema)
                    .currentAction(actionType)
                    .nodePath("available")
                    .intentId(intentId)
                    .trace(tempTrace)
                    .build();

                Result<Object, ErrorValue> availableResult = ExprEvaluator.evaluate(
                    action.getAvailable(),
                    tempCtx
                );

                if (availableResult.isErr()) {
                    return CompletableFuture.completedFuture(
                        createErrorResult(currentSnapshot, intent, "INTERNAL_ERROR",
                            "Error evaluating availability", startTime, context)
                    );
                }

                Object availableValue = availableResult.unwrap();
                if (!(availableValue instanceof Boolean)) {
                    return CompletableFuture.completedFuture(
                        createErrorResult(currentSnapshot, intent, "TYPE_MISMATCH",
                            "Availability condition must return boolean", startTime, context)
                    );
                }

                if (!((Boolean) availableValue)) {
                    return CompletableFuture.completedFuture(
                        createErrorResult(currentSnapshot, intent, "ACTION_UNAVAILABLE",
                            "Action \"" + actionType + "\" is not available", startTime, context)
                    );
                }
            }

            // Step 6: Snapshot 준비 (input + system 상태)
            SystemState computingSystem = currentSnapshot.getSystem()
                .withStatus(SystemState.Status.COMPUTING)
                .withCurrentAction(actionType);
            Snapshot preparedSnapshot = currentSnapshot
                .withInput(intentInput)
                .withSystem(computingSystem);

            // Step 7: 평가 컨텍스트 생성
            TraceContext traceContext = TraceContext.create(startTime);
            String flowPath = "actions." + actionType + ".flow";
            EvalContext evalContext = EvalContext.builder()
                .snapshot(preparedSnapshot)
                .schema(schema)
                .currentAction(actionType)
                .nodePath(flowPath)
                .intentId(intentId)
                .trace(traceContext)
                .build();

            FlowState flowState = FlowState.initial(preparedSnapshot);
            Snapshot baseSnapshot = currentSnapshot;

            // Step 8-10: Flow 평가 + Computed 재계산 + System/Trace 갱신
            return FlowEvaluator.evaluate(action.getFlow(), evalContext, flowState, flowPath)
                .thenApply(flowResult -> buildComputeResult(
                    baseSnapshot,
                    preparedSnapshot,
                    flowResult,
                    schema,
                    intent,
                    startTime,
                    context
                ))
                .exceptionally(error -> createErrorResult(
                    preparedSnapshot,
                    intent,
                    "COMPUTE_ERROR",
                    "Computation failed: " + error.getMessage(),
                    startTime,
                    context
                ));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                createErrorResult(snapshot, intent, "COMPUTE_ERROR",
                    "Computation failed: " + e.getMessage(), startTime, context)
            );
        }
    }

    private static ComputeResult buildComputeResult(
        Snapshot baseSnapshot,
        Snapshot preparedSnapshot,
        FlowResult flowResult,
        DomainSchema schema,
        Intent intent,
        long startTime,
        HostContext context
    ) {
        FlowState flowState = flowResult.state();

        // Step 9: Computed 재계산
        Snapshot finalSnapshot = flowState.getSnapshot();
        Result<Map<String, Object>, ErrorValue> computedResult =
            ComputedEvaluator.evaluateComputed(schema, finalSnapshot);
        if (computedResult.isOk()) {
            finalSnapshot = finalSnapshot.withComputed(computedResult.unwrap());
        }

        // Step 10: System 상태 및 Meta 업데이트
        ComputeStatus computeStatus = mapFlowStatus(flowState.getStatus());
        SystemState systemState = finalSnapshot.getSystem();
        SystemState.Status systemStatus = mapSystemStatus(computeStatus);

        systemState = systemState.withStatus(systemStatus)
            .withPendingRequirements(flowState.getRequirements())
            .withCurrentAction(flowState.getStatus().isPending() ? intent.getType() : null);

        if (flowState.getError() != null) {
            systemState = systemState.withError(flowState.getError());
        } else {
            systemState = systemState.withLastError(null);
        }

        finalSnapshot = finalSnapshot.withSystem(systemState);

        Snapshot.SnapshotMeta updatedMeta = finalSnapshot.getMeta().nextVersion(context.getNow());
        finalSnapshot = finalSnapshot.withMeta(updatedMeta);

        long duration = context.getDurationMs() != null
            ? context.getDurationMs()
            : System.currentTimeMillis() - startTime;

        TraceGraph trace = TraceBuilder.buildGraph(
            flowResult.trace(),
            intent,
            baseSnapshot.getMeta().getVersion(),
            finalSnapshot.getMeta().getVersion(),
            duration,
            mapTermination(flowState.getStatus())
        );

        return switch (computeStatus) {
            case COMPLETE -> ComputeResult.complete(finalSnapshot, trace);
            case PENDING -> ComputeResult.pending(finalSnapshot, flowState.getRequirements(), trace);
            case HALTED -> ComputeResult.halted(finalSnapshot, trace);
            case ERROR -> ComputeResult.error(finalSnapshot, trace);
        };
    }

    private static ComputeResult createErrorResult(
        Snapshot snapshot,
        Intent intent,
        String code,
        String message,
        long startTime,
        HostContext context
    ) {
        ErrorValue error = ErrorValue.create(
            code,
            message,
            intent.getType(),
            "",
            context.getNow()
        );

        return createErrorResult(snapshot, intent, error, startTime, context);
    }

    private static ComputeResult createErrorResult(
        Snapshot snapshot,
        Intent intent,
        ErrorValue error,
        long startTime,
        HostContext context
    ) {
        SystemState systemState = snapshot.getSystem()
            .withStatus(SystemState.Status.ERROR)
            .withError(error)
            .withCurrentAction(null)
            .withPendingRequirements(List.of());

        Snapshot errorSnapshot = snapshot
            .withInput(intent.getInput() != null ? intent.getInput() : new HashMap<>())
            .withSystem(systemState)
            .withMeta(snapshot.getMeta().nextVersion(context.getNow()));

        TraceNode errorTrace = TraceNode.builder()
            .id("trace-error-" + (intent.getIntentId() != null ? intent.getIntentId() : startTime))
            .kind(TraceNode.Kind.ERROR)
            .sourcePath("")
            .output(error)
            .children(List.of())
            .timestamp(context.getNow())
            .build();

        TraceGraph trace = TraceBuilder.buildGraph(
            errorTrace,
            intent,
            snapshot.getMeta().getVersion(),
            errorSnapshot.getMeta().getVersion(),
            context.getDurationMs() != null ? context.getDurationMs() : System.currentTimeMillis() - startTime,
            TraceGraph.TraceTermination.ERROR
        );

        return ComputeResult.error(errorSnapshot, trace);
    }

    private static ComputeStatus mapFlowStatus(FlowStatus status) {
        return switch (status) {
            case RUNNING, COMPLETE -> ComputeStatus.COMPLETE;
            case PENDING -> ComputeStatus.PENDING;
            case HALTED -> ComputeStatus.HALTED;
            case ERROR -> ComputeStatus.ERROR;
        };
    }

    private static SystemState.Status mapSystemStatus(ComputeStatus status) {
        return switch (status) {
            case COMPLETE, HALTED -> SystemState.Status.IDLE;
            case PENDING -> SystemState.Status.PENDING;
            case ERROR -> SystemState.Status.ERROR;
        };
    }

    private static TraceGraph.TraceTermination mapTermination(FlowStatus status) {
        return switch (status) {
            case RUNNING, COMPLETE -> TraceGraph.TraceTermination.COMPLETE;
            case PENDING -> TraceGraph.TraceTermination.EFFECT;
            case HALTED -> TraceGraph.TraceTermination.HALT;
            case ERROR -> TraceGraph.TraceTermination.ERROR;
        };
    }

    private static String validateInput(ActionSpec action, Map<String, Object> input) {
        FieldSpec inputSpec = action.getInputSpec();
        if (inputSpec != null && "object".equals(inputSpec.getType()) && inputSpec.getFields() != null) {
            return validateInput(inputSpec.getFields(), input);
        }
        return validateInput(action.getInputFields(), input);
    }

    private static String validateInput(Map<String, FieldSpec> inputSpec, Map<String, Object> input) {
        if (inputSpec == null || inputSpec.isEmpty()) {
            if (input == null || input.isEmpty()) {
                return null;
            }
            return "Unknown input field: " + input.keySet().iterator().next();
        }

        for (Map.Entry<String, FieldSpec> entry : inputSpec.entrySet()) {
            if (entry.getValue().isRequired() && !input.containsKey(entry.getKey())) {
                return "Missing required field: " + entry.getKey();
            }
        }

        for (String key : input.keySet()) {
            if (!inputSpec.containsKey(key)) {
                return "Unknown field: " + key;
            }
        }

        for (Map.Entry<String, FieldSpec> entry : inputSpec.entrySet()) {
            if (input.containsKey(entry.getKey())) {
                String error = validateFieldValue(entry.getValue(), input.get(entry.getKey()), entry.getKey());
                if (error != null) {
                    return error;
                }
            }
        }

        return null;
    }

    private static String validateFieldValue(FieldSpec spec, Object value, String path) {
        if (value == null) {
            if (spec.isRequired()) {
                return "Missing required field: " + path;
            }
            return null;
        }

        if (spec.getEnumValues() != null && !spec.getEnumValues().isEmpty()) {
            if (!spec.getEnumValues().contains(value)) {
                return "Invalid enum value for " + path;
            }
            return null;
        }

        return switch (spec.getType()) {
            case "string" -> value instanceof String ? null : "Expected string for " + path;
            case "number" -> value instanceof Number ? null : "Expected number for " + path;
            case "integer" -> (value instanceof Integer || value instanceof Long) ? null : "Expected integer for " + path;
            case "boolean" -> value instanceof Boolean ? null : "Expected boolean for " + path;
            case "array" -> {
                if (!(value instanceof List<?> list)) {
                    yield "Expected array for " + path;
                }
                FieldSpec items = spec.getItems();
                if (items != null) {
                    for (int i = 0; i < list.size(); i++) {
                        String error = validateFieldValue(items, list.get(i), path + "[" + i + "]");
                        if (error != null) {
                            yield error;
                        }
                    }
                }
                yield null;
            }
            case "object" -> {
                if (!(value instanceof Map<?, ?> map)) {
                    yield "Expected object for " + path;
                }
                Map<String, FieldSpec> fields = spec.getFields();
                if (fields != null && !fields.isEmpty()) {
                    for (Map.Entry<String, FieldSpec> entry : fields.entrySet()) {
                        String fieldName = entry.getKey();
                        FieldSpec fieldSpec = entry.getValue();
                        if (fieldSpec.isRequired() && !map.containsKey(fieldName)) {
                            yield "Missing required field: " + path + "." + fieldName;
                        }
                    }
                    for (Object keyObj : map.keySet()) {
                        String key = String.valueOf(keyObj);
                        if (!fields.containsKey(key)) {
                            yield "Unknown field: " + path + "." + key;
                        }
                    }
                    for (Map.Entry<String, FieldSpec> entry : fields.entrySet()) {
                        String fieldName = entry.getKey();
                        if (map.containsKey(fieldName)) {
                            String error = validateFieldValue(entry.getValue(), map.get(fieldName), path + "." + fieldName);
                            if (error != null) {
                                yield error;
                            }
                        }
                    }
                }
                yield null;
            }
            default -> null;
        };
    }

    /**
     * 편의 메서드: 결과를 동기로 대기 (테스트용)
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 상태
     * @param intent 실행할 액션
     * @param timeoutSeconds 타임아웃 (초)
     * @return 계산 결과
     * @throws Exception 타임아웃 또는 계산 실패
     */
    public static ComputeResult computeSync(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        int timeoutSeconds
    ) throws Exception {
        return compute(schema, snapshot, intent)
            .get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static ComputeResult computeSync(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        HostContext context,
        int timeoutSeconds
    ) throws Exception {
        return compute(schema, snapshot, intent, context)
            .get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }
}
