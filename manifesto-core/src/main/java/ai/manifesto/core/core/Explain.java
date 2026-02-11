package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.evaluator.EvalContext;
import ai.manifesto.core.evaluator.ExprEvaluator;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.trace.TraceContext;
import ai.manifesto.core.utils.PathUtils;

import java.util.*;

/**
 * KR: Explain는 Core 실행 결과를 디버깅 가능한 설명 정보로 변환하는 도우미 타입입니다.
 * EN: Explain is a helper type that converts Core execution results into debuggable explanation data.
 */
public class Explain {

    private Explain() {
        // 정적 메서드만 제공
    }

    /**
     * 경로의 값을 설명한다
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 스냅샷
     * @param path 설명할 값의 경로 (예: "count", "computed.total")
     * @return 값, 추적, 의존성을 포함한 설명 결과
     */
    public static ExplainResult explain(
        DomainSchema schema,
        Snapshot snapshot,
        String path
    ) {
        Objects.requireNonNull(schema, "schema is required");
        Objects.requireNonNull(snapshot, "snapshot is required");
        Objects.requireNonNull(path, "path is required");

        TraceContext traceContext = TraceContext.create(snapshot.getMeta().getTimestamp());
        return explainWithTrace(schema, snapshot, path, traceContext);
    }

    /**
     * 트레이스를 함께 경로를 설명한다 (재귀)
     */
    private static ExplainResult explainWithTrace(
        DomainSchema schema,
        Snapshot snapshot,
        String path,
        TraceContext traceContext
    ) {
        // Computed 경로 확인
        if (path.startsWith("computed.")) {
            return explainComputed(schema, snapshot, path, traceContext);
        }

        // System 경로 확인
        if (path.startsWith("system.")) {
            String subPath = path.substring(7);  // "system." 제거
            Object value = PathUtils.getByPath(snapshot.getSystem(), subPath);
            TraceNode trace = TraceNode.builder()
                .id(traceContext.nextId())
                .kind(TraceNode.Kind.EXPR)
                .sourcePath(path)
                .inputs(Collections.singletonMap("path", path))
                .output(value)
                .children(new ArrayList<>())
                .timestamp(traceContext.getTimestamp())
                .build();
            return new ExplainResult(value, trace, new ArrayList<>());
        }

        // Input 경로 확인
        if (path.startsWith("input.") || path.equals("input")) {
            Object value;
            if (path.equals("input")) {
                value = snapshot.getInput();
            } else {
                String subPath = path.substring(6);  // "input." 제거
                value = PathUtils.getByPath(snapshot.getInput(), subPath);
            }
            TraceNode trace = TraceNode.builder()
                .id(traceContext.nextId())
                .kind(TraceNode.Kind.EXPR)
                .sourcePath(path)
                .inputs(Collections.singletonMap("path", path))
                .output(value)
                .children(new ArrayList<>())
                .timestamp(traceContext.getTimestamp())
                .build();
            return new ExplainResult(value, trace, new ArrayList<>());
        }

        // 기본: Data 경로
        Object value = PathUtils.getByPath(snapshot.getData(), path);
        TraceNode trace = TraceNode.builder()
            .id(traceContext.nextId())
            .kind(TraceNode.Kind.EXPR)
            .sourcePath(path)
            .inputs(Collections.singletonMap("path", path))
            .output(value)
            .children(new ArrayList<>())
            .timestamp(traceContext.getTimestamp())
            .build();
        return new ExplainResult(value, trace, new ArrayList<>());
    }

    /**
     * Computed 값을 설명한다
     */
    private static ExplainResult explainComputed(
        DomainSchema schema,
        Snapshot snapshot,
        String path,
        TraceContext traceContext
    ) {
        // Computed 필드 정의 조회 (path는 "computed.*" 형식)
        ComputedFieldDef spec = schema.getComputedFields().get(path);

        if (spec == null) {
            // Computed 필드 정의가 없으면 저장된 값만 반환
            Object value = null;
            Map<String, Object> computed = snapshot.getComputed();
            if (computed != null) {
                value = computed.get(path);
            }
            TraceNode trace = TraceNode.builder()
                .id(traceContext.nextId())
                .kind(TraceNode.Kind.COMPUTED)
                .sourcePath(path)
                .inputs(Collections.singletonMap("path", path))
                .output(value)
                .children(new ArrayList<>())
                .timestamp(traceContext.getTimestamp())
                .build();
            return new ExplainResult(value, trace, new ArrayList<>());
        }

        // 표현식을 평가하여 추적을 얻는다
        EvalContext ctx = EvalContext.builder()
            .snapshot(snapshot)
            .schema(schema)
            .currentAction(null)
            .nodePath(path)
            .intentId(null)
            .trace(traceContext)
            .uuidCounter(0)
            .build();
        Result<Object, ErrorValue> result = ExprEvaluator.evaluate(spec.getExpression(), ctx);

        Object value = result.isOk() ? result.unwrap() : null;

        // 의존성 정보를 포함한 추적 구성
        List<TraceNode> childTraces = new ArrayList<>();
        List<String> allDeps = new ArrayList<>();

        for (String dep : spec.getDependencies()) {
            ExplainResult depResult = explainWithTrace(schema, snapshot, dep, traceContext);
            childTraces.add(depResult.getTrace());
            allDeps.add(dep);
        }

        // Spec의 표현식을 저장
        Map<String, Object> exprInfo = new HashMap<>();
        exprInfo.put("expr", spec.getExpression().getClass().getSimpleName());
        exprInfo.put("deps", allDeps);

        TraceNode trace = TraceNode.builder()
            .id(traceContext.nextId())
            .kind(TraceNode.Kind.COMPUTED)
            .sourcePath(path)
            .inputs(exprInfo)
            .output(value)
            .children(childTraces)
            .timestamp(traceContext.getTimestamp())
            .build();

        return new ExplainResult(value, trace, allDeps);
    }
}
