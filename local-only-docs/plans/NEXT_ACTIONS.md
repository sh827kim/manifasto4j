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
- TS 변경점 반영 진행 완료
  - Core schema hash에 meta namespace 반영
  - Compiler `onceIntent` 지원(parser/analyzer/IR/renderer/test)
  - Compiler 골든 테스트 통과 정합화 완료

---

## 1. 다음 우선 작업 (1순위)

### 1-1. Core 결정성(System time) 제거
**목표**: Core에서 `System.currentTimeMillis()` 직접 사용 제거

**수정 대상**
- `manifesto-core/src/main/java/ai/manifesto/core/HostContext.java`
- `manifesto-core/src/main/java/ai/manifesto/core/Snapshot.java`
- `manifesto-core/src/main/java/ai/manifesto/core/TraceNode.java`
- `manifesto-core/src/main/java/ai/manifesto/core/Requirement.java`
- `manifesto-core/src/main/java/ai/manifesto/core/utils/DagUtils.java`
- `manifesto-core/src/main/java/ai/manifesto/core/core/Compute.java`

**수정 원칙**
- Core 내부에서 시간은 `HostContext.getNow()` 기반만 사용
- 기본 생성자에서 시스템 시간 호출 제거

---

### 1-2. Golden 벡터 자동 생성/동기화 구현
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

## 2. 다음 우선 작업 (2순위)

### 2-1. World MVP 구현
**근거 문서**: `local-only-docs/design/world-mvp-design.ko.md`

**구현 대상**
- `manifesto-world` 모듈 추가
- Proposal/Decision/WorldRecord 데이터 모델
- PolicyAuthority/RoleAuthority/ManualAuthority(placeholder)
- `WorldRuntime.submitProposal(...)` API

---

### 2-2. Golden 테스트 확장

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

- [ ] Core System time 제거 진행 여부
- [ ] Golden 벡터 자동 생성/동기화 구현 착수 여부
- [ ] World MVP 구현 착수 여부
- [ ] Golden 테스트 확장 대상 결정 (compiler/host/world 포함)
