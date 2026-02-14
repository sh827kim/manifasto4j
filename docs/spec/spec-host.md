# Manifesto Java Host SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | host integration (Java runtime using manifesto-core) |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/host/docs/VERSION-INDEX.md` |
| Latest | `2.0.2` |

## 1. Scope

This summary documents the host responsibilities when integrating Java core:

- compute-effect loop
- patch application rules
- requirement handling
- translator/compiler integration constraints

## 2. Host Responsibilities

Host MUST:

- Call core `compute()` for all semantic transitions
- Execute effects and apply resulting patches via `apply()`
- Re-run `compute()` after effects until status resolves
- Serialize intent processing per snapshot lineage
- Use the **same intentId** across lowering/evaluation and compute

Host MUST NOT:

- Mutate Snapshot directly
- Pass non-concrete expressions to core.apply()
- Include `$system.*` in Translator evaluation path
- Skip compute() between effect cycles

## 3. Patch Processing (Translator/MEL)

When processing Translator output or MEL patches:

1) Lower MEL IR → Core IR (ConditionalPatchOp)
2) Evaluate to concrete values (Patch[])
3) Apply patches via core.apply()

Key rules:

- Evaluation is total: invalid operations return null, never throw
- Conditions are boolean-only
- Patches are applied sequentially with a working snapshot

## 4. Effect Loop (Host)

- If `compute()` returns **PENDING**, host MUST execute requirements
- Apply resulting patches and clear pending requirements
- Call `compute()` again with the same intentId

## 5. Restrictions

- `$system.*` is forbidden in Translator evaluation path
- `core.apply()` only accepts concrete patches

## 6. Java Runtime Boundary Status (2026-02-11)

- Java host now separates runtime boundaries into:
  - `ExecutionKey`
  - `HostMailbox` (in-memory FIFO)
  - `HostRunner` (single-runner drain)
  - `HostJob` (`StartIntent`, `ContinueCompute`, `FulfillRequirements`)
- Execution remains synchronous in-process, but the contract boundary is aligned for future TS event-loop parity.


## 7. Context Provider + Effect Executor Boundary (2026-02-14, Cycle 6)

- Host effect 실행 경계 보강:
  - `EffectExecutionContext`, `EffectContextProvider`
  - `ContextAwareEffectHandler`
  - `EffectExecutor` + `EffectExecutionOutcome/Error`
- `HostRuntimeOptions` 확장:
  - `contextProvider` 옵션 추가(기본 provider 제공)
- trace 확장:
  - `effect:attempt`, `effect:retry`, `effect:success`, `effect:failure` 이벤트 추가
- host 에러 payload 확장:
  - 기존 `HOST_EFFECT_FAILED` 유지
  - 세부 필드(`effectErrorCode`, `effectAttempts`, `effectRetryable`) 추가
