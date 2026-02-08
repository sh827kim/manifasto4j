# NEXT_ACTIONS (다음 사이클 실행 계획)

작성일: 2026-02-08  
기준: 완료된 골든 확장 1차를 baseline으로 두고, 다음 구현 사이클의 실행 항목만 기록한다.

---

## 1. Action 1 - World Phase 8+ 시나리오 추가 포팅 (즉시 진행)

목표:
- authority/query/escalation edge 경로를 TS `world.test.ts` 기준으로 추가 고정한다.

실행 아이템:
1. tribunal timeout approve/reject 경계 시나리오 테스트 추가
2. stale pending(브랜치 스위치) 정리 경로를 tribunal까지 확장
3. world 이벤트/상태 전이 핵심 필드 회귀 검증 추가
4. `./gradlew :manifesto-world:test` 통과

완료 기준:
- 신규 edge 케이스 테스트가 안정적으로 통과
- 기존 world 테스트 회귀 없음

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-world/src/test/java/ai/manifesto/world/ManifestoWorldTest.java`
    - `tribunalTimeoutApproveIsAppliedByTick`
    - `staleTribunalProposalIsDroppedOnBranchSwitch`
- 검증:
  - `./gradlew :manifesto-world:test` 통과

---

## 2. Action 2 - Host 정책 기능 강화 (`$host` 2차)

목표:
- Host 실패/복구 관찰성을 높인다.

실행 아이템:
1. `$host.lastError`, `$host.errors` 기록 경로 추가
2. retry/timeout 옵션 설계 및 최소 구현
3. host golden 벡터에 실패/재시도 케이스 추가
4. `./gradlew :manifesto-host:test` 통과

완료 기준:
- 실패 실행의 host 기록이 snapshot에서 일관되게 확인됨
- retry/timeout 정책이 테스트로 고정됨

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-host/src/main/java/ai/manifesto/host/HostRuntimeOptions.java`
    - `maxEffectRetries`, `maxEffectDurationMillis` 옵션 추가
  - `manifesto-host/src/main/java/ai/manifesto/host/HostRuntime.java`
    - `$host.lastError`, `$host.errors` 기록 경로 추가
    - effect retry/timeout 정책 반영
  - `manifesto-host/src/test/java/ai/manifesto/host/HostRuntimeTest.java`
    - 실패 기록/재시도 복구 테스트 추가
  - `manifesto-host/src/test/java/ai/manifesto/host/HostGoldenTest.java`
  - `manifesto-host/src/test/resources/golden/host-e2e.json`
    - 실패 기록 골든 케이스 추가
- 검증:
  - `./gradlew :manifesto-host:test` 통과

---

## 3. Action 3 - Compiler lowering/evaluation 정식 계층 정합화

목표:
- Lite 의존도를 줄이고 TS 계층과 오류/shape 규칙을 맞춘다.

실행 아이템:
1. lowering API 확장 (context 제약 + shape 검증)
2. evaluation 오류 코드/trace 정규화
3. compiler golden/vector 케이스 추가
4. `./gradlew :manifesto-compiler:test` 통과

완료 기준:
- 신규 API/오류 규칙이 테스트로 고정
- 기존 golden/vector 회귀 없음

상태:
- [ ] 대기

---

## 4. Action 4 - Bridge/App runtime 확장

목표:
- Intent 발행/라우팅/구독 경계를 실사용 수준으로 확장한다.

실행 아이템:
1. Bridge 이벤트 라우팅 구조화
2. App runtime/session/store 최소 경로 추가
3. app-bridge 통합 테스트 보강
4. `./gradlew :manifesto-app:test :manifesto-bridge:test` 통과

완료 기준:
- runtime 경로가 테스트로 재현 가능
- 문서와 구현의 용어/역할이 정합

상태:
- [ ] 대기

---

## 실행 순서

1. [완료] Action 1
2. [완료] Action 2
3. Action 3
4. Action 4
