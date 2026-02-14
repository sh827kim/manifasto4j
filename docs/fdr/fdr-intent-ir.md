# Manifesto Java Intent-IR FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/intent-ir/docs/VERSION-INDEX.md` |
| Latest | `0.2.0` |
| Status | Bootstrap completed (Java skeleton) |
| Scope | intent IR design notes |

## 1. Goals

- Provide a stable intermediate form for intent pipelines

## 2. Follow-ups

- Bootstrap 완료:
  - `IntentIrDocument`, `IntentIrNormalizer`, `DefaultIntentIrNormalizer` 추가
- 구현 완료:
  - `IntentIrCanonicalizer`/`IntentIrHashing` 추가
  - canonical serialization/hash 단위 테스트 추가
- 구현 완료 (2026-02-14):
  - `IntentIrKeyDeriver` 추가(strict/semantic/sim key)
  - `DefaultIntentIrLexicon` 추가(도메인/액션 허용성 검증)
  - `DefaultIntentIrResolver` 추가(action 보정 규칙)
- 다음 단계:
  - host/app 연동 경계 테스트 추가
