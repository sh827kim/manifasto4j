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

