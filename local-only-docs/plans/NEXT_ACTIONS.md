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

---

## 1. 다음 우선 작업 (1순위)

### 1-1. Golden 벡터 자동 생성/동기화 구현
**목표**: TS → JSON 벡터 생성, Java → 소비 자동화

**할 일**
1. TS 레포에 벡터 덤프 모드 추가
   - MEL 입력 → schema 결과를 JSON 저장
   - `vectorVersion` 필드 포함
2. Java 레포에 동기화 스크립트 추가
   - 예: `scripts/sync-golden.sh`
   - TS 벡터 → `manifesto-compiler/src/test/resources/golden/`로 복사
3. CI에서 동기화 검증 추가

**필수 확인 사항**
- 동치성 기준: `local-only-docs/golden/golden-test-schema-equivalence.ko.md`
- 벡터 포맷: `local-only-docs/golden/golden-test-vector-format.ko.md`

---

### 1-2. World 정식 구현 진행 (MVP 아님)
**근거 문서**: `local-only-docs/plans/WORLD_FULL_IMPLEMENTATION_PLAN_2026-02-08.md`

**목표**
- TS `packages/world` 구조와 테스트 시나리오 기준의 정식 구현

**즉시 할 일 (일곱 번째 사이클)**
1. TS `world.test.ts` 시나리오 포팅 확대 (authority/lineage/query matrix)
2. authority escalation 정책 고도화(멀티 홉/정책별 fallback 정의)
3. execution key 재시도 정책 추가(현재 `:1` 고정)
4. app-world-host 장애 경계 테스트 강화

**완료 기준 (일곱 번째 사이클)**
- TS world 포팅 coverage 증가(핵심 시나리오 대부분 대응)
- hardening 케이스(타임아웃/분기/중복 worldId/실패 경계) 회귀 통과

---

### 1-3. App bootstrap genesis computed 정합화
**목표**: TS READY-8(`539b5b8`)와 초기 snapshot computed 평가 정책 정렬

**할 일**
1. `manifesto-app` 초기화 경로에서 computed 평가 시점 점검
2. 초기 snapshot 생성 정책 문서화 (computed 포함 여부)
3. 최소 회귀 테스트 추가

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
