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
- 구현 완료 (2026-02-14, Cycle 2):
  - `schema` 패키지 타입(`Head/Term/Predicate/Event/Resolved`) 추가
  - `IntentIrSchemaValidator` 추가(문서/해석 모델 구조 검증)
  - `IntentIrLowerer`/`DefaultIntentIrLowerer`/`IntentIrLowerResult` 추가
  - lexicon 정책 확장(`requiredInputKeys`, `requiredMetaKeys`)
  - resolver discourse/focus 우선순위 규칙 추가
- 다음 단계:
  - lower 결과를 app/world 경계와 연결하는 통합 회귀 확대
