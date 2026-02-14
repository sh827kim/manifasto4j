# Cycle 7 Execution Tasks (2026-02-14)

Scope:
- Phase 7 (Compiler/Core 잔여 갭 제거)
- Phase 8 (통합 마감: 테스트/골든/문서)

Cycle Status:
- 완료

Cycle Goal:
- compiler loader/renderer edge-case와 core explain/validate parity 벡터를 보강하고, release readiness 문서를 최신 상태로 동기화한다.

## Task 1. Compiler API/Loader/Renderer 보강

### 1.1 Loader API
- [x] MEL 소스 로더(`file`/`classpath`) 추가
- [x] loader + compiler facade 연동 API 추가

### 1.2 Renderer Edge-case
- [x] renderer malformed input/unknown op/path edge-case 테스트 추가
- [x] renderer option(newline/indent/comment) 경계 테스트 추가

## Task 2. Core explain/validate parity 벡터 확장

### 2.1 Explain
- [x] computed/system/input/data 경로 explain 회귀 테스트 추가
- [x] computed 정의 미존재 fallback 경로 테스트 추가

### 2.2 Validate
- [x] validate edge-case parity 벡터 확장
- [x] golden/vector 테스트 보강

## Task 3. Release Readiness (Phase 8)

### 3.1 문서 동기화
- [x] README/INDEX/spec/fdr/parity/progress 문서 동기화
- [x] release 체크리스트 문서 추가

### 3.2 검증
- [x] `./gradlew :manifesto-compiler:test :manifesto-core:test` 통과
- [x] `./gradlew test` 통과
- [x] `./gradlew checkGoldenSync` 가능 여부 확인 및 결과 기록

## Verification Notes
- `./gradlew :manifesto-compiler:test :manifesto-core:test` 성공
- `./gradlew test` 성공
- `./gradlew checkGoldenSync` 성공 (N/A 처리)
  - TS 저장소에서 기존 compiler vector 위치(`packages/compiler/vectors` 등)가 더 이상 존재하지 않아 현재 baseline에서는 sync 대상이 없음을 명시

## Exit Criteria
1. compiler/core 잔여 edge-case 회귀 테스트가 추가된다.
2. release readiness 문서/체크리스트가 최신 코드 상태와 일치한다.
3. 전체 테스트/골든 검증 결과가 기록된다.
