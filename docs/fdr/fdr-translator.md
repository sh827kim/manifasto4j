# Manifesto Java Translator FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/translator/core/docs/translator-FDR-v0.11.md` |
| Latest | `0.11 / 1.0.3 spec line` |
| Status | Bootstrap completed (Java skeleton) |
| Scope | translator design notes |

## 1. Goals

- Provide consistent intent/patch generation
- Maintain safety via verification stages

## 2. Follow-ups

- Bootstrap 완료:
  - `TranslatorMessageAdapter<TExternalMessage>` 인터페이스 추가
  - `Translator`/`TranslationRequest`/`TranslationResult` 계약 추가
- 다음 단계:
  - interpret/verify/refine 단계 파이프라인 구현
  - adapter capability test contract 정의
