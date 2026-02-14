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
