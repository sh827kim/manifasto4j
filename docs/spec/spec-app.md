# Manifesto Java App SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | app facade for server/CLI |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/app/docs/VERSION-INDEX.md` |
| Latest | `2.3.1` |

## 1. Scope

App is a facade over Core/Host/World/Store. Java App should provide:

- createApp(domain, opts)
- app.ready() explicit initialization
- act(intent) with ActionHandle lifecycle
- subscribe(state selector)
- service registration (effect handlers)

## 2. Server/CLI-Friendly Minimal App

Minimal conformance for server/CLI:

- Domain/System runtime kind 분리(`RuntimeKind`)
- ActionHandle: status/result/trace + updates + await/timeout/cancel
- Services map for effect execution
- Optional external store integration (disabled by default)

## 3. Required Behaviors

- MUST not modify core semantics
- MUST keep compute-effect loop deterministic
- MUST use explicit initialization (ready)
- MUST expose action execution as observable handle

## 4. ActionHandle Lifecycle (2026-02-14)

- `ActionHandle`는 최종 `ComputeResult` 외에 phase update history를 함께 보관한다.
- 표준 phase 집합:
  - `PREPARING`
  - `SUBMITTED`
  - `EXECUTING`
  - `COMPLETED`
  - `FAILED`
  - `REJECTED`
  - `PREPARATION_FAILED`
- App 구현체는 world/non-world 경로 모두에서 최소 `PREPARING -> ... -> terminal` 전이를 기록해야 한다.

## 5. Session/Branch/Hook API (2026-02-14)

- Session:
  - `getSessionId()`
  - `hasSessionPersistence()`
  - `createSession(actorId, context)`
- Branch:
  - `getCurrentBranchId()`
  - `listBranches()`
  - `createBranch(name, worldId)`
  - `switchBranch(name)`
  - `switchBranch(worldId)`
- Hook:
  - `addHook(AppHook)`
  - `removeHook(AppHook)`
  - 우선순위(`priority`)
  - 이벤트 필터(`supports(eventType)`)
  - 에러 정책(`CONTINUE`, `FAIL_FAST`)
  - Hook 이벤트:
    - `onReady`
    - `onBeforeAct`
    - `onActionUpdate`
    - `onAfterAct`
    - `onBranchSwitched`

## 6. Facades (2026-02-14)

- `SystemFacade`: `system.*` 액션 실행 경계
- `MemoryFacade`: `ingest/recall` 경계

## 7. Optional Features (defer)

- Hook plugin chain 고도화(필터 조합/재시도 정책)
- Memory provider pluggability
