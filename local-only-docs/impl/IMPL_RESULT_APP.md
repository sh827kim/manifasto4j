# IMPL_RESULT_APP

## Scope
TypeScript `packages/app` 대비 Java `manifesto-app` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/app/src`

## 대상 (Java)
- `manifesto-app/src/main/java/ai/manifesto/app`

## 구현됨 (요약)
- App 인터페이스(최소): `ready()`, `act(Intent)`, `subscribe(selector, handler)`, `getSnapshot()`, `getSchema()`
- DefaultApp: `HostRuntime.run` 기반 동기 실행 + 구독자 notify
- AppFactory: `schema + initialSnapshot + host`로 App 생성
- World 연동 경로: `createWorldApp(...)`, `switchBranch(...)`, `getWorld()`
- READY-8 정합화 1차: `ready()`에서 genesis 생성 전 computed 평가 반영

## 미구현
### 1) v2 App API 및 구성
- TS는 `createApp(AppConfig)` 중심의 v2 API 제공
  - Host/WorldStore 주입, plugins, hooks, actorPolicy, scheduler, systemActions, validation 등 지원
- Java는 `AppFactory.createApp(schema, snapshot, host)`만 제공
  - AppConfig, legacy API, 마이그레이션 호환 없음

### 2) 실행/오케스트레이션 레이어
- TS는 `execution/*`에 액션 실행, 준비/실행 단계, 큐, executor, proposal 관리, 결과 매핑 등 제공
- Java는 `HostRuntime.run` 단일 호출만 지원
  - action queue, proposal manager, domain executor, v2 executor, initializer, liveness guard 미구현

### 3) Runtime 기능군
- TS `runtime/*`
  - system facade/runtime, memory facade/hub, policy service, subscription, session 등
- Java에 해당 기능 없음

### 4) Storage/World/Branch
- TS `storage/*`
  - world store(메모리/델타), world events, branch manager, schema compatibility, world head tracker
- Java는 world 연동 경로는 존재하나 TS app의 storage/runtime 계층은 미구현

### 5) Hooks/Plugins/Services
- TS hooks(`hookable`, `app-ref`, queue), plugins, service registry, effect handlers
- Java에 hooks/plugins/services 추상화 없음

### 6) Error/Diagnostics
- TS `errors/*`는 앱 수명주기/액션/메모리/정책/플러그인 등 세분 오류 제공
- Java는 해당 에러 모델 미구현

### 7) 테스트 및 스펙 기반 동작
- TS는 다수의 v2 스펙/호환성/정책/세션/메모리 테스트 존재
- Java는 `AppTest` 최소 수준

## 불일치
- (현재까지 발견된 주요 불일치 없음 — 구현 범위가 제한되어 미구현이 대부분)

## 정리
- Java `manifesto-app`는 **서버/CLI용 최소 실행 레이어**로 구현됨
- TS `app`의 핵심 기능(호스트/월드스토어 분리, 정책, 세션, 메모리, 브랜치, 이벤트, 훅/플러그인)은 **대부분 미구현**

## 다음 작업 후보 (app 기준)
1. v2 AppConfig 모델 및 createApp API 도입 (Host/WorldStore 주입)
2. 실행 파이프라인 계층화 (prepare/execute/result, queue, proposal)
3. WorldStore/Branch/Events 최소 구현
4. Memory/Policy/Services/Hooks의 단계적 도입

## 최근 업데이트 (2026-02-08)
- `createWorldApp` + `DefaultApp` world 연동 경로 반영
- READY-8(genesis computed) 1차 정합화 반영
