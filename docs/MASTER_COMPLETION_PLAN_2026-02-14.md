# Master Completion Plan & Actions (Unified, 2026-02-14)

이 문서는 기존 `MASTER_COMPLETION_PLAN` + `NEXT_ACTIONS`를 통합한 단일 실행 기준 문서입니다.

## 1. Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `517d3ac`
- 상세 전수 점검: `docs/TS_JAVA_FULL_AUDIT_2026-02-14.md`

## 2. Source-Validated Findings

1. App parity gap is still highest.
- TS app export surface is broad (`/workspace/manifasto-ts-core/packages/app/src/index.ts`).
- Java app is still facade-centric (`/workspace/manifesto-java-core/manifesto-app/src/main/java/ai/manifesto/app/App.java`, `/workspace/manifesto-java-core/manifesto-app/src/main/java/ai/manifesto/app/DefaultApp.java`).

2. Host compliance density is still low.
- TS host has large compliance/golden/unit suites (`/workspace/manifasto-ts-core/packages/host/src/__tests__`).
- Java host currently has small test surface (`/workspace/manifesto-java-core/manifesto-host/src/test/java`).

3. Host golden/vector standardization is needed.
- Java has only one host golden vector file (`/workspace/manifesto-java-core/manifesto-host/src/test/resources/golden/host-e2e.json`) and ad-hoc loader path in `HostGoldenTest`.
- Therefore “회귀 벡터 표준화”는 실제 소스 기준으로 유효한 작업 항목이다.

4. checkGoldenSync is in N/A policy mode.
- Current script behavior treats missing TS compiler vectors as N/A (`/workspace/manifesto-java-core/scripts/check-golden-sync.sh`).
- 운영 정책 문서화/자동 복구 절차는 계속 필요하다.

## 3. Priority Execution Order
1. P0-A App parity surface expansion
2. P0-B Host compliance hardening
3. P1-A Intent-IR semantic depth
4. P1-B Translator conformance depth
5. P1-C Codegen option materialization
6. P2-A World ingress/query hardening
7. P2-B Compiler auxiliary surface and sync policy
8. Final docs sync + full verification

## 4. Unified Action Checklist

### P0-A App Parity Surface Expansion
- [x] A1-1 app plugin/policy/world-store 경계 인터페이스 정식화
- [x] A1-2 schema compatibility/resume-recovery 에러 타입 정식화
- [x] A1-3 memory hub/backfill/recall 계약 반영
- [x] A1-4 app 회귀 테스트 확장(lifecycle/action/session/branch/hook/system/memory)
- [x] A1-5 app world-store/recovery/compatibility 회귀 테스트 확장

Validation:
- `./gradlew :manifesto-app:test`

### P0-B Host Compliance Hardening
- [x] A2-1 HostError taxonomy 정식화
- [x] A2-2 mailbox/runner/job ordering/liveness compliance 테스트 추가
- [x] A2-3 effect retry/timeout/failure/trace invariant 테스트 추가
- [x] A2-4 host-owned namespace consistency 테스트 추가
- [x] A2-5 host golden/vector harness 표준화 (fixture/loader/assertion 규약 통일)

Validation:
- `./gradlew :manifesto-host:test`

### P1-A Intent-IR Semantic Depth
- [x] A3-1 schema validator rule/code 세분화
- [x] A3-2 lexicon feature(role/theta/selectional) 정책 강화
- [x] A3-3 resolver discourse/focus 규칙 강화
- [x] A3-4 lower edge-case 회귀 벡터 확장

Validation:
- `./gradlew :manifesto-intent-ir:test`

### P1-B Translator Conformance Depth
- [x] A4-1 plugin family(coverage/dependency/or/task-enumeration 계열) 보강
- [x] A4-2 그래프 invariant/diagnostics/parallel conformance 테스트 확장
- [x] A4-3 manifesto exporter lowering failure taxonomy 정밀화

Validation:
- `./gradlew :manifesto-translator:test`

### P1-C Codegen Option Materialization
- [x] A5-1 naming/nullability/style 옵션 실반영 구현
- [x] A5-2 옵션 조합 snapshot 회귀 테스트 확장

Validation:
- `./gradlew :manifesto-codegen:test`

### P2-A World Ingress/Query Hardening
- [x] A6-1 ingress context/epoch 계약 강화
- [x] A6-2 query filter/sort/limit 계약 강화
- [x] A6-3 world governance/query 회귀 테스트 보강

Validation:
- `./gradlew :manifesto-world:test`

### P2-B Compiler Auxiliary Surface
- [x] A7-1 cli/formatter 지원 범위 재정의 및 보강
- [x] A7-2 checkGoldenSync N/A 정책 문서화 고도화
- [x] A7-3 TS vector 재도입 시 sync 복구 절차 자동화

Validation:
- `./gradlew :manifesto-compiler:test`
- `./gradlew checkGoldenSync`

## 5. Completion Gate
- [x] `./gradlew test`
- [x] `./gradlew checkGoldenSync`
- [x] 문서 동기화(`docs/README.md`, `docs/INDEX.md`, `docs/MASTER_COMPLETION_PLAN_2026-02-14.md`, `docs/TS_JAVA_FULL_AUDIT_2026-02-14.md`)

## 6. Global DoD
1. TS 공개 계약의 핵심 runtime/API 의미를 Java에서 재현한다.
2. 모듈별 핵심/경계 시나리오 회귀 테스트가 존재한다.
3. 통합 테스트와 골든 검증이 안정 통과한다.
4. 문서가 코드 상태와 일치한다.

## 7. Operating Rules
1. 상위 우선순위(P0 -> P1 -> P2) 완료 전 다음 우선순위로 이동하지 않는다.
2. 기능 변경은 테스트와 같은 작업 사이클에서 반영한다.
3. 각 phase 종료 시 문서를 즉시 동기화한다.
