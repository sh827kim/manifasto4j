# Manifesto Java Bridge FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/archives/manifesto-ai-bridge__v1.2.0__FDR-1.1.0v.md` |
| Latest | `1.1.0` |
| Status | Draft (Java port) |
| Scope | projection layer design notes |

## 1. Goals

- Project external triggers into semantic Intent
- Keep projection deterministic and non-authoritative
- Provide SnapshotView delivery and ActionCatalog support

## 2. Key Decisions

- Treat Bridge as a generalized projection layer (not UI-only)
- SnapshotView excludes meta/system to avoid leakage
- ActionCatalog used for DX/LLM context, not authorization

## 3. Follow-ups

- Define Java interface for projection inputs/outputs
- Decide how to serialize ActionCatalog in server/CLI contexts

