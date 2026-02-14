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
