# Manifesto Java App SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | Java SDK facade for server/CLI (`manifesto-sdk` + `manifesto-runtime`) |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/sdk/src/index.ts` |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/runtime/src/types/app.ts` |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/docs/internals/adr/008-sdk-first-transition-and-app-retirement.md` |
| Latest | SDK `1.2.0`, Runtime `0.1.2` (TS `c7a47aa` 기준) |

## 1. Scope

TS 기준에서 `@manifesto-ai/app`는 retire 되었고, `@manifesto-ai/sdk`가 canonical public entry다.
Java도 동일하게 `manifesto-sdk`를 canonical public entry로 사용한다.

Java App should provide:

- SDK-level `createApp(...)` entry semantics
- `app.ready()` explicit initialization
- `act(intent)` with ActionHandle lifecycle
- `subscribe(state selector)`
- world/system/memory facade boundary

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

## 8. SDK/Runtime Contract Parity Map (Rebaseline, 2026-02-18)

TS SDK/Runtime 공개 계약(`packages/sdk/src/index.ts`, `packages/runtime/src/types/app.ts`) 대비 Java sdk/runtime 기준의 매핑:

| TS 계약군 | Java 대응 | 상태 |
| --- | --- | --- |
| SDK canonical entry (`createApp`, `createTestApp`) | `AppFactory.createApp(...)`, `AppFactory.createWorldApp(...)` | 부분 반영 |
| App lifecycle (`status`, `ready`, `dispose`) | `App.getStatus()`, `ready()`, `dispose()` | 반영 |
| Action API (`act`, `ActionHandle`, `getActionHandle`) | `act(Intent)`, `ActionHandle` | 부분 반영 |
| Session/Branch surface | `createSession`, `listBranches`, `switchBranch` | 부분 반영 |
| World query (`getWorld`, `getSnapshot(worldId)`, `getHeads`) | `getWorld()`, branch/world 조회 API 일부 | 부분 반영 |
| System/Memory facade | `SystemFacade`, `MemoryFacade` | 반영 |
| SDK effects-first config (`schema + effects`) | `AppFactory.createApp(schema, initialData, effects)` / `createTestApp(...)` + `AppConfig.sdk(...)` | 반영 |

비고:
- 본 문서는 app 기준이 아닌 **sdk/runtime 기준 parity 문서**로 재기준화되었다.
- Java canonical 진입점은 `manifesto-sdk`이며 runtime 구현은 `manifesto-runtime`에 위치한다.

## 9. Immediate Gaps (P0)

- `getHeads/getLatestHead`는 노출되었고 기본 정렬 동작을 제공하지만, TS의 world-query 보장 범위와의 세부 의미동치 검증은 추가 필요
- `manifesto-sdk`가 runtime 타입(`ai.manifesto.app.*`)을 직접 재노출하는 구간은 후속 단계에서 sdk 전용 타입으로 정리 필요
