package ai.manifesto.core.evaluator;

import ai.manifesto.core.*;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.flow.PatchOp;
import ai.manifesto.core.trace.TraceContext;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.utils.PathUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * FlowEvaluator - Flow 노드 평가 엔진
 *
 * Flow는 액션의 실행 흐름을 정의한다.
 * 각 노드(Seq, If, Patch, Effect, Call, Halt, Fail)를 평가한다.
 *
 * 핵심 원칙:
 * - 비동기 처리 (CompletableFuture)
 * - 상태 불변성 (Copy-on-Write)
 * - 재진입 안전성 (상태 가드)
 * - Patch는 즉시 적용, Effect는 선언만
 *
 * 상태 머신:
 * RUNNING → [계속] → COMPLETE (모든 노드 완료)
 *        → [Effect 발견] → PENDING
 *        → [Halt 발견] → HALTED
 *        → [Fail 발견] → ERROR
 */
public class FlowEvaluator {

    private FlowEvaluator() {
        // 정적 메서드만 제공
    }

    /**
     * Flow 평가 (비동기)
     *
     * @param flow 평가할 Flow 노드
     * @param ctx 평가 컨텍스트
     * @param state 현재 Flow 상태
     * @param nodePath 현재 노드 경로 (추적용)
     * @return 평가 결과를 담은 CompletableFuture
     */
    public static CompletableFuture<FlowResult> evaluate(
        FlowNode flow,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 재진입 안전성: 이미 종료된 상태면 진행하지 않음
        if (!state.isRunning()) {
            TraceNode trace = createTraceNode(
                TraceNode.Kind.FLOW,
                nodePath,
                "already_terminated",
                ctx.getTrace()
            );
            return CompletableFuture.completedFuture(new FlowResult(state, trace));
        }

        // Flow 종류별 처리 (Pattern Matching)
        if (flow instanceof FlowNode.Seq seq) {
            return evaluateSeq(seq, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.If ifFlow) {
            return evaluateIf(ifFlow, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.Patch patch) {
            return evaluatePatch(patch, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.Effect effect) {
            return evaluateEffect(effect, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.Call call) {
            return evaluateCall(call, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.Halt halt) {
            return evaluateHalt(halt, ctx, state, nodePath);
        } else if (flow instanceof FlowNode.Fail fail) {
            return evaluateFail(fail, ctx, state, nodePath);
        } else {
            // 알 수 없는 Flow 타입
            ErrorValue error = ErrorValue.create(
                "UNKNOWN_FLOW_TYPE",
                "Unknown flow type: " + flow.getClass().getSimpleName(),
                ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
                nodePath,
                ctx.getTrace().getTimestamp()
            );
            TraceNode trace = createTraceNode(
                TraceNode.Kind.ERROR,
                nodePath,
                "unknown_type",
                ctx.getTrace()
            );
            FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);
            return CompletableFuture.completedFuture(new FlowResult(newState, trace));
        }
    }

    /**
     * Seq 평가 - 단계별 순차 실행
     *
     * 각 단계를 순차적으로 평가한다.
     * 이전 단계의 FlowState가 다음 단계에 전달된다.
     * 어느 한 단계에서 종료되면(COMPLETE 아님) 더 이상 진행하지 않는다.
     */
    private static CompletableFuture<FlowResult> evaluateSeq(
        FlowNode.Seq seq,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        List<FlowNode> steps = seq.getSteps();

        if (steps.isEmpty()) {
            // 빈 Seq는 즉시 완료
            TraceNode trace = createTraceNode(
                TraceNode.Kind.FLOW,
                nodePath,
                "seq_empty",
                ctx.getTrace()
            );
            return CompletableFuture.completedFuture(new FlowResult(state, trace));
        }

        // 첫 번째 단계부터 시작
        return evaluateSeqSteps(steps, 0, ctx, state, nodePath);
    }

    /**
     * Seq의 단계별 평가 (재귀)
     *
     * @param steps 모든 단계
     * @param index 현재 단계 인덱스
     * @param ctx 평가 컨텍스트
     * @param state 누적된 상태
     * @param seqPath Seq 노드의 경로
     */
    private static CompletableFuture<FlowResult> evaluateSeqSteps(
        List<FlowNode> steps,
        int index,
        EvalContext ctx,
        FlowState state,
        String seqPath
    ) {
        // 기저 조건 1: 모든 단계 완료
        if (index >= steps.size()) {
            TraceNode trace = createTraceNode(
                TraceNode.Kind.FLOW,
                seqPath,
                "seq_complete",
                ctx.getTrace()
            );
            return CompletableFuture.completedFuture(new FlowResult(state, trace));
        }

        // 기저 조건 2: 이미 종료된 상태
        if (!state.isRunning()) {
            TraceNode trace = createTraceNode(
                null,
                seqPath,
                "seq_terminated",
                ctx.getTrace()
            );
            return CompletableFuture.completedFuture(new FlowResult(state, trace));
        }

        // 단계별 경로 생성 (추적용)
        String stepPath = seqPath + ".steps[" + index + "]";
        FlowNode currentStep = steps.get(index);

        // 현재 단계 평가
        return evaluate(currentStep, ctx, state, stepPath)
            .thenCompose(result -> {
                // 현재 단계의 결과 상태로 다음 단계 평가
                // 상태가 COMPLETE가 아니면 그대로 반환 (재진입 안전성)
                if (!result.state().getStatus().isComplete()) {
                    return CompletableFuture.completedFuture(result);
                }

                // 다음 단계 평가
                return evaluateSeqSteps(steps, index + 1, ctx, result.state(), seqPath);
            });
    }

    /**
     * If 평가 - 조건부 실행
     *
     * 1. 조건 식 평가
     * 2. 조건이 true면 then 분기, false면 else 분기 평가
     */
    private static CompletableFuture<FlowResult> evaluateIf(
        FlowNode.If ifFlow,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 1. 조건 식 평가
        Result<Object, ErrorValue> condResult = ExprEvaluator.evaluate(
            ifFlow.getCond(),
            ctx
        );

        if (condResult.isErr()) {
            // 조건 평가 실패
            ErrorValue error = (ErrorValue) condResult.mapErr(e -> e).unwrap();
            TraceNode trace = createTraceNode(
                TraceNode.Kind.FLOW,
                nodePath,
                "if_cond_error",
                ctx.getTrace()
            );
            FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);
            return CompletableFuture.completedFuture(new FlowResult(newState, trace));
        }

        // 2. 조건값을 boolean으로 변환
        Object condValue = condResult.unwrap();
        boolean condition = toBoolean(condValue);

        // 3. 분기 선택
        FlowNode branch = condition ? ifFlow.getThenBranch() : ifFlow.getElseBranch();

        if (branch == null) {
            // else 분기가 없는데 조건이 false
            TraceNode trace = createTraceNode(
                TraceNode.Kind.FLOW,
                nodePath,
                "if_no_else",
                ctx.getTrace()
            );
            return CompletableFuture.completedFuture(new FlowResult(state, trace));
        }

        // 4. 선택된 분기 평가
        String branchPath = nodePath + (condition ? ".then" : ".else");
        return evaluate(branch, ctx, state, branchPath);
    }

    /**
     * Patch 평가 - 상태 변경
     *
     * 1. 값 식 평가
     * 2. Patch 생성 (set/unset/merge)
     * 3. Snapshot에 적용
     * 4. FlowState 업데이트
     *
     * 중요: Patch는 즉시 적용되어 다음 식이 변경된 상태를 볼 수 있다.
     */
    private static CompletableFuture<FlowResult> evaluatePatch(
        FlowNode.Patch patchFlow,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 1. 값 식 평가 (unset의 경우 null)
        ExprNode valueExpr = patchFlow.getValue();
        Object value = null;

        if (valueExpr != null) {
            Result<Object, ErrorValue> valueResult = ExprEvaluator.evaluate(
                valueExpr,
                ctx
            );

            if (valueResult.isErr()) {
                // 값 평가 실패
                ErrorValue error = (ErrorValue) valueResult.mapErr(e -> e).unwrap();
                TraceNode trace = createTraceNode(
                    TraceNode.Kind.PATCH,
                    nodePath,
                    "patch_value_error",
                    ctx.getTrace()
                );
                FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);
                return CompletableFuture.completedFuture(new FlowResult(newState, trace));
            }

            value = valueResult.unwrap();
        }

        // 2. Patch 객체 생성 및 적용
        String path = patchFlow.getPath();
        PatchOp op = patchFlow.getOp();
        Snapshot currentSnapshot = state.getSnapshot();
        Snapshot newSnapshot = applyPatchToSnapshot(currentSnapshot, op, path, value);

        // 3. FlowState 업데이트
        // Patch 객체도 기록 (추적용)
        Patch patchObj = createPatch(op, path, value);
        FlowState newState = state
            .withSnapshot(newSnapshot)
            .addPatch(patchObj);

        TraceNode trace = createTraceNode(
            TraceNode.Kind.PATCH,
            nodePath,
            "patch_applied",
            ctx.getTrace()
        );

        return CompletableFuture.completedFuture(new FlowResult(newState, trace));
    }

    /**
     * Effect 평가 - 외부 작업 요청
     *
     * 1. 파라미터 식들 평가
     * 2. Requirement 생성
     * 3. FlowState를 PENDING으로 변경
     *
     * 중요: Snapshot은 변경하지 않는다!
     * Effect는 선언만 하고, Host가 나중에 실제로 실행한다.
     */
    private static CompletableFuture<FlowResult> evaluateEffect(
        FlowNode.Effect effect,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 1. 파라미터 평가
        Map<String, Object> params = new HashMap<>();

        for (Map.Entry<String, ExprNode> entry : effect.getParams().entrySet()) {
            Result<Object, ErrorValue> paramResult = ExprEvaluator.evaluate(
                entry.getValue(),
                ctx
            );

            if (paramResult.isErr()) {
                // 파라미터 평가 실패
                ErrorValue error = (ErrorValue) paramResult.mapErr(e -> e).unwrap();
                TraceNode trace = createTraceNode(
                    TraceNode.Kind.EFFECT,
                    nodePath,
                    "effect_param_error",
                    ctx.getTrace()
                );
                FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);
                return CompletableFuture.completedFuture(new FlowResult(newState, trace));
            }

            params.put(entry.getKey(), paramResult.unwrap());
        }

        // 2. Requirement 생성
        Requirement req = Requirement.create(
            effect.getType(),
            params,
            ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
            nodePath,
            ctx.getTrace().getTimestamp()
        );

        // 3. FlowState 업데이트
        // - 상태를 PENDING으로 변경 (addRequirement 메서드가 자동으로 처리)
        // - Requirement 추가
        // - Snapshot은 변경하지 않음
        FlowState newState = state.addRequirement(req);

        TraceNode trace = createTraceNode(
            TraceNode.Kind.EFFECT,
            nodePath,
            "effect_declared",
            ctx.getTrace()
        );

        return CompletableFuture.completedFuture(new FlowResult(newState, trace));
    }

    /**
     * Call 평가 - 다른 Flow 호출
     *
     * Schema에 정의된 다른 액션의 Flow를 호출한다.
     * Call의 flow 이름으로 Action을 조회하고, 해당 Flow를 재귀적으로 평가한다.
     */
    private static CompletableFuture<FlowResult> evaluateCall(
        FlowNode.Call call,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 호출할 액션 이름
        String actionId = call.getFlow();

        // Schema에서 해당 액션 조회
        var action = ctx.getSchema().getAction(actionId);
        if (action == null) {
            ErrorValue error = ErrorValue.create(
                "FLOW_NOT_FOUND",
                "Flow not found: " + actionId,
                ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
                nodePath,
                ctx.getTrace().getTimestamp()
            );

            TraceNode trace = createTraceNode(
                TraceNode.Kind.CALL,
                nodePath,
                "flow_not_found",
                ctx.getTrace()
            );

            FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);
            return CompletableFuture.completedFuture(new FlowResult(newState, trace));
        }

        // 호출할 Flow 노드 조회
        FlowNode calleeFlow = action.getFlow();

        // 재귀적으로 Flow 평가
        String callPath = nodePath + ".call." + actionId;
        return evaluate(calleeFlow, ctx, state, callPath);
    }

    /**
     * Halt 평가 - 정상 중단
     *
     * Flow를 정상적으로 중단한다.
     * 상태를 HALTED로 변경한다.
     */
    private static CompletableFuture<FlowResult> evaluateHalt(
        FlowNode.Halt halt,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        FlowState newState = state.withStatus(FlowStatus.HALTED);

        TraceNode trace = createTraceNode(
            TraceNode.Kind.HALT,
            nodePath,
            halt.getReason() != null ? halt.getReason() : "halted",
            ctx.getTrace()
        );

        return CompletableFuture.completedFuture(new FlowResult(newState, trace));
    }

    /**
     * Fail 평가 - 에러로 중단
     *
     * 1. 메시지 식 평가 (있으면)
     * 2. ErrorValue 생성
     * 3. FlowState를 ERROR로 변경
     */
    private static CompletableFuture<FlowResult> evaluateFail(
        FlowNode.Fail fail,
        EvalContext ctx,
        FlowState state,
        String nodePath
    ) {
        // 1. 메시지 식 평가
        String message = fail.getCode();

        if (fail.getMessage() != null) {
            Result<Object, ErrorValue> msgResult = ExprEvaluator.evaluate(
                fail.getMessage(),
                ctx
            );

            if (msgResult.isOk()) {
                message = msgResult.unwrap() != null
                    ? msgResult.unwrap().toString()
                    : fail.getCode();
            }
        }

        // 2. ErrorValue 생성
        ErrorValue error = ErrorValue.create(
            fail.getCode(),
            message,
            ctx.getCurrentAction() != null ? ctx.getCurrentAction() : "",
            nodePath,
            ctx.getTrace().getTimestamp()
        );

        // 3. FlowState 업데이트
        FlowState newState = state.withStatus(FlowStatus.ERROR).withError(error);

        TraceNode trace = createTraceNode(
            TraceNode.Kind.ERROR,
            nodePath,
            fail.getCode(),
            ctx.getTrace()
        );

        return CompletableFuture.completedFuture(new FlowResult(newState, trace));
    }

    // ===== 헬퍼 메서드 =====

    /**
     * 값을 boolean으로 변환
     *
     * 자바스크립트의 falsy 개념을 적용:
     * - null, false → false
     * - 0, "" → false
     * - 나머지 → true
     */
    private static boolean toBoolean(Object value) {
        if (value == null || value == Boolean.FALSE) {
            return false;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.doubleValue() != 0.0;
        }
        if (value instanceof String s) {
            return !s.isEmpty();
        }
        return true;  // 다른 모든 값은 true
    }

    /**
     * Snapshot에 Patch 적용
     *
     * 세 가지 연산:
     * - SET: 값 설정 (없으면 생성, 있으면 덮어쓰기)
     * - UNSET: 키 제거
     * - MERGE: 얕은 병합
     */
    @SuppressWarnings("unchecked")
    private static Snapshot applyPatchToSnapshot(
        Snapshot snapshot,
        PatchOp op,
        String path,
        Object value
    ) {
        Map<String, Object> data = new HashMap<>(snapshot.getData());

        switch (op) {
            case SET -> {
                // 경로에 값 설정
                Object result = PathUtils.setByPath(data, path, value);
                if (result instanceof Map) {
                    data = (Map<String, Object>) result;
                }
            }
            case UNSET -> {
                // 경로의 키 제거
                Object result = PathUtils.unsetByPath(data, path);
                if (result instanceof Map) {
                    data = (Map<String, Object>) result;
                }
            }
            case MERGE -> {
                // 얕은 병합
                Object result = PathUtils.mergeByPath(data, path, value);
                if (result instanceof Map) {
                    data = (Map<String, Object>) result;
                }
            }
        }

        return snapshot.withData(data);
    }

    /**
     * Patch 객체 생성
     */
    @SuppressWarnings("unchecked")
    private static Patch createPatch(PatchOp op, String path, Object value) {
        return switch (op) {
            case SET -> Patch.set(path, value);
            case UNSET -> Patch.unset(path);
            case MERGE -> {
                // Merge는 Map이어야 함
                if (value instanceof Map) {
                    yield Patch.merge(path, (Map<String, Object>) value);
                } else {
                    // 맵이 아니면 빈 맵으로 처리
                    yield Patch.merge(path, new HashMap<>());
                }
            }
        };
    }

    /**
     * TraceNode 생성 헬퍼 (필수: kind)
     */
    private static TraceNode createTraceNode(
        TraceNode.Kind kind,
        String nodePath,
        String label,
        TraceContext trace
    ) {
        Objects.requireNonNull(kind, "kind is required");
        String id = trace.nextId();

        return TraceNode.builder()
            .id(id)
            .kind(kind)
            .sourcePath(nodePath)
            .input("label", label)
            .timestamp(trace.getTimestamp())
            .build();
    }
}
