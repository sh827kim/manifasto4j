# NEXT_ACTIONS (다음 사이클 실행 계획)

작성일: 2026-02-08
기준: Action 1~4 완료 이후, Priority 2/3 잔여 항목을 실행한다.

---

## 1. Action 1 - Spring AI Bridge Adapter 정의

목표:
- Spring AI 입력을 `SourceEvent`로 정규화하는 표준 어댑터를 bridge 모듈에 추가한다.

실행 아이템:
1. 외부 이벤트 어댑터 인터페이스 추가
2. Spring AI 스타일 입력 맵(`type/eventId/payload/metadata`) 정규화 구현
3. 매핑/검증 단위 테스트 추가
4. `./gradlew :manifesto-bridge:test` 통과

완료 기준:
- adapter 경로가 테스트로 고정되고, 잘못된 type 입력이 명시적으로 거부됨

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/ExternalEventAdapter.java`
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/SpringAiMessageAdapter.java`
  - `manifesto-bridge/src/test/java/ai/manifesto/bridge/SpringAiMessageAdapterTest.java`

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
- [ ] 진행 예정

---

## 실행 순서

1. [완료] Action 1
2. [완료] Action 2
3. [진행 예정] Action 3
