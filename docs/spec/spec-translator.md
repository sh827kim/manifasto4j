# Manifesto Java Translator SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Bootstrap completed (Java skeleton) |
| Scope | natural language → semantic intents/patches |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/translator/core/docs/translator-SPEC-v1.0.3.md` |

## 1. Scope

Translator defines a multi-stage pipeline for converting NL input into intents/patches.

## 2. Responsibilities

- Schema-guided interpretation
- Validation and verification
- Deterministic output where possible
- LLM integration boundaries

## 3. Java Bootstrap Contract (2026-02-13)

- 프레임워크 종속 구현체는 기본 모듈에 포함하지 않는다.
- `TranslatorMessageAdapter<TExternalMessage>` 인터페이스로 외부 SDK 메시지 변환 경계를 고정한다.
- `Translator` 인터페이스는 `TranslationRequest`를 받아 `TranslationResult(Intent IR + diagnostics)`를 반환한다.

## 4. Pipeline Baseline (2026-02-13)

- 기본 파이프라인은 `interpret -> verify -> refine` 3단계를 순차 실행한다.
- 기본 구현:
  - `RuleBasedInterpreter`
  - `DefaultTranslatorVerifier`
  - `DefaultTranslatorRefiner`
  - `DefaultTranslator`
- 이 파이프라인은 프레임워크 비종속 계약 위에서 동작하며, 이후 LLM adapter 구현체는 교체 가능하다.

## 5. Adapter Capability Contract (2026-02-13)

- `TranslatorAdapterCapabilityValidator`를 통해 adapter 최소 호환성을 점검한다.
- 점검 항목:
  - translator message round-trip(role/content/attributes) 보존
  - external message round-trip 크기/구조 보존
- 결과는 `TranslatorAdapterCapabilityReport`로 반환되며 CI 테스트에 바로 사용할 수 있다.

## 6. Domain Policy Ruleset (2026-02-13)

- `TranslatorPolicyProvider`로 도메인별 정책 룰셋을 주입할 수 있다.
- 기본 verifier는 다음 정책을 추가 검증한다.
  - `TRV101`: 도메인 허용 action 목록 위반
  - `TRV102`: 도메인 필수 context key 누락
- 기본 구현체로 `InMemoryTranslatorPolicyProvider`를 제공한다.

## 7. Pipeline/Plugin Architecture (2026-02-14)

- `TranslatorPipeline` 실행기를 도입해 `interpret -> verify -> refine`를 명시적 stage로 분리했다.
- `TranslatorPipelinePlugin` hook 계약을 추가했다.
  - `before/after interpret`
  - `before/after verify`
  - `before/after refine`
- `DefaultTranslator`는 파이프라인 실행기에 위임하며, plugin 목록 주입을 지원한다.

## 8. Intent-IR Bridge Plugin (2026-02-14)

- `IntentIrResolutionPlugin` 추가:
  - verify 이후 Intent-IR resolver/lexicon을 적용해 draft 보정
  - resolver/lexicon 진단 코드를 translator diagnostics에 병합
  - `lexiconValid` 메타 플래그를 결과 Intent IR에 반영
  - optional `IntentIrLowerer`를 주입하면 lower 결과(input/meta/action)를 translator draft에 반영

## 9. External Policy Provider + Reload (2026-02-14)

- `TranslatorPolicyProvider` 계약 확장:
  - `snapshot()` 현재 정책 맵 조회
  - `reload()` 정책 재로딩
- `FileTranslatorPolicyProvider` 추가:
  - 파일 기반 정책 로딩(`*.allowedActions`, `*.requiredContextKeys`)
  - 런타임 재로딩 지원

## 10. Core Models + Strategies + Invariants (2026-02-14, Cycle 3)

- core 모델 추가:
  - `Span`, `Chunk`
  - `IntentGraphNode`, `DependencyEdge`, `IntentGraph`
  - `ExecutionStep`, `ExecutionPlan`
  - `TranslatorDiagnostic`, `GraphValidationResult`
- strategies 계층 추가:
  - `DecomposeStrategy`, `TranslateStrategy`, `MergeStrategy`
  - `SentenceWindowDecomposeStrategy`
  - `DeterministicGraphTranslateStrategy`
  - `ConservativeMergeStrategy`, `AggressiveMergeStrategy`
  - `StrategyComposer`
- helpers + invariants 추가:
  - helpers: `TranslatorChunkValidator`, `TranslatorGraphValidator`, `ExecutionPlanBuilder`
  - invariants: `CausalIntegrityChecker`, `CompletenessChecker`, `StatefulnessChecker`, `ReferentialIdentityChecker`, `AbstractDependencyChecker`

## 11. Pipeline Options & Plugin Ordering (2026-02-14, Cycle 3)

- `TranslatorPipelineOptions` 추가:
  - diagnostics aggregation policy
  - plugin priority sorting 옵션
- `TranslatorDiagnosticsBag` 도입:
  - `PRESERVE`, `DEDUP` 정책 지원
- `TranslatorPipelinePlugin` 확장:
  - `priority()`
  - `type()` (`INSPECTOR`, `TRANSFORMER`)
- `DefaultTranslator`가 options 주입 생성자를 지원하도록 확장

## 12. Adapter SPI + Target Exporters (2026-02-14, Cycle 4)

- adapter SPI 추가:
  - `adapters.spi`: `LlmPort`, `LlmRequest`, `LlmResponse`, `LlmCallOptions`, `LlmException` 등
  - `adapters.spi.provider`: `ProviderCapabilityProfile`, `ProviderRequestMapper`, `ProviderResponseNormalizer`, `ProviderAdapterBinding`
- provider profile 구현 추가:
  - `adapters.profile`: OpenAI/Ollama/Claude capability profile + mapper + normalizer
  - 네트워크 SDK 직접 의존 없이 payload mapping/normalization 계약만 코어에 반영
- target exporter 계약 추가:
  - `targets`: `ExportInput`, `TargetExporter`, `TargetExporters`
  - `targets.json`: `JsonTargetExporter`
  - `targets.manifesto`: `ManifestoTargetExporter`, `ManifestoBundle`, `InvocationPlan/Step`, `LoweringResult`
  - `targets.openapi`: `OpenApiTargetExporter`

## 13. Translation + Export Orchestration (2026-02-14, Cycle 4)

- `DefaultTranslator` 확장:
  - `translateAndExport(request, exporter, context)` 추가
  - 실행 순서: `strategy compose -> graph validator + invariant suite -> pipeline run -> target export`
- 통합 결과 모델:
  - `TranslatorExportResult<TOut>` (translation result + graph + execution plan + graph diagnostics + exported output)
- 테스트:
  - adapter contract test
  - target exporter test(json/manifesto/openapi)
  - multi-target integration test
