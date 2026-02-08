# 다음 사이클 점검 결과 (2026-02-08)

기준 리포:
- Java: `/workspace/manifesto-java-core`
- TS 레퍼런스: `/workspace/core/packages/world`, `/workspace/core/packages/app`

목적:
- 문서 + 소스코드를 함께 점검해 다음 사이클의 우선 작업을 확정한다.

---

## 1) 요약 결론

다음 사이클의 최우선은 `World P0 하드닝`이다.

이유:
1. `submitProposal` 입력 검증에서 TS 대비 필수 검증이 누락되어 있음
2. `executeProposal`의 executor 예외 경로에서 terminal 상태/이벤트 정합이 약함
3. App READY-8(genesis computed 평가) 정합이 아직 미반영

---

## 2) 핵심 갭 (코드 근거)

### A. submitProposal 검증 갭
- Java:
  - `manifesto-world/src/main/java/ai/manifesto/world/ManifestoWorld.java:155`
  - 현재 actor/base world 존재 여부 중심
- TS 기준:
  - `core/packages/world/src/world.ts:465` (base world pendingRequirements 금지)
  - `core/packages/world/src/world.ts:499` (origin actor 일치 검증)
  - `core/packages/world/src/world.ts:508` (intentKey 검증)

판정:
- 기능 정합 `High` 리스크

### B. executeProposal 실패 경계 갭
- Java:
  - `manifesto-world/src/main/java/ai/manifesto/world/ManifestoWorld.java:266`
  - executor 호출 실패 시 catch 경로 부재
- TS 기준:
  - `core/packages/world/src/world.ts:825`
  - `core/packages/world/src/world.ts:953`
  - 실패 시 failed world 생성 + execution:failed 이벤트 + proposal failed 전이 보장

판정:
- 안정성/회귀 `High` 리스크

### C. READY-8 (genesis computed) 미정합
- Java:
  - `manifesto-app/src/main/java/ai/manifesto/app/DefaultApp.java:60`
  - ready()에서 snapshot 그대로 genesis 저장
- TS 기준:
  - `core/packages/app/src/bootstrap/app-bootstrap.ts:184`
  - genesis snapshot 단계에서 computed 평가

판정:
- app 초기 상태 정합 `Medium-High` 리스크

### D. Execution key 정책 확장성
- Java:
  - `manifesto-world/src/main/java/ai/manifesto/world/ManifestoWorld.java:165`
  - `:1` 고정
- TS 기준:
  - `core/packages/world/src/world.ts:519`
  - execution key policy 주입 가능 구조

판정:
- 재시도/운영 정책 `Medium` 리스크

### E. 테스트 커버리지 격차
- TS world 통합 테스트(`it`) 약 46개
- Java world 관련 테스트:
  - `manifesto-world/src/test/java/ai/manifesto/world/ManifestoWorldTest.java` (9)
  - `manifesto-world/src/test/java/ai/manifesto/world/authority/AuthorityEvaluatorTest.java` (3)
  - `manifesto-app/src/test/java/ai/manifesto/app/AppWorldIntegrationTest.java` (1)

판정:
- 회귀 방어력 `High` 리스크

---

## 3) 확정 우선순위 (다음 사이클)

1. World P0 하드닝
   - submitProposal 입력 검증 강화
   - executeProposal 예외 경계(실패 world/event/status) 보강
2. App READY-8 정합
   - genesis computed 평가 반영 + 회귀 테스트
3. ExecutionKeyPolicy 도입
   - 정책 주입 + 재시도 attempt 확장 기반
4. TS world.test 시나리오 포팅 확장
   - authority/lineage/query matrix 우선

---

## 4) 이번 턴 즉시 진행 항목

1. 문서 반영 완료 (`본 문서`, `NEXT_ACTIONS`, `PORTING_SUMMARY`)
2. `World P0 하드닝` 구현 착수
   - submitProposal 검증
   - executeProposal 실패 경계
3. 관련 테스트 추가 및 `:manifesto-world:test`, `:manifesto-app:test` 실행

---

## 후속 업데이트 (2026-02-08)

- A/B/C 핵심 갭 조치 완료
  - submitProposal 검증 보강 반영
  - executeProposal 실패 경계 보강 반영
  - READY-8(`DefaultApp.ready()` genesis computed) 반영
- Host 경계 안정화 및 `$host` 1차 반영 완료
- Golden 확장 완료
  - core validate (`V-002`, `V-005`, `V-008`)
  - world approve/reject terminal
  - compiler onceIntent edge + namespace hash impact
