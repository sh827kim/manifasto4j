# Manifesto Java Core SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | manifesto-core (Java) |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/core/docs/VERSION-INDEX.md` |
| Latest | `2.0.1` |

## 1. Scope

This document summarizes the Java port behavior for the core engine only:

- DomainSchema / Snapshot / Intent / Result / ErrorValue
- Compute / Apply / Validate
- Flow / Expr evaluation
- TraceGraph / TraceNode / Explain

Non-core packages (host, world, react, etc.) are out of scope.

## 2. Determinism Rules

The Java core is deterministic:

- No IO, no randomness (except deterministic UUID via trace context)
- No time access for logic (timestamps only for trace/meta)
- No mutable shared state
- All errors are values (`ErrorValue`)

## 3. Core Data Structures

- **Snapshot**: immutable state (`data`, `computed`, `system`, `input`, `meta`)
- **Intent**: action request (`type`, `input`, `intentId`)
- **Patch**: SET/UNSET/MERGE operations
- **Result**: functional error container
- **TraceNode/TraceGraph**: computation trace

## 4. Compute Pipeline (Java)

`Compute.compute(schema, snapshot, intent)` executes these steps:

1) Evaluate computed fields (DAG order)
2) Find ActionSpec by intent type
3) Validate intent id
4) Validate input fields
5) Evaluate action availability
6) Prepare snapshot (input + system status)
7) Build evaluation context
8) Evaluate Flow
9) Re-evaluate computed fields
10) Update system/meta + build trace

## 5. Validation Rules (Java)

The following validation rules are enforced in Java:

- V-001: Computed deps must exist
- V-002: Computed dependency graph must be acyclic
- V-003: Expr get paths must exist and be allowed
- V-004: Flow call references must exist
- V-005: Flow call graph must be acyclic
- V-008: Schema hash must match canonical hash

## 6. Flow Evaluation

Flow nodes:

- Seq, If, Patch, Effect, Call, Halt, Fail

Key behaviors:

- Patch: applied immediately to snapshot
- Effect: declared only, produces Requirement + PENDING status
- array.map / array.filter effects: inline patch (no host)
- Call: resolves action and evaluates target Flow

## 7. Expression Evaluation

Expr nodes are immutable and pure. Supported categories:

- literal / comparison / logical / arithmetic / string
- collection (map/filter/reduce etc.)
- object
- type

Evaluation uses `EvalContext` with `$item/$index/$array` for collection contexts.

## 8. Trace & Explain

- Flow/Expr evaluations emit `TraceNode` and are assembled into `TraceGraph`
- `TraceBuilder` flattens trace nodes into `id -> TraceNode` map
- `TraceRecorder` is a helper for consistent trace node creation
- `TraceReplay` (Java) validates structural integrity of a TraceGraph
- `Explain` can produce trace for a specific path (data/computed/input/system)

## 9. Known Gaps (relative to TS spec)

- Only core Java spec is summarized here
- Full SPEC/FDRs for other packages are not ported yet

