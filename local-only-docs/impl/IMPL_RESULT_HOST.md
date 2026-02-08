# IMPL_RESULT_HOST

## Scope
TypeScript `packages/host` 대비 Java `manifesto-host` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/host/src`

## 대상 (Java)
- `manifesto-host/src/main/java/ai/manifesto/host`

## 구현됨 (요약)
- `HostRuntime`: compute → effect → apply 루프의 최소 구현
- `EffectHandler` / `EffectResult` 인터페이스
- 테스트: `HostRuntimeTest`
- 비수렴 effect 루프 가드 추가 (최대 반복 제한 기반 fail-fast)

## 미구현
### 1) Host 아키텍처(메일박스/러너/잡 모델)
- TS는 **Mailbox + Runner + Job** 기반 이벤트 루프 구조 제공
- Java는 단일 while 루프에서 동기 처리 (job/runner/mailbox 개념 없음)

### 2) Execution Context / Runtime
- TS는 `ExecutionContext`, `Runtime`(시간/스케줄링), `HostContextProvider` 제공
- Java는 `HostContext.forSnapshot`만 사용 (스케줄링, env, trace hook 미지원)

### 3) Host-owned state namespace
- TS는 host 상태를 `data.$host`에 저장 (HOST-NS-1)
- Java는 `data.$host` 1차 반영 완료
  - `currentIntentId`, `intentSlots` 기록 경로 추가
  - Core `Apply`에서 `$host` 예약 경로 허용
- Java는 `data.$host` 2차 반영 완료
  - `lastError`, `errors` 기록 경로 추가
- 단, stale slot lifecycle/고급 상태 모델은 추가 정합화 필요

### 4) Effect 실행 모델
- TS는 registry/executor, handler 옵션, auto-effect 비활성화, fulfill/inject 흐름 제공
- Java는 `handlers` 맵과 즉시 실행만 지원

### 5) Trace / Error 모델
- TS는 `TraceEvent`, HostError 코드, 상태 추적 제공
- Java는 HostError 타입 없음, trace 이벤트 훅 없음

### 6) Snapshot/Intent 흐름
- TS는 `createSnapshot`, `evaluateComputed`를 통한 초기화 및 통합 상태 관리 제공
- Java는 초기 snapshot 생성/관리 기능 없음 (caller가 직접 생성)

### 7) 테스트 및 스펙 준수
- TS는 Host compliance test suite 및 golden tests 다수 존재
- Java는 최소 테스트만 존재

## 불일치
### 1) Host-owned state namespace
- TS는 host 상태를 `data.$host`에 저장 (HOST-NS-1)
- Java도 `data.$host` 1차 반영 완료 (intent slot 기록)
- Java도 host 에러 누적(`lastError/errors`) 반영 완료
- 남은 과제: stale slot lifecycle 정합화

## 정리
- Java host는 **최소 동기 런타임** 수준
- TS host의 핵심 스펙(메일박스/러너/잡/컨텍스트/호스트 상태 네임스페이스)을 대부분 누락

## 다음 작업 후보 (host 기준)
1. Host state namespace 정합화 (`data.$host` 기반)
2. ExecutionContext/Runtime/Trace hook 기본 구조 도입
3. Job/Runner/Mailbox 최소 모델 추가
4. Effect registry/executor와 fulfill/inject 흐름 추가

## 최근 업데이트 (2026-02-08)
- `HostRuntime` 비수렴 루프 가드 반영
- Host 테스트 경로를 최신 data 무접두사 규칙(`status`)으로 정렬
- `HostRuntimeOptions` 추가(timeout/maxIterations 정책 분리)
- pending 경계 보강:
  - missing handler 시 `PENDING` 반환 유지
  - `PENDING + empty requirements` 조기 반환
- `$host` 예약 경로 허용 및 HostRuntime intent slot 기록 반영
- `$host.lastError/errors` 기록 반영 + retry/timeout 옵션 확장
- 관련 테스트 보강:
  - `testEffectLoopGuardStopsNonConvergingExecution`
  - `testReturnsPendingWhenHandlerMissing`
  - `testRecordsHostErrorWhenEffectFails`
  - `testEffectRetryCanRecoverFromTransientFailure`
  - `ApplyTest.testHostReservedPathPatchAllowedWithoutStateSpecField`
  - `HostGoldenTest` + `golden/host-e2e.json` 확장
- `data.$host` 정합화 설계 초안 작성:
  - `local-only-docs/design/host-runtime-boundary-hardening.ko.md`
