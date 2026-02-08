# NEXT_ACTIONS (바로 다음에 해야 할 일)

작성일: 2026-02-08
역할: **즉시 실행 가능한 작업 목록**을 제공한다. 다음 대화에서 바로 실행 가능한 수준으로 구체화한다.

---

## 0. 현재 상태 요약

- Golden 테스트 문서 및 기본 벡터/테스트 코드 추가 완료
  - `local-only-docs/golden/golden-test-scope.ko.md`
  - `local-only-docs/golden/golden-test-schema-equivalence.ko.md`
  - `local-only-docs/golden/golden-test-vector-format.ko.md`
  - `local-only-docs/golden/golden-test-sync-strategy.ko.md`
  - `manifesto-compiler/src/test/resources/golden/compiler-e2e.json`
  - `manifesto-compiler/src/test/java/ai/manifesto/compiler/CompilerGoldenTest.java`
  - `manifesto-core/src/test/resources/golden/compute.json`
  - `manifesto-core/src/test/java/ai/manifesto/core/core/GoldenComputeTest.java`
- Core/Compiler 골든 테스트는 **TS 테스트 구조를 참조**하여 구성함.
- World MVP 설계 문서 작성 완료 (`local-only-docs/design/world-mvp-design.ko.md`).
- World 정식 구현 1차 착수 완료
  - `manifesto-world` 모듈 추가 + `settings.gradle` 연결
  - schema/types 골격 구현
  - proposal 상태기계/queue 구현
  - hash/factory 1차 구현 (`computeSnapshotHash`, `computeWorldId`, genesis/execution world, decision record)
  - actor registry 구현
  - lineage DAG 구현
  - world store/memory store 구현
  - authority evaluator 구현(auto/policy/hitl/tribunal)
  - `ManifestoWorld` 오케스트레이터 1차 구현(createGenesis/submitProposal/processHITLDecision/execute)
  - `manifesto-app`에 World 주입 경로 1차 구현 (`createWorldApp`, `DefaultApp` world path)
  - epoch/branch switch + world event sink 1차 구현
  - query API 보강 (`getProposal`, `getDecisionByProposal`, `getEvaluatingProposals`, `getWorld`, `getSnapshot`, `getGenesis`)
  - authority timeout 처리(`tick`) + stale pending drop 구현
  - policy escalation 구현 (authorityId target 라우팅, loop guard, escalation event)
  - execution key/idempotency 보강 테스트 추가
  - branch/event payload envelope 표준화(`schemaHash`, `epoch`)
  - app/world 통합 테스트 추가 및 안정화
  - `:manifesto-world:test` 통과
- TS 변경점 반영 진행 완료
  - Core schema hash에 meta namespace 반영
  - Compiler `onceIntent` 지원(parser/analyzer/IR/renderer/test)
  - Compiler 골든 테스트 통과 정합화 완료
  - Core 결정성 1차 정리 완료(`System.currentTimeMillis()` 제거)

### Baseline 고정 (2026-02-08)
- world P0 hardening 반영:
  - `submitProposal` 검증(base world pending/origin actor/intentKey)
  - `executeProposal` 예외 경계(failed world/event/status)
- app READY-8 1차 반영:
  - `DefaultApp.ready()` genesis computed 평가
- world execution key 정책 주입 1차 반영:
  - `ExecutionKeyPolicy` 도입 + 기본 정책 유지

---

## 1. 다음 우선 작업 (1순위)

기준 점검 문서:
- `local-only-docs/reports/next-cycle-review-2026-02-08.md`
- `local-only-docs/reports/module-implementation-audit-2026-02-08.md`

### 1-1. World 정식 구현 Phase 8 확장 (최우선)
**목표**: TS `world.test.ts` 시나리오 대응 커버리지를 핵심 경로까지 확장

**할 일**
1. TS `world.test.ts`의 authority/lineage/query 시나리오 추가 포팅
2. escalation 정책 고도화 (멀티 홉 + fallback 규칙)
3. app-world-host 장애 경계 회귀 확대 (실패 world/event/status 일관성)

**완료 기준**
- `:manifesto-world:test` 통과
- TS world 시나리오 포팅 coverage가 현재 대비 의미 있게 증가

**진행 상태 (2026-02-08 업데이트)**
- [x] escalation 멀티 홉 경로 테스트 추가
- [x] escalation 실패 fallback(`proposal:escalation_failed`) 반영 및 테스트 추가
- [x] lineage query API 확장(`isDescendant`, `findPath`) 및 테스트 추가
- [x] sequential/branching 계열 시나리오 포팅 보강
- [x] `:manifesto-world:test`, `:manifesto-app:test`, `:manifesto-host:test` 통과

---

### 1-2. Compiler lowering/evaluation 정식 계층 정합화
**목표**: Lite 구현 의존도를 낮추고 TS compiler 핵심 계층과 동작 정렬

**할 일**
1. lowering 정식 API 확장 (context 제약, shape 검증, 오류 코드)
2. runtime patch evaluation trace/skip reason 보강
3. compiler golden/vector 케이스 확장

**완료 기준**
- `:manifesto-compiler:test` 통과
- 주요 lowering/eval 시나리오가 TS 케이스와 정렬

**진행 상태 (2026-02-08 업데이트)**
- [x] `RuntimePatchEvaluatorLite`에 trace 포함 평가 API(`evaluateWithTrace`) 추가
- [x] skip reason code 정규화(`COND_FALSE`, `COND_NULL`, `COND_NON_BOOLEAN`)
- [x] applied/skipped/dropped trace 이벤트 기록 추가
- [x] 단위 테스트 추가(`RuntimePatchEvaluatorLiteTest`) + `:manifesto-compiler:test` 통과

---

### 1-3. Golden 벡터 자동 동기화
**목표**: TS ↔ Java 골든 벡터 업데이트를 수작업에서 자동화로 전환

**할 일**
1. TS 벡터 dump 생성 경로 확정
2. Java 동기화 스크립트(`scripts/sync-golden.sh`) 구현
3. CI 검증 훅 추가(벡터 불일치 fail)

**진행 상태 (2026-02-08 업데이트)**
- [x] Java 동기화 스크립트 추가: `scripts/sync-golden.sh`
- [x] TS compiler 벡터/골든 경로 자동 탐색 + 복사/누락 보고 기능 반영
- [x] TS 레포 벡터 dump 경로 확정 (`/packages/compiler/vectors`)
- [x] 실제 동기화/검증 실행 완료 (`sync-golden.sh`, `check-golden-sync.sh`)
- [x] CI 훅 대체 Gradle task 추가 (`syncGoldenVectors`, `checkGoldenSync`)

---

### 1-4. Host 경계 안정화
**목표**: host 최소 런타임의 수렴/종료/장애 동작을 안정화

**할 일**
1. loop guard 정책 문서화 및 timeout/iteration 파라미터 정리
2. host pending/non-converging 시나리오 테스트 추가
3. host 네임스페이스(`data.$host`) 정합화 설계 초안 작성

---

## 2. 다음 우선 작업 (2순위)

### 2-1. Golden 테스트 확장

**대상**
- Validate (V-002, V-005, V-008)
- Host (effect 실행 결과)
- World (승인/거절 결과)
- Compiler (`onceIntent` edge case, namespace hash 영향 케이스)

---

## 3. 다음 우선 작업 (3순위)

### 3-1. Bridge 범용 어댑터 확장
- Intent 구조화, SnapshotView 전달 채널 확장

### 3-2. Host 정책 기능 강화
- 재시도/타임아웃/실패 기록

---

## 4. 문서/정리 작업

1. **IMPL_RESULT 문서 정합성 확인** (현재 일부 내용이 최신과 불일치)
2. 포팅 평가/액션 플랜 문서 최신화
3. local-only-docs에 최신 문서 모으기

---

## 5. 빠른 체크리스트 (다음 대화 시작 시)

- [ ] Golden 벡터 자동 생성/동기화 구현 착수 여부
- [ ] App bootstrap genesis computed 정합화 착수 여부
- [ ] World 정식 구현 Phase 8 확장 착수 여부
- [ ] Golden 테스트 확장 대상 결정 (compiler/host/world 포함)
