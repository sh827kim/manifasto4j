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
- 구현 완료:
  - `interpret -> verify -> refine` 기본 파이프라인(`DefaultTranslator`) 추가
  - rule-based interpreter 및 기본 verifier/refiner 추가
- 추가 완료:
  - `TranslatorAdapterCapabilityValidator`/`TranslatorAdapterCapabilityReport` 추가
  - verifier 정책 강화(`verified`, `verificationScore`, TRV 규칙 코드)
- 추가 완료:
  - 도메인 정책 룰셋 주입 계약(`TranslatorPolicyProvider`) 추가
  - `InMemoryTranslatorPolicyProvider` 및 정책 검증 코드(TRV101/TRV102) 반영
- 추가 완료 (2026-02-14):
  - `TranslatorPipeline` + `TranslatorPipelinePlugin` 도입
  - `DefaultTranslator`를 pipeline 기반으로 전환
  - `IntentIrResolutionPlugin` 추가(resolver/lexicon bridge)
- 추가 완료 (2026-02-14):
  - `FileTranslatorPolicyProvider` 추가(파일 기반 정책 소스)
  - 정책 provider `reload()`/`snapshot()` 계약 도입
- 추가 완료 (2026-02-14, Cycle 3):
  - translator core type(`Chunk/IntentGraph/ExecutionPlan/Diagnostics`) 도입
  - strategies 계층(`decompose/translate/merge`) + `StrategyComposer` 도입
  - helpers/invariants 계층 및 검증 테스트 추가
  - pipeline options + diagnostics aggregation + plugin priority/type 규칙 도입
- 다음 단계:
  - adapter/target 패밀리(모듈 분리) 구현 확대
