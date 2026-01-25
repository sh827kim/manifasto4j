package ai.manifesto.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ComputeResult - compute() 함수의 결과
 *
 * compute() 함수가 반환하는 완전한 결과 객체다.
 * 다음을 포함한다:
 * - snapshot: 계산 후의 새로운 상태
 * - requirements: Host가 처리해야 할 효과 목록
 * - trace: 계산 추적 (디버깅, 설명 가능성)
 * - status: 계산 결과 상태
 */
public class ComputeResult {

    private final Snapshot snapshot;                // 새로운 상태
    private final List<Requirement> requirements;  // 펼딩된 효과
    private final TraceGraph trace;                // 계산 추적
    private final ComputeStatus status;            // 실행 결과 상태

    /**
     * 생성자
     */
    private ComputeResult(Snapshot snapshot, List<Requirement> requirements,
                         TraceGraph trace, ComputeStatus status) {
        this.snapshot = Objects.requireNonNull(snapshot);
        this.requirements = new ArrayList<>(requirements != null ?
            requirements : new ArrayList<>());
        this.trace = trace;
        this.status = Objects.requireNonNull(status);
    }

    // Getters
    public Snapshot getSnapshot() { return snapshot; }
    public List<Requirement> getRequirements() { return new ArrayList<>(requirements); }
    public TraceGraph getTrace() { return trace; }
    public ComputeStatus getStatus() { return status; }

    /**
     * 상태 확인 헬퍼 메서드
     */
    public boolean isComplete() { return status.isComplete(); }
    public boolean isPending() { return status.isPending(); }
    public boolean isHalted() { return status.isHalted(); }
    public boolean isError() { return status.isError(); }

    /**
     * 빌더
     */
    public static class Builder {
        private Snapshot snapshot;
        private List<Requirement> requirements = new ArrayList<>();
        private TraceGraph trace;
        private ComputeStatus status = ComputeStatus.COMPLETE;

        public Builder snapshot(Snapshot snapshot) {
            this.snapshot = snapshot;
            return this;
        }

        public Builder requirements(List<Requirement> requirements) {
            this.requirements = new ArrayList<>(requirements);
            return this;
        }

        public Builder addRequirement(Requirement requirement) {
            this.requirements.add(requirement);
            return this;
        }

        public Builder trace(TraceGraph trace) {
            this.trace = trace;
            return this;
        }

        public Builder status(ComputeStatus status) {
            this.status = status;
            return this;
        }

        public ComputeResult build() {
            return new ComputeResult(snapshot, requirements, trace, status);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 완료 결과 생성
     */
    public static ComputeResult complete(Snapshot snapshot, TraceGraph trace) {
        return new ComputeResult(snapshot, new ArrayList<>(), trace,
            ComputeStatus.COMPLETE);
    }

    /**
     * 펼딩 결과 생성
     */
    public static ComputeResult pending(Snapshot snapshot,
                                        List<Requirement> requirements,
                                        TraceGraph trace) {
        return new ComputeResult(snapshot, requirements, trace,
            ComputeStatus.PENDING);
    }

    /**
     * 중단 결과 생성
     */
    public static ComputeResult halted(Snapshot snapshot, TraceGraph trace) {
        return new ComputeResult(snapshot, new ArrayList<>(), trace,
            ComputeStatus.HALTED);
    }

    /**
     * 에러 결과 생성
     */
    public static ComputeResult error(Snapshot snapshot, TraceGraph trace) {
        return new ComputeResult(snapshot, new ArrayList<>(), trace,
            ComputeStatus.ERROR);
    }

    @Override
    public String toString() {
        return "ComputeResult{" +
               "status=" + status +
               ", snapshotVersion=" + snapshot.getMeta().getVersion() +
               ", requirements=" + requirements.size() +
               ", trace=" + (trace != null ? "present" : "null") +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComputeResult that)) return false;
        return Objects.equals(snapshot, that.snapshot) &&
               Objects.equals(requirements, that.requirements) &&
               Objects.equals(trace, that.trace) &&
               status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(snapshot, requirements, trace, status);
    }
}
