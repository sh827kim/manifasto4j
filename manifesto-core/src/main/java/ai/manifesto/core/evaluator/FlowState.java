package ai.manifesto.core.evaluator;

import ai.manifesto.core.ErrorValue;
import ai.manifesto.core.Patch;
import ai.manifesto.core.Requirement;
import ai.manifesto.core.Snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * KR: FlowState는 Core 평가 파이프라인에서 flow state 역할을 수행하는 구현 타입입니다.
 * EN: FlowState is an implementation type performing flow state roles in the Core evaluation pipeline.
 */
public class FlowState {
    private final Snapshot snapshot;
    private final FlowStatus status;
    private final List<Patch> patches;
    private final List<Requirement> requirements;
    private final ErrorValue error; // nullable

    private FlowState(
        Snapshot snapshot,
        FlowStatus status,
        List<Patch> patches,
        List<Requirement> requirements,
        ErrorValue error) {

        this.snapshot = Objects.requireNonNull(snapshot, "snapshot is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.patches = List.copyOf(patches != null ? patches : List.of());
        this.requirements = List.copyOf(requirements != null ? requirements : List.of());
        this.error = error;
    }

    // ===== Getters =====

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public List<Patch> getPatches() {
        return patches;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public ErrorValue getError() {
        return error;
    }

    // ===== 상태 확인 헬퍼 =====

    public boolean isRunning() {
        return status == FlowStatus.RUNNING;
    }

    public boolean isTerminated() {
        return status != FlowStatus.RUNNING;
    }

    public boolean hasError() {
        return error != null;
    }

    public boolean hasPendingRequirements() {
        return !requirements.isEmpty();
    }

    // ===== Copy-on-Write 패턴 =====

    /**
     * 초기 상태 생성 (Snapshot만으로)
     */
    public static FlowState initial(Snapshot snapshot) {
        return new FlowState(snapshot, FlowStatus.RUNNING, List.of(), List.of(), null);
    }

    /**
     * Snapshot을 변경한 새로운 상태 생성
     */
    public FlowState withSnapshot(Snapshot snapshot) {
        return new FlowState(snapshot, status, patches, requirements, error);
    }

    /**
     * 상태를 변경한 새로운 상태 생성
     */
    public FlowState withStatus(FlowStatus status) {
        return new FlowState(snapshot, status, patches, requirements, error);
    }

    /**
     * Patch를 추가한 새로운 상태 생성
     */
    public FlowState addPatch(Patch patch) {
        List<Patch> newPatches = new ArrayList<>(patches);
        newPatches.add(patch);
        return new FlowState(snapshot, status, newPatches, requirements, error);
    }

    /**
     * 여러 Patch를 추가한 새로운 상태 생성
     */
    public FlowState addPatches(List<Patch> newPatches) {
        List<Patch> combined = new ArrayList<>(patches);
        combined.addAll(newPatches);
        return new FlowState(snapshot, status, combined, requirements, error);
    }

    /**
     * Requirement를 추가한 새로운 상태 생성
     * 상태가 PENDING으로 변경됨
     */
    public FlowState addRequirement(Requirement requirement) {
        List<Requirement> newRequirements = new ArrayList<>(requirements);
        newRequirements.add(requirement);
        return new FlowState(snapshot, FlowStatus.PENDING, patches, newRequirements, error);
    }

    /**
     * 여러 Requirement를 추가한 새로운 상태 생성
     * 상태가 PENDING으로 변경됨
     */
    public FlowState addRequirements(List<Requirement> newRequirements) {
        List<Requirement> combined = new ArrayList<>(requirements);
        combined.addAll(newRequirements);
        return new FlowState(snapshot, FlowStatus.PENDING, patches, combined, error);
    }

    /**
     * 에러를 설정한 새로운 상태 생성
     * 상태가 ERROR로 변경됨
     */
    public FlowState withError(ErrorValue error) {
        return new FlowState(snapshot, FlowStatus.ERROR, patches, requirements, error);
    }

    /**
     * 완료 상태로 변경한 새로운 상태 생성
     */
    public FlowState complete() {
        return new FlowState(snapshot, FlowStatus.COMPLETE, patches, requirements, error);
    }

    /**
     * 중단 상태로 변경한 새로운 상태 생성
     */
    public FlowState halt() {
        return new FlowState(snapshot, FlowStatus.HALTED, patches, requirements, error);
    }

    @Override
    public String toString() {
        return "FlowState{" +
               "status=" + status +
               ", patches=" + patches.size() +
               ", requirements=" + requirements.size() +
               ", error=" + (error != null ? error.getCode() : "none") +
               '}';
    }
}
