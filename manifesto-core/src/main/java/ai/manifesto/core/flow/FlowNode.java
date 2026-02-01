package ai.manifesto.core.flow;

import ai.manifesto.core.expr.ExprNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * FlowNode - 액션의 실행 흐름을 정의하는 노드
 *
 * Flow는 Manifesto의 실행 단위다.
 * 각 FlowNode는 다음 중 하나를 나타낸다:
 * - Seq: 순차 실행
 * - If: 조건부 실행
 * - Patch: 상태 변경
 * - Effect: 외부 작업 요청
 * - Call: 다른 Flow 호출
 * - Halt: 정상 중단
 * - Fail: 에러로 중단
 *
 * 예: seq(
 *   patch("count").set(0),
 *   if(get("input.title") == "")
 *     fail("VALIDATION_ERROR")
 *   else
 *     effect("api.save", { title: get("input.title") })
 * )
 */
public sealed class FlowNode {

    // ===== Seq - 순차 실행 =====

    /**
     * Seq - 여러 FlowNode를 순차적으로 실행
     */
    public static final class Seq extends FlowNode {
        private final List<FlowNode> steps;

        public Seq(List<FlowNode> steps) {
            Objects.requireNonNull(steps, "steps is required");
            this.steps = List.copyOf(steps);
        }

        public List<FlowNode> getSteps() {
            return steps;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Seq seq)) return false;
            return Objects.equals(steps, seq.steps);
        }

        @Override
        public int hashCode() {
            return Objects.hash(steps);
        }

        @Override
        public String toString() {
            return "Seq{steps=" + steps.size() + "}";
        }

        // 정적 헬퍼 메서드
        public static Seq of(FlowNode... steps) {
            return new Seq(Arrays.asList(steps));
        }

        public static Seq of(List<FlowNode> steps) {
            return new Seq(steps);
        }
    }

    // ===== If - 조건부 실행 =====

    /**
     * If - 조건에 따라 다른 Flow 실행
     */
    public static final class If extends FlowNode {
        private final ExprNode cond;
        private final FlowNode thenBranch;
        private final FlowNode elseBranch; // nullable

        public If(ExprNode cond, FlowNode thenBranch, FlowNode elseBranch) {
            Objects.requireNonNull(cond, "cond is required");
            Objects.requireNonNull(thenBranch, "thenBranch is required");
            this.cond = cond;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public ExprNode getCond() {
            return cond;
        }

        public FlowNode getThenBranch() {
            return thenBranch;
        }

        public FlowNode getElseBranch() {
            return elseBranch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof If ifFlow)) return false;
            return Objects.equals(cond, ifFlow.cond) &&
                   Objects.equals(thenBranch, ifFlow.thenBranch) &&
                   Objects.equals(elseBranch, ifFlow.elseBranch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cond, thenBranch, elseBranch);
        }

        @Override
        public String toString() {
            return "If{cond=" + cond + "}";
        }

        // 정적 헬퍼 메서드
        public static If of(ExprNode cond, FlowNode thenBranch, FlowNode elseBranch) {
            return new If(cond, thenBranch, elseBranch);
        }

        public static If of(ExprNode cond, FlowNode thenBranch) {
            return new If(cond, thenBranch, null);
        }
    }

    // ===== Patch - 상태 변경 선언 =====

    /**
     * Patch - Snapshot 상태를 변경
     */
    public static final class Patch extends FlowNode {
        private final PatchOp op;
        private final String path;
        private final ExprNode value; // nullable (unset에서는 null)

        public Patch(PatchOp op, String path, ExprNode value) {
            Objects.requireNonNull(op, "op is required");
            Objects.requireNonNull(path, "path is required");
            this.op = op;
            this.path = path;
            this.value = value;
        }

        public PatchOp getOp() {
            return op;
        }

        public String getPath() {
            return path;
        }

        public ExprNode getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Patch patch)) return false;
            return op == patch.op &&
                   Objects.equals(path, patch.path) &&
                   Objects.equals(value, patch.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(op, path, value);
        }

        @Override
        public String toString() {
            return "Patch{op=" + op + ", path='" + path + "'}";
        }

        // 정적 헬퍼 메서드
        public static Patch set(String path, ExprNode value) {
            Objects.requireNonNull(value, "value is required for set operation");
            return new Patch(PatchOp.SET, path, value);
        }

        public static Patch unset(String path) {
            return new Patch(PatchOp.UNSET, path, null);
        }

        public static Patch merge(String path, ExprNode value) {
            Objects.requireNonNull(value, "value is required for merge operation");
            return new Patch(PatchOp.MERGE, path, value);
        }
    }

    // ===== Effect - 외부 작업 요청 =====

    /**
     * Effect - Host에게 외부 작업 수행 요청
     * Effect는 선언만 하고 실행하지 않는다.
     * Host가 Requirement를 받아서 실제로 실행한다.
     */
    public static final class Effect extends FlowNode {
        private final String type;
        private final Map<String, ExprNode> params;

        public Effect(String type, Map<String, ExprNode> params) {
            Objects.requireNonNull(type, "type is required");
            Objects.requireNonNull(params, "params is required");
            this.type = type;
            this.params = Map.copyOf(params);
        }

        public String getType() {
            return type;
        }

        public Map<String, ExprNode> getParams() {
            return params;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Effect effect)) return false;
            return Objects.equals(type, effect.type) &&
                   Objects.equals(params, effect.params);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, params);
        }

        @Override
        public String toString() {
            return "Effect{type='" + type + "', params=" + params.size() + "}";
        }

        // 정적 헬퍼 메서드
        public static Effect of(String type, Map<String, ExprNode> params) {
            return new Effect(type, params);
        }
    }

    // ===== Call - Flow 호출 =====

    /**
     * Call - 다른 Flow를 호출 (현재 Schema에 정의된 Flow)
     */
    public static final class Call extends FlowNode {
        private final String flow;

        public Call(String flow) {
            Objects.requireNonNull(flow, "flow is required");
            this.flow = flow;
        }

        public String getFlow() {
            return flow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Call call)) return false;
            return Objects.equals(flow, call.flow);
        }

        @Override
        public int hashCode() {
            return Objects.hash(flow);
        }

        @Override
        public String toString() {
            return "Call{flow='" + flow + "'}";
        }

        // 정적 헬퍼 메서드
        public static Call of(String flow) {
            return new Call(flow);
        }
    }

    // ===== Halt - 정상 중단 =====

    /**
     * Halt - Flow를 정상적으로 중단
     */
    public static final class Halt extends FlowNode {
        private final String reason; // nullable

        public Halt(String reason) {
            this.reason = reason;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Halt halt)) return false;
            return Objects.equals(reason, halt.reason);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reason);
        }

        @Override
        public String toString() {
            return "Halt{reason='" + reason + "'}";
        }

        // 정적 헬퍼 메서드
        public static Halt of(String reason) {
            return new Halt(reason);
        }

        public static Halt of() {
            return new Halt(null);
        }
    }

    // ===== Fail - 에러로 중단 =====

    /**
     * Fail - 에러와 함께 Flow를 중단
     */
    public static final class Fail extends FlowNode {
        private final String code;
        private final ExprNode message; // nullable

        public Fail(String code, ExprNode message) {
            Objects.requireNonNull(code, "code is required");
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public ExprNode getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Fail fail)) return false;
            return Objects.equals(code, fail.code) &&
                   Objects.equals(message, fail.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, message);
        }

        @Override
        public String toString() {
            return "Fail{code='" + code + "'}";
        }

        // 정적 헬퍼 메서드
        public static Fail of(String code, ExprNode message) {
            return new Fail(code, message);
        }

        public static Fail of(String code) {
            return new Fail(code, null);
        }
    }
}
