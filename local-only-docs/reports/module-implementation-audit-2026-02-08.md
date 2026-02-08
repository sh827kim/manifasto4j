# 모듈별 구현 상태 점검 리포트 (2026-02-08)

대상 리포: `/workspace/manifesto-java-core`  
기준 TS 리포: `/workspace/core`

---

## 1. 점검 방법

다음 기준으로 모듈별 상태를 점검했다.

1. 코드/테스트 규모 지표
- `src/main/java` 파일 수
- `src/test/java`의 `@Test` 수

2. 실행 검증
- 성공:
  - `./gradlew :manifesto-core:test :manifesto-compiler:test :manifesto-world:test :manifesto-app:test :manifesto-bridge:test :manifesto-builder:test`
  - `./gradlew :manifesto-effect-utils:test :manifesto-examples:test`
- 제한:
  - `./gradlew :manifesto-host:test`는 시간 제한(90s) 내 완료되지 않음
  - `./gradlew test` 전체 실행도 `:manifesto-host:test` 단계에서 시간 제한으로 중단됨

3. 문서/코드 정합성
- `local-only-docs/PORTING_SUMMARY.md`
- `local-only-docs/impl/IMPL_RESULT_*.md`
- 최근 world/app 변경 코드 반영 여부

---

## 2. 모듈별 상태 요약

### 2.1 핵심 모듈

`manifesto-core`
- 지표: main 101, test 167
- 상태: **구현 성숙도 높음 (상)**
- 근거:
  - Core API/평가기/검증/적용/추적 구조가 넓게 구현됨
  - 테스트 수가 가장 많고 회귀 기반이 안정적
  - 최근 deterministic 정합화 반영됨
- 리스크:
  - TS 최신 변경점이 계속 들어오므로 벡터 동기화 자동화 미완료는 리스크

`manifesto-compiler`
- 지표: main 88, test 31
- 상태: **중간 이상 (중상)**
- 근거:
  - lexer/parser/analyzer/IR/render 및 golden 테스트 체계 존재
  - `onceIntent` 반영 및 정합화 완료
- 리스크:
  - lowering/evaluation 계층은 여전히 Lite 중심
  - TS 정식 계층 대비 diagnostics/shape 검증 격차 존재

`manifesto-world`
- 지표: main 66, test 47
- 상태: **활발한 구현 진행 (중상)**
- 근거:
  - schema/proposal/authority/lineage/persistence/orchestrator 구현
  - 최근 P0 하드닝 + READY-8 연계 + execution key policy 주입 반영
  - 테스트 규모가 빠르게 증가 중
- 리스크:
  - TS `world.test.ts` 대비 시나리오 포팅은 아직 추가 필요
  - escalation 정책/재시도 정책/경계 시나리오 하드닝 잔여

`manifesto-app`
- 지표: main 4, test 3
- 상태: **기능 구현은 동작, 범위는 제한적 (중)**
- 근거:
  - `DefaultApp` + `AppFactory` 중심 최소 SDK 동작
  - world 통합 경로와 READY-8(초기 computed) 1차 반영 완료
- 리스크:
  - TS app v2 대비 hooks/plugins/runtime/services/branch/storage가 크게 부족

`manifesto-host`
- 지표: main 3, test 1
- 상태: **최소 런타임 수준 (중하)**
- 근거:
  - compute-effect loop 최소 구현
- 리스크:
  - TS host 아키텍처(메일박스/러너/잡/컨텍스트/호스트 상태 네임스페이스)와 격차 큼
  - 테스트 실행이 시간 제한 내 완료되지 않아 별도 원인 점검 필요

---

### 2.2 보조 모듈

`manifesto-bridge`
- 지표: main 4, test 1
- 상태: **스켈레톤 + 최소 기능 (하)**
- 리스크:
  - TS bridge의 registry/dispatch/subscription/world 연동 미구현

`manifesto-builder`
- 지표: main 1, test 1
- 상태: **최소 DSL 유틸 (하)**
- 리스크:
  - TS builder의 typed refs/accessor/diagnostics/DSL 계층 미구현

`manifesto-effect-utils`
- 지표: main 1, test 0 (`test NO-SOURCE`)
- 상태: **초기 유틸만 존재 (하)**
- 리스크:
  - combinator/transform/error 모델 대부분 미구현

`manifesto-examples`
- 지표: main 1, test 0 (`test NO-SOURCE`)
- 상태: **샘플 최소 구성**

---

## 3. 문서 정합성 점검 결과

다음 문서는 현 코드 상태 대비 업데이트 필요성이 확인됨.

`local-only-docs/impl/IMPL_RESULT_CORE.md`
- 현재 문서에는 “System time 직접 사용 잔존”으로 기재되어 있으나,
  최근 코드/요약에서는 결정성 정합화가 반영된 상태로 관리 중

`local-only-docs/impl/IMPL_RESULT_APP.md`
- “world/branch/storage 계층 없음” 표현이 현재 world 통합 반영 상태와 일부 불일치

`local-only-docs/impl/IMPL_RESULT_HOST.md`
- “최근 업데이트 없음(2026-02-01)” 상태로 최신 점검 결과 미반영

`local-only-docs/impl/IMPL_RESULT_BRIDGE.md`, `IMPL_RESULT_BUILDER.md`, `IMPL_RESULT_EFFECT_UTILS.md`
- 전반적으로 최신 코드/테스트 지표 반영이 부족

---

## 4. 종합 판정

1. **Core/Compiler/World는 실구현 기반이 확보된 상태**이며, world는 빠르게 정합화 중이다.
2. **Host/App/Bridge/Builder/Effect-utils는 TS 대비 기능 격차가 큰 영역**으로, 특히 host/app 아키텍처 레벨 차이가 크다.
3. **전체 테스트 배치에서 host 테스트 단계 타임아웃 이슈가 존재**하므로, 다음 계획 수립 전에 host 테스트 실행 시간/루프 종료 조건 점검이 필요하다.
4. **impl 문서군(`local-only-docs/impl`)은 최신 코드 상태와 일부 불일치**하므로, 다음 사이클에서 문서 정합화 작업을 우선 포함하는 것이 바람직하다.

---

## 5. 다음 계획 수립 전 권고 선행 점검

1. `:manifesto-host:test` 단독 타임아웃 원인 파악 (테스트/루프/타임아웃 정책)
2. `local-only-docs/impl/IMPL_RESULT_*.md` 최신 상태 동기화
3. world/app 최근 반영(READY-8, execution key policy, P0 hardening)을 기준 baseline으로 고정

### 진행 결과 (2026-02-08)
- [x] 1번 완료
  - 원인: 최신 경로 규칙(data 무접두사)과 일부 테스트 경로(`data.status`) 불일치
  - 조치: host/app 테스트 경로 정렬 + `HostRuntime` 비수렴 루프 가드 추가
  - 검증: `./gradlew :manifesto-host:test` 통과
- [x] 2번 완료
  - `IMPL_RESULT_CORE/COMPILER/HOST/APP/BRIDGE/BUILDER/EFFECT_UTILS` 동기화
- [x] 3번 완료
  - `local-only-docs/plans/NEXT_ACTIONS.md`에 baseline 고정 항목 반영

### 후속 업데이트 (2026-02-08)
- Host timeout/loop guard 정책 코드 + 문서 반영 완료
- Host/World/Core/Compiler golden 확장 반영 완료
- 모듈 상태 해석 시 본 리포트의 일부 “미구현” 항목은 historical 맥락으로 읽고,
  최신 상태는 `local-only-docs/PORTING_SUMMARY.md`와 `local-only-docs/plans/NEXT_ACTIONS.md`를 우선 참조
