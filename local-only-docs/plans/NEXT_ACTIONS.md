# NEXT_ACTIONS (즉시 실행 항목)

작성일: 2026-02-08  
기준: 완료된 항목은 제거하고, 다음 구현 사이클의 실행 액션만 남긴다.

---

## 1. Action 1 - Validate Golden 확장 (즉시 진행)

목표:
- Core validation golden에 `V-002`, `V-005`, `V-008` 회귀 케이스를 고정한다.

실행 아이템:
1. `manifesto-core/src/test/resources/golden/validate.json` 추가
2. `manifesto-core/src/test/java/ai/manifesto/core/core/GoldenValidateTest.java` 추가
3. 기대값 비교 기준 고정
- `isValid`
- `errorCodes` (정렬 비교)
4. 검증 실행
- `./gradlew :manifesto-core:test`

완료 기준:
- validation golden 테스트가 실패 재현 없이 통과
- 기존 `ValidateTest`와 중복되지 않는 회귀 축(코드 기반 기대값)이 확보

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-core/src/test/resources/golden/validate.json`
  - `manifesto-core/src/test/java/ai/manifesto/core/core/GoldenValidateTest.java`
- 검증:
  - `./gradlew :manifesto-core:test` 통과

---

## 2. Action 2 - World Golden 확장

목표:
- World 승인/거절 terminal 경로를 golden 벡터로 고정한다.

실행 아이템:
1. world golden 벡터 초안 정의
- approve terminal: `COMPLETED`, result world 생성
- reject terminal: `REJECTED`, result world 미생성
2. `manifesto-world` golden 테스트 추가
3. `:manifesto-world:test` 실행 및 회귀 고정

완료 기준:
- 승인/거절 경로가 벡터 기반으로 비교 가능
- world 상태/이벤트 핵심 필드가 고정됨

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-world/src/test/resources/golden/world-e2e.json`
  - `manifesto-world/src/test/java/ai/manifesto/world/WorldGoldenTest.java`
  - `manifesto-world/build.gradle` (golden test JSON 파싱 의존성 추가)
- 검증:
  - `./gradlew :manifesto-world:test` 통과

---

## 3. Action 3 - Compiler Golden 확장

목표:
- compiler golden에 `onceIntent` edge case와 namespace hash 영향 케이스를 추가한다.

실행 아이템:
1. 케이스 정의
- `onceIntent` 중복/스코프 경계
- `meta.namespace` 변화에 따른 hash 차이
2. `compiler-e2e.json` 또는 별도 golden 벡터 확장
3. `:manifesto-compiler:test` 실행

완료 기준:
- 신규 케이스가 golden mismatch 없이 통과
- 기존 parser/analyzer/IR/renderer 정합을 깨지 않음

상태:
- [x] 완료 (2026-02-08)
- 산출물:
  - `manifesto-compiler/src/test/resources/golden/compiler-e2e.json`
    - `onceintent-contextual-edge` 케이스 추가
    - `namespace-hash-impact` 케이스 추가
  - `manifesto-compiler/src/test/java/ai/manifesto/compiler/CompilerGoldenTest.java`
    - `expectHashDifferent` 벡터 처리 로직 추가
- 검증:
  - `./gradlew :manifesto-compiler:test` 통과

---

## 다음 실행 순서

1. [완료] Action 1 - Validate Golden 확장
2. [완료] Action 2 - World Golden 확장
3. [완료] Action 3 - Compiler Golden 확장

---

## 다음 사이클 후보

1. World phase 8+ 시나리오 추가 포팅 (authority/query/escalation edge)
2. Host `$host.lastError/errors` 및 retry/timeout 정책 고도화
3. Compiler lowering/evaluation 정식 계층(API/오류코드/shape 검증) 반영
4. Bridge/App runtime 확장 (session/store/pipeline)
