# Manifesto Java App FDR (Porting)

| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/sdk/src/index.ts` |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/runtime/src/types/app.ts` |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/docs/internals/adr/008-sdk-first-transition-and-app-retirement.md` |
| Latest | SDK `1.2.0`, Runtime `0.1.2` |
| Status | Draft (Java port) |
| Scope | Java SDK facade design notes (`manifesto-sdk` + `manifesto-runtime`) |

## 1. Goals

- Provide a stable server/CLI API surface
- Encapsulate host compute-loop and effect handling
- Keep core unchanged and deterministic
- Align public contract interpretation with TS SDK-first policy

## 2. Key Decisions

### 2.1 SDK-first Alignment Policy
- TS에서 `@manifesto-ai/app`는 retire 되었고 `@manifesto-ai/sdk`가 canonical entry다.
- Java도 canonical entry를 `manifesto-sdk`로 맞추고, runtime 구현은 `manifesto-runtime`에 분리한다.
- 따라서 Java App 관련 설계/갭 평가는 `packages/sdk` + `packages/runtime` 계약 기준으로 수행한다.
- `AppConfig`는 legacy 필드(`initialSnapshot`, `hostRuntime`)와 SDK-style 필드(`initialData`, `effects`)를 함께 수용한다.

### 2.2 Minimal App for Server/CLI
Java App starts with a minimal subset:

- createApp + ready + act + subscribe
- no UI bindings (React excluded)
- world/store integrations are optional in default profile
- SDK-style 진입점으로 `createApp(schema, initialData, effects)` / `createTestApp(...)` 오버로드를 제공

### 2.3 ActionHandle as Observable
Action execution should return a handle for status/result/trace.
This supports CLI progress reporting and server logs.

### 2.4 Action Phase History
`ActionHandle` now stores explicit phase updates (`ActionUpdate`) so runtime transitions can be inspected after execution.
This closes the gap between minimal synchronous execution and lifecycle observability requirements.

### 2.5 Session/Branch/Hook Contract
- Session identity/persistence exposure (`getSessionId`, `hasSessionPersistence`)
- Session execution API (`AppSession`, actor/context binding)
- Branch query/control APIs (`getCurrentBranchId`, `listBranches`, alias branch switching)
- Hook contract (`AppHook`) for ready/act/phase/branch lifecycle integration
  - priority ordering
  - event filtering
  - error isolation policy (`CONTINUE`, `FAIL_FAST`)

### 2.6 Action Result + RuntimeKind
- Action result family added:
  - `CompletedActionResult`
  - `FailedActionResult`
  - `RejectedActionResult`
  - `PreparationFailedActionResult`
- Runtime kind separation:
  - `DOMAIN`
  - `SYSTEM`

### 2.7 Facades
- `SystemFacade` for `system.*` action dispatch
- `MemoryFacade` with disabled/in-memory baseline implementations
- World query 보강: branch head 목록/최신 head 조회(`getHeads`, `getLatestHead`)

## 3. Deferred Areas

- Hook chain composability/retry policy
- Memory provider pluggability
- Full system action catalog
- sdk 전용 타입 계층(`ai.manifesto.sdk.*`) 확장 및 runtime 타입 직접 노출 축소

## 4. Cross-Module Integration Regression (2026-02-14, TASK-C2)

- 현재 `manifesto-runtime`에 core/host/world 연계 회귀 테스트를 유지한다:
  - 승인 경로에서 `App -> World -> Host -> Core` 종단 상태 전이 검증
  - branch 전환 후 snapshot 복원 및 재실행 독립성 검증
  - app policy 거절 시 world proposal 제출 전 차단 검증
- 관련 테스트: `CrossModuleIntegrationRegressionTest`
