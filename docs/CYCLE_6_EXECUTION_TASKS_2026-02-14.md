# Cycle 6 Execution Tasks (2026-02-14)

Scope:
- Phase 6 (Host/World 고도화)

Cycle Status:
- 완료 (2026-02-14)

Cycle Goal:
- Host의 context-provider/effect executor 경계를 보강하고, World의 event/query/persistence 계약 테스트를 확장해 TS shape 기준 안정성을 높인다.

## Task 1. Host context/effect 경계 보강

### 1.1 Context Provider
- [x] effect execution context 모델 추가
- [x] context provider 계약 + 기본 provider 추가
- [x] context-aware effect handler 계약 추가

### 1.2 Effect Executor
- [x] effect retry/timeout/error 분류 모델 추가
- [x] effect executor 도입 및 HostRuntime 통합
- [x] host error payload에 effect 실패 분류 정보 추가

## Task 2. Host trace invariant 회귀 강화

### 2.1 Trace 이벤트 확장
- [x] effect attempt/retry/success/failure trace 이벤트 추가

### 2.2 테스트
- [x] context-aware handler 테스트 추가
- [x] effect retry/failure trace 테스트 추가

## Task 3. World event/query/persistence 계약 확장

### 3.1 Event Query
- [x] in-memory world event journal 추가(type/time query)
- [x] world event journal 단위 테스트 추가

### 3.2 Persistence Query
- [x] `WorldStore.listProposalsByStatus` 계약 추가(default)
- [x] proposal status filter 테스트 추가

### 3.3 Integration
- [x] world + event journal + status query 통합 테스트 추가

## Task 4. 문서/리포트/검증

### 4.1 문서 업데이트
- [x] `docs/spec/spec-host.md` 업데이트
- [x] `docs/fdr/fdr-host.md` 업데이트
- [x] `docs/spec/spec-world.md` 업데이트
- [x] `docs/fdr/fdr-world.md` 업데이트
- [x] `docs/INDEX.md` Cycle 6 문서 반영

### 4.2 parity 리포트
- [x] `docs/TS_PARITY_MATRIX_2026-02-14.md` host/world 상태 갱신
- [x] `docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` 진행률 갱신

### 4.3 검증
- [x] `./gradlew :manifesto-host:test :manifesto-world:test` 통과
- [x] `./gradlew test` 통과

## Exit Criteria
1. Host effect 실행 경계(context/retry/error)가 타입/테스트로 고정된다.
2. World event/query/persistence의 필터/조회 계약이 테스트로 검증된다.
3. host/world parity 상태가 문서/리포트에 동기화된다.
