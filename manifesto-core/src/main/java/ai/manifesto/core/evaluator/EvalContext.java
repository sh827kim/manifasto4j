package ai.manifesto.core.evaluator;

import ai.manifesto.core.Snapshot;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.trace.TraceContext;
import ai.manifesto.core.utils.UuidUtils;

import java.util.List;
import java.util.Objects;

/**
 * EvalContext - 표현식과 Flow 평가 시 필요한 모든 컨텍스트 정보
 *
 * 평가 중에 참조되는 불변 데이터:
 * - snapshot: 현재 상태
 * - schema: 도메인 스키마
 * - currentAction: 현재 액션명
 * - nodePath: 현재 노드의 경로 (추적용)
 * - intentId: Intent 식별자 (재진입 안전성, 결정론적 UUID용)
 * - trace: 추적 컨텍스트
 *
 * 컬렉션 컨텍스트 (filter/map/find 등에서):
 * - $item: 현재 항목
 * - $index: 현재 인덱스
 * - $array: 전체 배열
 * - $acc: reduce 누적값
 *
 * 결정론적 UUID 생성:
 * - uuidCounter: mutable 카운터 (결정론성을 위해 의도적으로 mutable)
 * - nextUuid()로 다음 UUID 생성
 */
public class EvalContext {
    private final Snapshot snapshot;
    private final DomainSchema schema;
    private final String currentAction; // nullable
    private final String nodePath;
    private final String intentId; // nullable
    private int uuidCounter; // mutable: 결정론적 UUID 생성용
    private final TraceContext trace;

    // 컬렉션 컨텍스트
    private final Object $item; // nullable
    private final Integer $index; // nullable
    private final List<?> $array; // nullable
    private final Object $acc; // nullable

    private EvalContext(Builder builder) {
        this.snapshot = Objects.requireNonNull(builder.snapshot, "snapshot is required");
        this.schema = Objects.requireNonNull(builder.schema, "schema is required");
        this.currentAction = builder.currentAction;
        this.nodePath = Objects.requireNonNull(builder.nodePath, "nodePath is required");
        this.intentId = builder.intentId;
        this.uuidCounter = builder.uuidCounter;
        this.trace = Objects.requireNonNull(builder.trace, "trace is required");
        this.$item = builder.$item;
        this.$index = builder.$index;
        this.$array = builder.$array;
        this.$acc = builder.$acc;
    }

    // ===== Getters =====

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public DomainSchema getSchema() {
        return schema;
    }

    public String getCurrentAction() {
        return currentAction;
    }

    public String getNodePath() {
        return nodePath;
    }

    public String getIntentId() {
        return intentId;
    }

    public TraceContext getTrace() {
        return trace;
    }

    public Object get$item() {
        return $item;
    }

    public Integer get$index() {
        return $index;
    }

    public List<?> get$array() {
        return $array;
    }

    public Object get$acc() {
        return $acc;
    }

    // ===== Copy-on-Write 패턴 =====

    /**
     * Snapshot을 변경한 새로운 컨텍스트 생성
     */
    public EvalContext withSnapshot(Snapshot snapshot) {
        return new Builder(this).snapshot(snapshot).build();
    }

    /**
     * nodePath를 변경한 새로운 컨텍스트 생성
     */
    public EvalContext withNodePath(String nodePath) {
        return new Builder(this).nodePath(nodePath).build();
    }

    /**
     * 컬렉션 컨텍스트 설정 (filter/map/find 등에서 사용)
     */
    public EvalContext withCollectionContext(Object item, int index, List<?> array) {
        return new Builder(this)
            .$item(item)
            .$index(index)
            .$array(array)
            .build();
    }

    /**
     * reduce 컨텍스트 설정 ($acc 포함)
     */
    public EvalContext withReduceContext(Object acc, Object item, int index, List<?> array) {
        return new Builder(this)
            .$acc(acc)
            .$item(item)
            .$index(index)
            .$array(array)
            .build();
    }

    /**
     * 컬렉션 컨텍스트 제거
     */
    public EvalContext clearCollectionContext() {
        return new Builder(this)
            .$item(null)
            .$index(null)
            .$array(null)
            .$acc(null)
            .build();
    }

    // ===== 결정론적 UUID 생성 =====

    /**
     * 다음 UUID 생성 (결정론적)
     * 같은 intentId + counter -> 같은 UUID 보장
     *
     * 주: 이 메서드는 uuidCounter를 증가시키므로
     * 호출할 때마다 다른 UUID를 생성한다.
     * 같은 위치에서 여러 번 호출하면 매번 다른 UUID를 받는다.
     */
    public String nextUuid() {
        String uuid = UuidUtils.generateDeterministic(intentId != null ? intentId : "", uuidCounter);
        uuidCounter++;
        return uuid;
    }

    /**
     * 현재 UUID 카운터 값 조회
     */
    public int getUuidCounter() {
        return uuidCounter;
    }

    // ===== Builder 패턴 =====

    /**
     * 새로운 Builder 생성
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder - 메서드 체이닝으로 EvalContext 생성
     */
    public static class Builder {
        private Snapshot snapshot;
        private DomainSchema schema;
        private String currentAction;
        private String nodePath;
        private String intentId;
        private int uuidCounter;
        private TraceContext trace;
        private Object $item;
        private Integer $index;
        private List<?> $array;
        private Object $acc;

        public Builder() {
        }

        /**
         * 복사 생성자 - 기존 컨텍스트에서 복사
         */
        public Builder(EvalContext ctx) {
            this.snapshot = ctx.snapshot;
            this.schema = ctx.schema;
            this.currentAction = ctx.currentAction;
            this.nodePath = ctx.nodePath;
            this.intentId = ctx.intentId;
            this.uuidCounter = ctx.uuidCounter;
            this.trace = ctx.trace;
            this.$item = ctx.$item;
            this.$index = ctx.$index;
            this.$array = ctx.$array;
            this.$acc = ctx.$acc;
        }

        public Builder snapshot(Snapshot snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder schema(DomainSchema schema) {
            this.schema = schema;
            return this;
        }

        public Builder currentAction(String currentAction) {
            this.currentAction = currentAction;
            return this;
        }

        public Builder nodePath(String nodePath) {
            this.nodePath = nodePath;
            return this;
        }

        public Builder intentId(String intentId) {
            this.intentId = intentId;
            return this;
        }

        public Builder uuidCounter(int uuidCounter) {
            this.uuidCounter = uuidCounter;
            return this;
        }

        public Builder trace(TraceContext trace) {
            this.trace = trace;
            return this;
        }

        public Builder $item(Object item) {
            this.$item = item;
            return this;
        }

        public Builder $index(Integer index) {
            this.$index = index;
            return this;
        }

        public Builder $array(List<?> array) {
            this.$array = array;
            return this;
        }

        public Builder $acc(Object acc) {
            this.$acc = acc;
            return this;
        }

        public EvalContext build() {
            return new EvalContext(this);
        }
    }

    /**
     * 팩토리 메서드 - 새로운 평가 컨텍스트 생성
     */
    public static EvalContext create(
        Snapshot snapshot,
        DomainSchema schema,
        String currentAction,
        String nodePath,
        String intentId,
        long timestamp) {

        return builder()
            .snapshot(snapshot)
            .schema(schema)
            .currentAction(currentAction)
            .nodePath(nodePath)
            .intentId(intentId)
            .trace(TraceContext.create(timestamp))
            .uuidCounter(0)
            .build();
    }
}
