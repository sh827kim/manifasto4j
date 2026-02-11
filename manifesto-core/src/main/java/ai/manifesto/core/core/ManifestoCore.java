package ai.manifesto.core.core;

import ai.manifesto.core.*;
import ai.manifesto.core.schema.DomainSchema;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * KR: ManifestoCore는 validate/compute/apply/explain 동작을 제공하는 Core 공개 API 인터페이스입니다.
 * EN: ManifestoCore is the public Core API interface providing validate/compute/apply/explain operations.
 */
public interface ManifestoCore {

    /**
     * 비동기 계산 (상태 전환)
     *
     * 10단계 흐름:
     * 1. Computed 필드 평가 (DAG 순서)
     * 2. Schema에서 Action 조회
     * 3. IntentId 검증
     * 4. Input 검증
     * 5. Action available 조건 평가
     * 6. Snapshot input 설정
     * 7. 평가 컨텍스트 생성
     * 8. Flow 평가
     * 9. Computed 재계산
     * 10. System 상태 업데이트
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 상태
     * @param intent 실행할 액션
     * @return 계산 결과 (비동기)
     */
    CompletableFuture<ComputeResult> compute(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent
    );

    /**
     * 동기식 compute (테스트용)
     *
     * compute()의 결과를 타임아웃 내에 기다린다.
     *
     * @param schema 도메인 스키마
     * @param snapshot 현재 상태
     * @param intent 실행할 액션
     * @param timeoutSeconds 타임아웃 (초)
     * @return 계산 결과
     * @throws Exception 타임아웃 또는 계산 실패
     */
    ComputeResult computeSync(
        DomainSchema schema,
        Snapshot snapshot,
        Intent intent,
        int timeoutSeconds
    ) throws Exception;

    /**
     * Patch 배열 적용
     *
     * Snapshot에 일련의 Patch를 순차적으로 적용한다.
     * 원본 Snapshot은 변경되지 않는다 (불변성 보증).
     *
     * @param snapshot 원본 상태
     * @param patches 적용할 Patch 배열
     * @return 변경된 새로운 Snapshot 또는 에러
     */
    Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        List<Patch> patches
    );

    /**
     * 단일 Patch 적용
     *
     * @param snapshot 원본 상태
     * @param patch 적용할 Patch
     * @return 변경된 Snapshot 또는 에러
     */
    Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch patch
    );

    /**
     * 여러 Patch 적용 (가변인자)
     *
     * @param snapshot 원본 상태
     * @param patches 적용할 Patch들
     * @return 변경된 Snapshot 또는 에러
     */
    Result<Snapshot, ErrorValue> apply(
        Snapshot snapshot,
        Patch... patches
    );

    /**
     * Schema와 Snapshot 검증
     *
     * Snapshot의 구조와 내용이 Schema와 일치하는지 검증한다.
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 검증 결과 (isValid + 에러 목록)
     */
    Validate.ValidationResult validate(
        DomainSchema schema,
        Snapshot snapshot
    );

    /**
     * 검증 여부 확인
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 검증 성공 여부
     */
    boolean isValid(
        DomainSchema schema,
        Snapshot snapshot
    );

    /**
     * 검증 실패 시 첫 번째 에러 반환
     *
     * @param schema 도메인 스키마
     * @param snapshot 검증할 Snapshot
     * @return 첫 번째 에러 또는 null (검증 성공)
     */
    String getFirstError(
        DomainSchema schema,
        Snapshot snapshot
    );

    /**
     * 기본 구현체 반환
     *
     * @return ManifestoCoreImpl 인스턴스
     */
    static ManifestoCore getInstance() {
        return ManifestoCoreImpl.INSTANCE;
    }
}
