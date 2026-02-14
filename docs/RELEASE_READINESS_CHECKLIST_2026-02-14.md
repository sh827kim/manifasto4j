# Release Readiness Checklist (2026-02-14)

## Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` (current working tree)

## Cycle 7 Scope
- compiler loader/renderer edge-case 보강
- core explain/validate parity vector 보강
- 문서/리포트 동기화

## Checklist
- [x] `:manifesto-compiler:test` 통과
- [x] `:manifesto-core:test` 통과
- [x] 전체 `./gradlew test` 통과
- [x] `./gradlew checkGoldenSync` 통과 (N/A 허용)

## checkGoldenSync 상태
- 실행일: `2026-02-14`
- 결과: 성공 (N/A 처리)
- 처리 원인: TS 측 compiler vector 경로가 기존 스크립트 후보 경로에 존재하지 않아 sync 대상이 없는 baseline으로 판정
  - 확인한 경로 후보:
    - `packages/compiler/vectors`
    - `packages/compiler/src/__tests__/vectors`
    - `packages/compiler/test/vectors`
    - `packages/compiler/tests/vectors`
    - `packages/compiler/__tests__/vectors`

## Residual Actions
1. TS 측에서 vector/golden artifact가 재도입되면 경로 후보를 즉시 갱신
2. N/A 모드 사용 기준을 문서화하고 릴리즈 게이트 정책에 반영
