# Host Runtime 경계 안정화 설계 초안 (2026-02-08)

## 1. 배경

Java `manifesto-host`는 현재 최소 compute-effect loop 구현이다.
이번 사이클의 목표는 다음 3가지를 우선 정리하는 것이다.

1. loop guard 정책 명시화 (timeout vs iteration)
2. pending/non-converging 경계 동작 회귀 테스트 보강
3. TS host 스펙(`data.$host`)과의 정합화 방향 확정

참고 기준:
- TS host 구현: `/workspace/core/packages/host/src/host.ts`
- TS host namespace: `/workspace/core/packages/host/src/types/host-state.ts`
- TS host spec: `/workspace/core/packages/host/docs/host-SPEC-v2.0.2.md`

## 2. 이번 사이클 반영 사항

### 2-1. loop guard 정책

- `HostRuntimeOptions` 도입:
  - `timeoutSeconds`
  - `maxIterations`
- 기본 정책:
  - `timeoutSeconds` 기본 5초
  - `maxIterations` 기본 `timeoutSeconds * 100`
- 기존 API 호환:
  - `run(..., int timeoutSeconds)`는 내부적으로 `HostRuntimeOptions.forTimeoutSeconds()`를 사용

핵심 효과:
- 운영 정책(시간/반복)을 외부에서 제어 가능
- 테스트에서 반복 상한을 의도적으로 낮춰 경계 케이스를 빠르게 재현 가능

### 2-2. pending/non-converging 경계

- missing handler인 경우:
  - 기존 pending 결과를 그대로 반환 (`PENDING`)
- `ComputeResult.PENDING`이면서 requirement가 비어 있는 경우:
  - 루프를 지속하지 않고 즉시 pending 반환
- 반복 상한 초과 시:
  - `ERROR` 반환 (마지막 trace 유지)

추가 테스트:
- `HostRuntimeTest.testReturnsPendingWhenHandlerMissing`
- `HostRuntimeTest.testEffectLoopGuardStopsNonConvergingExecution` (options 기반 상한 검증)

## 3. `data.$host` 정합화 설계 초안

현재 Java host는 requirement 정리 시 `system.pendingRequirements` 중심으로만 동작한다.
TS v2.0.2 스펙은 host 소유 상태를 `data.$host`에 저장하도록 정의한다.

### 3-1. 도입 원칙

1. Core 소유 필드(`system.*`) 확장 금지
2. Host 상태는 `data.$host`에만 저장
3. Java Core가 `$host` 경로 patch를 허용하는 조건을 재확인 후 단계 도입

### 3-2. 1차 목표 데이터 구조

`data.$host`에 아래 필드를 우선 도입한다.

- `currentIntentId`
- `intentSlots` (intentId -> { type, input })
- `lastError`
- `errors`

### 3-3. 단계별 적용 계획

1. 스키마/패치 경로 검증:
   - Java Core에서 `$host.*` patch validation 허용 여부 확인
2. HostRuntime 최소 반영:
   - intent 처리 시작 시 `currentIntentId`/`intentSlots` 기록
   - effect 실패 시 `lastError`/`errors` 기록
3. 회귀 테스트:
   - host 실행 후 `snapshot.data.$host` 상태 검증 추가
4. app/world 연계 점검:
   - app/world 경로에서 `$host` 필드 존재 시 충돌 없는지 확인

## 4. 리스크

1. Java Core patch validation이 `$host` 경로를 차단하면 선행 수정 필요
2. 기존 테스트가 `system.*` 중심 가정을 갖고 있으면 일부 수정 필요
3. host/app/world 모듈 간 snapshot 직렬화 형태 점검 필요

## 5. 다음 액션

1. [x] `$host` patch 허용 여부를 core 테스트로 고정
2. [x] `HostRuntime`에 `data.$host` 기록 최소 경로 도입
3. [x] host golden 벡터(경계 케이스 포함) 확장

## 6. 진행 결과 (2026-02-08 2차 업데이트)

- Core `Apply`가 `$host` 예약 경로를 허용하도록 반영
  - 파일: `manifesto-core/src/main/java/ai/manifesto/core/core/Apply.java`
  - 테스트: `manifesto-core/src/test/java/ai/manifesto/core/core/ApplyTest.java`
- HostRuntime가 pending 처리 시 `$host`에 intent 슬롯을 기록하도록 반영
  - `$host.currentIntentId`
  - `$host.intentSlots.{intentId}.type`
  - `$host.intentSlots.{intentId}.input`
  - 파일: `manifesto-host/src/main/java/ai/manifesto/host/HostRuntime.java`
  - 테스트: `manifesto-host/src/test/java/ai/manifesto/host/HostRuntimeTest.java`
- host golden 테스트 1차 반영
  - 파일: `manifesto-host/src/test/java/ai/manifesto/host/HostGoldenTest.java`
  - 벡터: `manifesto-host/src/test/resources/golden/host-e2e.json`
