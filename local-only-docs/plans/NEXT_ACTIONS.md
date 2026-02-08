# NEXT_ACTIONS (다음 사이클 실행 계획)

작성일: 2026-02-08
기준: bridge 프레임워크 중립성 유지 + world/compiler 정합 강화

---

## 1. Action 1 - Bridge ProjectionResult 모델 도입

목표:
- projection 결과를 `intent`와 `none(reason)`로 명시적으로 표현한다.

실행 아이템:
1. `ProjectionResult` 타입 추가
2. `Projection`을 `ProjectionResult` 반환으로 확장
3. `BridgeRuntime.projectResult()` 추가, `project()`는 호환 API로 유지
4. none 결과/예외 경계 테스트 추가
5. `./gradlew :manifesto-bridge:test :manifesto-app:test` 통과

완료 기준:
- Bridge에서 intent 미발행 케이스를 타입으로 표현 가능
- 기존 app 연동 경로 회귀 없음

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/ProjectionResult.java`
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/Projection.java`
  - `manifesto-bridge/src/main/java/ai/manifesto/bridge/BridgeRuntime.java`
  - `manifesto-bridge/src/test/java/ai/manifesto/bridge/BridgeRuntimeTest.java`
  - `manifesto-app/src/test/java/ai/manifesto/app/AppBridgeIntegrationTest.java`
- 검증:
  - `./gradlew :manifesto-bridge:test :manifesto-app:test` 통과

---

## 2. Action 2 - Bridge API 최소 뼈대 추가

목표:
- Projection 실행기를 dispatch 중심 API로 확장한다.

실행 아이템:
1. `dispatchEvent(SourceEvent)` 최소 API 정의
2. `dispatch(IntentBody)` 또는 동등 진입점 최소 API 정의
3. app-bridge 통합 테스트 1~2건 추가

완료 기준:
- 브리지 호출 경로가 projection 직접 호출 없이도 재현 가능

상태:
- [ ] 진행 예정

---

## 3. Action 3 - World TS 시나리오 3차 포팅

목표:
- TS `world.test.ts` 남은 query/authority/branch edge를 추가 고정한다.

실행 아이템:
1. query edge 2건 이상
2. authority/branch edge 2건 이상
3. `./gradlew :manifesto-world:test` 통과

완료 기준:
- world 회귀 방어력 강화

상태:
- [ ] 진행 예정

---

## 4. Action 4 - Compiler strict 계층 2차

목표:
- strict lowering/evaluation 규칙의 오류 코드/trace 정합을 확장한다.

실행 아이템:
1. strict 오류 코드 체계 보강
2. trace/skip reason 정합 테스트 추가
3. `./gradlew :manifesto-compiler:test` 통과

완료 기준:
- strict API에서 오류/trace 규칙이 테스트로 고정

상태:
- [ ] 진행 예정

---

## 5. Action 5 - 문서 동기화

목표:
- 구현 상태와 문서 상태를 일치시킨다.

실행 아이템:
1. `PORTING_SUMMARY`, `IMPL_RESULT_*`, 리포트 문서 갱신
2. 사이클 종료 체크리스트 갱신

완료 기준:
- 문서-코드 불일치 항목 0개

상태:
- [ ] 진행 예정
