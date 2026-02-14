# Manifesto Java App FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/app/docs/VERSION-INDEX.md` |
| Latest | `2.3.1` |
| Status | Draft (Java port) |
| Scope | app facade design notes |

## 1. Goals

- Provide a stable server/CLI API surface
- Encapsulate host compute-loop and effect handling
- Keep core unchanged and deterministic

## 2. Key Decisions

### 2.1 Minimal App for Server/CLI
Java App starts with a minimal subset:

- createApp + ready + act + subscribe
- no UI bindings (React excluded)
- world/store integrations are optional in default profile

### 2.2 ActionHandle as Observable
Action execution should return a handle for status/result/trace.
This supports CLI progress reporting and server logs.

### 2.3 Action Phase History
`ActionHandle` now stores explicit phase updates (`ActionUpdate`) so runtime transitions can be inspected after execution.
This closes the gap between minimal synchronous execution and lifecycle observability requirements.

## 3. Deferred Areas

- Branch/session management
- Plugins/hooks
- Full system action catalog
