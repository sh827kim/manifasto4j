# Manifesto Java Core FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/core/docs/VERSION-INDEX.md` |
| Latest | `2.0.1` |
| Status | Draft (Java port) |
| Scope | manifesto-core (Java) |

## 1. Goals

- Match TypeScript core semantics
- Preserve determinism and immutability
- Keep zero external dependencies in core

## 2. Key Decisions

### 2.1 Sealed Classes for Unions
TypeScript unions are modeled as Java sealed classes (FlowNode, ExprNode, Patch, Result).
This provides compile-time exhaustiveness.

### 2.2 Result Monad for Errors
All errors are values (`Result<T,E>` + `ErrorValue`).
No exceptions are used for business logic.

### 2.3 Deterministic Trace IDs
Trace IDs are generated from `TraceContext` with a monotonic counter.
This preserves determinism for the same evaluation order.

### 2.4 Inline Array Effects
`array.map` / `array.filter` are treated as pure effects and applied immediately
as patches to snapshot data (no host).

### 2.5 Schema Hash
Schema hash uses canonical JSON + SHA-256 via `ValidationUtils.computeSchemaHash`.

## 3. Deviations from TS

- Java includes `TraceReplay` as a minimal validation utility. TS does not
  define Replay explicitly.
- Java exposes Apply/Validate/Compute as explicit core classes; TS groups
  these by module functions.

## 4. Risks / Follow-ups

- SPEC/FDR for non-core packages remains unported.
- Some runtime behaviors (host integration) are not modeled in Java core.
- Document alignment should be re-validated after major updates.

