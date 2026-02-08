# Manifesto Java Bridge SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | intent projection & snapshot view delivery |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/archives/manifesto-ai-bridge__v1.2.0__SPEC-1.1.0v.md` |
| Latest | `1.1.0` |

## 1. Scope

Bridge defines how external events are projected into Intent and how SnapshotView is delivered.

## 2. SourceEvent (Not UI-only)

SourceEvent kinds include:
- ui
- api
- agent
- system

Bridge is a general projection layer, not UI-specific.

## 3. Intent & Projection

- Intent is a command, not an event
- Projection is a weak interpreter (deterministic, no domain logic)
- Projection can read SnapshotView (data + computed only)
- Intent identity uses intentId (instance) and intentKey (semantic)

## 4. Action Catalog

- Bridge can output ActionCatalog for context injection
- Availability predicates must be deterministic
- Action catalog is not a security boundary

## 5. Determinism

- Projection must be deterministic
- occurredAt must not affect semantic projection

