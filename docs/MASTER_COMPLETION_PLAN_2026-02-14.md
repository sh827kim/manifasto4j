# Master Completion Plan & Actions (Unified, 2026-02-14)

이 문서는 기존 `MASTER_COMPLETION_PLAN` + `NEXT_ACTIONS`를 통합한 단일 실행 기준 문서입니다.

## 1. Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `1245a5b`
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

## 8. Post-Gate Next Cycle
- Completion Gate 통과 이후의 패키지별 재점검/실행 Task의 단일 기준 문서는
  본 문서(`docs/MASTER_COMPLETION_PLAN_2026-02-14.md`)이다.
- `docs/PACKAGE_GAP_ANALYSIS_2026-02-14.md`는 근거/분석 참고 문서로만 사용한다.

## 9. Post-Gate Gap Execution Plan (Package-Based)

### 9.1 Priority Order
1. `TASK-A1` app contract parity map + 누락 API/오류/옵션 계약 반영
2. `TASK-A2` app 회귀 테스트 확장
3. `TASK-A3` host compliance suite 확장
4. `TASK-A4` host golden scenario 표준화
5. `TASK-B1` world persistence/query/event parity
6. `TASK-B2` intent-ir edge-case 회귀 확장
7. `TASK-B3` translator conformance matrix + adapter SPI 경계 보강
8. `TASK-B4` codegen multi-plugin sequential mode 결정/PoC
9. `TASK-C1` compiler CLI entrypoint + strict golden lane
10. `TASK-C2` core/app/host/world 통합 회귀 안정화

### 9.2 Stage A (P0)

#### `TASK-A1` App Surface Parity (contract first)
- [x] TS `packages/app/src/index.ts` export를 기준으로 Java app 공개 계약 parity map 작성
- [x] Java app 누락 error/options/interface 보강 (`manifesto-app/src/main/java/ai/manifesto/app`)
- [x] API 추가 시 기존 동작 호환성 유지

Validation:
- `./gradlew :manifesto-app:test`

#### `TASK-A2` App Regression Expansion
- [x] lifecycle/action/session/branch/policy/memory/resume-recovery 시나리오를 TS 테스트 기준으로 매핑
- [x] Java app 테스트 밀도를 TS 핵심 시나리오 기준으로 확장

Validation:
- `./gradlew :manifesto-app:test`

#### `TASK-A3` Host Compliance Suite Expansion
- [x] mailbox/job/runner/ordering/liveness/handler/context 축으로 compliance 테스트 추가
- [x] host-owned namespace 및 effect reinjection/fulfill 경계 검증 강화

Validation:
- `./gradlew :manifesto-host:test`

#### `TASK-A4` Host Golden Scenario Standardization
- [x] determinism/trace-snapshot/complex-effects/todo-workflow 시나리오를 golden fixture로 정규화
- [x] golden loader/assertion 규약 통일

Validation:
- `./gradlew :manifesto-host:test`

### 9.3 Stage B (P1)

#### `TASK-B1` World Persistence Parity
- [x] `EdgeQuery` 필터 계약 추가
- [x] observable store event subscription 계약 추가
- [x] stats 조회 계약 추가

Validation:
- `./gradlew :manifesto-world:test`

#### `TASK-B2` Intent-IR Edge-Case Regression
- [x] resolver/lexicon/lower TS 회귀 케이스 매핑 문서화
- [x] Java intent-ir edge-case 테스트 벡터 확장

Validation:
- `./gradlew :manifesto-intent-ir:test`

#### `TASK-B3` Translator Conformance and Adapter Boundary
- [x] TS conformance suite 항목별 Java 대응 매트릭스 작성
- [x] adapter SPI/transport 경계 보강(프레임워크 비종속 유지)

Validation:
- `./gradlew :manifesto-translator:test`

#### `TASK-B4` Codegen Multi-Plugin Mode Decision
- [x] Java codegen에 TS형 sequential multi-plugin mode 도입 여부 결정 문서화
- [x] 채택 시 PoC, 미채택 시 근거와 대체전략 명시

Validation:
- `./gradlew :manifesto-codegen:test`

### 9.4 Stage C (P2)

#### `TASK-C1` Compiler CLI Operationalization
- [x] compile/format/check CLI entrypoint 제공
- [x] `checkGoldenSync` strict lane(CI용) 실행 경로 문서화

Validation:
- `./gradlew :manifesto-compiler:test`
- `./gradlew checkGoldenSync`

#### `TASK-C2` Cross-Module Integration Regression
- [x] core/app/host/world 통합 회귀 시나리오 정리
- [x] 교차 모듈 회귀 테스트 안정화

Validation:
- `./gradlew test`

## 10. Next Cycle Plan (Post-C2 Rebaseline, 2026-02-14)

### 10.1 Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `a5f951b`
- 상세 갭 근거: `docs/PACKAGE_GAP_ANALYSIS_2026-02-14.md`

### 10.2 Priority Execution Order
1. `TASK-D1` App SDK surface parity expansion
2. `TASK-D2` App memory/provider/context-freezing parity
3. `TASK-D3` App spec-compliance regression density
4. `TASK-E1` Host job-stage parity + trace-replay regression
5. `TASK-E2` Codegen materialized output parity (flush/artifacts)
6. `TASK-E3` Intent-IR canonical utility parity
7. `TASK-F1` Compiler parse/tokens API + CLI entry extension
8. `TASK-F2` World persistence error/batch contract tightening

### 10.3 Stage D (P0)

#### `TASK-D1` App SDK Surface Expansion
- [ ] TS `App` 인터페이스 대비 Java `App` 공개 계약 gap map 확정
- [ ] world-query/head/action-handle lookup/session surface를 Java API에 단계 반영
- [ ] 호환성: 기존 `AppFactory`/`DefaultApp` 사용 코드 회귀 없음 보장

Validation:
- `./gradlew :manifesto-app:test`

#### `TASK-D2` App Memory Context Parity
- [ ] memory provider/verifier 수준의 pluggable 계약 추가
- [ ] context freezing/recall 실패 표식(contract-only 우선) 반영
- [ ] memory 관련 오류/진단 타입 정리

Validation:
- `./gradlew :manifesto-app:test`

#### `TASK-D3` App Compliance Regression Expansion
- [ ] TS app test 축 기준 회귀 매트릭스 작성
- [ ] policy/subscription/publish-boundary/timing 카테고리 Java 회귀 추가
- [ ] cross-module(app-host-world-core) 회귀 시나리오 2건 이상 추가

Validation:
- `./gradlew :manifesto-app:test`
- `./gradlew test`

### 10.4 Stage E (P1)

#### `TASK-E1` Host Event-Loop Parity Hardening
- [ ] `ApplyPatches` 단계 분리 여부를 TS 모델 기준으로 결정하고 반영
- [ ] execution trace replay 성격의 회귀 축 추가
- [ ] job ordering/liveness invariant 테스트 보강

Validation:
- `./gradlew :manifesto-host:test`

#### `TASK-E2` Codegen Output Materialization
- [ ] outDir flush/clean/write 경계 API(또는 runner 옵션) 추가
- [ ] plugin artifacts 누적 결과 계약 추가
- [ ] filesystem integration test 추가

Validation:
- `./gradlew :manifesto-codegen:test`

#### `TASK-E3` Intent-IR Canonical Utility Parity
- [ ] strict/semantic canonical API를 TS 축과 대응되게 정리
- [ ] sim key distance/utility API 보강
- [ ] lexicon/resolver helper 공개면 정비 + 회귀 확장

Validation:
- `./gradlew :manifesto-intent-ir:test`

### 10.5 Stage F (P2)

#### `TASK-F1` Compiler API Surface Extension
- [ ] parse/tokens/check API 노출 경계(서비스/CLI) 확장
- [ ] CLI subcommand 확장(`parse`, `tokens`) 또는 동등 기능 제공
- [ ] 컴파일러 API/CLI 문서 및 회귀 테스트 갱신

Validation:
- `./gradlew :manifesto-compiler:test`

#### `TASK-F2` World Persistence Contract Tightening
- [ ] world error taxonomy 정비 (`WorldErrorCode` 대응 범위 정의)
- [ ] persistence batch result/utility 계약 도입 여부 결정 및 반영
- [ ] query/event/store-stats 회귀 보강

Validation:
- `./gradlew :manifesto-world:test`

### 10.6 Next Cycle Completion Gate
- [ ] `./gradlew test`
- [ ] `./gradlew checkGoldenSync`
- [ ] 문서 동기화(`docs/MASTER_COMPLETION_PLAN_2026-02-14.md`, `docs/PACKAGE_GAP_ANALYSIS_2026-02-14.md`, `docs/spec/*`, `docs/fdr/*`)
