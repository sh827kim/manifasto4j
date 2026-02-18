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

## 4. Persistence Parity Extension (2026-02-14, TASK-B1)

- query 계약 확장:
  - `EdgeQuery` 추가 (`fromWorldId`, `toWorldId`, `proposalId`, `decisionId`, `limit`)
  - `WorldStore.listEdges(query)` default 필터 계약 추가
- observable 계약 확장:
  - `ObservableWorldStore`, `StoreEventType`, `StoreEvent`, `StoreEventListener` 추가
  - `MemoryWorldStore`에서 typed/global subscription 지원
- stats 계약 확장:
  - `StoreStats` 추가
  - `WorldStore.getStats()` 및 `MemoryWorldStore` count 기반 구현 제공

## 5. Persistence Contract Tightening (Cycle 10 / TASK-F2)

- persistence 결과 타입에 오류 taxonomy를 명시한다.
  - `WorldErrorCode` 도입
  - `StoreResult`에 `errorCode` 포함
- 배치 계약/유틸리티를 도입한다.
  - `StoreBatchResult`, `StoreBatching`
  - `WorldStore` default batch API (`saveWorldBatch`, `saveEdgeBatch`, `saveProposalBatch`)
- `MemoryWorldStore`는 실패 케이스에 표준 오류 코드를 반환한다.
