# NEXT_ACTIONS (다음 사이클 실행 계획)

작성일: 2026-02-08
기준: Action 1~4 완료 이후, Priority 2/3 잔여 항목을 실행한다.

---

## 1. Action 1 - Framework-neutral Bridge Adapter 계약 정의

목표:
- 특정 프레임워크에 종속되지 않는 `ExternalEventAdapter` 계약을 정의한다.

실행 아이템:
1. 외부 이벤트 어댑터 인터페이스 추가
2. 구현체가 따라야 하는 입력 검증/매핑/오류 경계 규칙을 인터페이스 문서에 명시
3. `./gradlew :manifesto-bridge:test` 통과

완료 기준:
- adapter 계약만으로 구현 요건이 명확히 이해되고, bridge 모듈은 프레임워크 중립성을 유지함

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/ExternalEventAdapter.java`
- 검증:
  - `./gradlew :manifesto-bridge:test` 통과

---

## 2. Action 2 - World TS 시나리오 2차 포팅

목표:
- TS `world.test.ts`의 query/authority 경계 시나리오를 Java 테스트로 추가 고정한다.

실행 아이템:
1. query 결과 정합 경계 테스트 2~3건 추가
2. authority escalation 실패/복구 경계 1~2건 추가
3. `./gradlew :manifesto-world:test` 통과

완료 기준:
- world 회귀 방어력이 확대되고, edge case가 테스트로 문서화됨

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-world/src/test/java/ai/manifesto/world/ManifestoWorldTest.java`
    - `queryApisReturnNullForMissingIds`
    - `updateActorBindingReplacesPolicyAndRejectsUnknownActor`
- 검증:
  - `./gradlew :manifesto-world:test` 통과

---

## 3. Action 3 - Compiler 벡터 동기화 파이프라인 보강

목표:
- TS 변경점 유입 시 compiler golden/vector 동기화 누락을 자동 점검한다.

실행 아이템:
1. `scripts/check-golden-sync.sh` 출력 보강(누락 유형 분류)
2. Gradle 체크 task 리포트 메시지 정리
3. 문서에 운영 절차(로컬 실행 순서) 반영

완료 기준:
- 동기화 누락 시 원인 파악이 즉시 가능하고, 문서 절차와 도구 출력이 일치함

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `scripts/check-golden-sync.sh`
    - missing source/destination/mismatched 분류 리포트 추가
  - `local-only-docs/impl/IMPL_RESULT_COMPILER.md`
    - 로컬 운영 절차(동기화/검증 명령) 반영
- 검증:
  - `./gradlew checkGoldenSync` 통과

---

## 실행 순서

1. [완료] Action 1
2. [완료] Action 2
3. [완료] Action 3
