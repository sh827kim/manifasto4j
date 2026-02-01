package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ManifestoCoreImpl - ManifestoCore 인터페이스의 구현체
 *
 * 싱글톤 패턴으로 구현:
 * - INSTANCE 정적 필드로 유일한 인스턴스 보증
 * - getInstance()로 접근
 *
 * 각 메서드는 해당 클래스에 위임:
 * - compute() → Compute.compute()
 * - apply() → Apply.apply()
 * - validate() → Validate.validateSchema()
 *
 * 특징:
 * - 순수 위임 (delegation pattern)
 * - 상태 없음 (stateless)
 * - 예외 처리 없음 (Result 모나드 사용)
 */
public final class ManifestoCoreImpl implements ManifestoCore {

    /**
     * 싱글톤 인스턴스
     */
    static final ManifestoCoreImpl INSTANCE = new ManifestoCoreImpl();

    /**
     * 외부에서 new 불가 (싱글톤)
     */
    private ManifestoCoreImpl() {
        // 싱글톤 생성자
    }

    /**
     * 비동기 계산 (Compute에 위임)
     */
    @Override
    public CompletableFuture<ComputeResult> compute(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent
    ) {
        return Compute.compute(schema, snapshot, intent);
    }

    /**
     * 동기식 compute (타임아웃 포함)
     */
    @Override
    public ComputeResult computeSync(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        int timeoutSeconds
    ) throws Exception {
        return Compute.computeSync(schema, snapshot, intent, timeoutSeconds);
    }

    /**
     * Patch 배열 적용 (Apply에 위임)
     */
    @Override
    public Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        List<Patch> patches
    ) {
        return Apply.apply(snapshot, patches);
    }

    /**
     * 단일 Patch 적용
     */
    @Override
    public Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch patch
    ) {
        return Apply.apply(snapshot, patch);
    }

    /**
     * 가변인자 Patch 적용
     */
    @Override
    public Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch... patches
    ) {
        return Apply.apply(snapshot, patches);
    }

    /**
     * Schema와 Snapshot 검증 (Validate에 위임)
     */
    @Override
    public Validate.ValidationResult validate(
        DomainSchema schema,
        Snapshot snapshot
    ) {
        return Validate.validateSchema(schema);
    }

    /**
     * 검증 여부 확인
     */
    @Override
    public boolean isValid(
        DomainSchema schema,
        Snapshot snapshot
    ) {
        return Validate.isValid(schema, snapshot);
    }

    /**
     * 검증 실패 시 첫 번째 에러
     */
    @Override
    public String getFirstError(
        DomainSchema schema,
        Snapshot snapshot
    ) {
        return Validate.getFirstError(schema, snapshot);
    }

    /**
     * toString 구현 (싱글톤 식별용)
     */
    @Override
    public String toString() {
        return "ManifestoCoreImpl(singleton)";
    }
}
