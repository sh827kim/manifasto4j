# Manifesto Java World SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | governance layer semantics |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/world/docs/VERSION-INDEX.md` |
| Latest | `2.0.5` |

## 1. Scope

World defines governance: proposals, authority checks, decisions, and lineage DAG.

## 2. Responsibilities

- Proposal submission and validation
- Authority evaluation
- Decision recording
- Worldline DAG integrity
- Actor registry contracts


## 3. Event / Query / Persistence Contracts (2026-02-14, Cycle 6)

- event query sink 추가:
  - `InMemoryWorldEventJournal` (`queryByType`, `querySince`, `listAll`, `clear`)
- persistence query 계약 확장:
  - `WorldStore.listProposalsByStatus(status)` default API 추가
- 통합 검증:
  - world proposal lifecycle + event journal + proposal status query를 통합 테스트로 검증
