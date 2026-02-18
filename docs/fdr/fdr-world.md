# Manifesto Java World FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/world/docs/VERSION-INDEX.md` |
| Latest | `2.0.3` |
| Status | Draft (Java port) |
| Scope | governance layer design notes |

## 1. Goals

- Provide auditable governance semantics
- Preserve lineage and decision traceability

## 2. Follow-ups

- 구현 완료 (2026-02-14, Cycle 6):
  - event sink query 계층(`InMemoryWorldEventJournal`) 추가
  - persistence query 계약(`WorldStore.listProposalsByStatus`) 추가
  - world integration 테스트에 event/query/persistence 통합 케이스 추가
- 다음 단계:
  - ingress/epoch 시나리오를 골든 벡터로 확장
  - authority 전이 edge-case를 fixture 기반으로 강화

- 구현 완료 (2026-02-14, TASK-B1):
  - `EdgeQuery` 기반 edge filter/limit query 계약 추가
  - `ObservableWorldStore` + store event 타입/리스너 계약 추가
  - `StoreStats` 계약 및 `MemoryWorldStore` 통계 구현 추가
  - persistence query/event/stats 회귀 테스트 보강

- 구현 완료 (2026-02-18, TASK-F2):
  - `WorldErrorCode` 기반 persistence 오류 taxonomy 정비
  - `StoreResult.errorCode` 확장 및 `MemoryWorldStore` 실패 코드 매핑
  - `StoreBatchResult`/`StoreBatching` 도입 + `WorldStore` default batch API 추가
  - batch/result 오류코드 회귀 테스트 보강
