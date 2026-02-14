# Manifesto Java Intent-IR SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Bootstrap completed (Java skeleton) |
| Scope | intent intermediate representation |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/intent-ir/docs/VERSION-INDEX.md` |
| Latest | `0.2.0` |

## 1. Scope

Intent-IR defines a normalized representation for intents between translation and execution.

## 2. Responsibilities

- Normalize intent structure
- Provide deterministic serialization
- Support adapter/translator pipelines

## 3. Java Bootstrap Contract (2026-02-13)

- `IntentIrDocument`를 표준 전달 모델로 사용
- `IntentIrNormalizer`로 정규화 진입점 고정
- 기본 구현(`DefaultIntentIrNormalizer`)은 필수 필드 검증 + key 정렬 복사를 제공

## 4. Canonical/Hash Boundary (2026-02-13)

- `IntentIrCanonicalizer`는 정규화된 Intent IR을 canonical JSON으로 직렬화한다.
- `IntentIrHashing`은 canonical JSON의 SHA-256 해시를 생성한다.
- 동일 의미 입력은 동일 해시, 값 변화는 해시 변화로 검증한다.

## 5. Key Derivation + Lexicon/Resolver (2026-02-14)

- `IntentIrKeyDeriver` 추가:
  - strict key: canonical 전체 payload 기반 해시
  - semantic key: volatile meta(`requestId`, `traceId`, `timestamp` 등) 제외 해시
  - sim key: 토큰 기반 64-bit simhash
- `IntentIrLexicon` / `DefaultIntentIrLexicon` 추가:
  - 도메인별 허용 action 집합 검증
  - 진단 코드(`LXC001~LXC004`) 반환
- `IntentIrResolver` / `DefaultIntentIrResolver` 추가:
  - `unknown` action 보정(`meta.actionHint` 우선, lexicon fallback)
  - 진단 코드(`RSV001~RSV004`) 반환

## 6. Schema Layer + Validator (2026-02-14)

- `schema` 패키지 도입:
  - `IntentIrHead`, `IntentIrTerm`, `IntentIrPredicate`, `IntentIrEvent`, `ResolvedIntentIr`
- `IntentIrSchemaValidator` 추가:
  - document 구조 검증 (`IRS001~IRS005`)
  - resolved 구조 검증 (`IRS101~IRS105`)

## 7. Lower Layer (2026-02-14)

- `IntentIrLowerer` / `DefaultIntentIrLowerer` 추가
- `IntentIrLowerResult` 추가
- lower 결과는 실행 경계용 action/input/meta payload를 제공하고 diagnostics를 포함한다.

## 8. Lexicon/Resolver Advanced Rules (2026-02-14)

- `IntentIrLexiconPolicy` 추가:
  - `allowedActions`
  - `requiredInputKeys`
  - `requiredMetaKeys`
- lexicon 진단 확장:
  - `LXC005` required input key missing
  - `LXC006` required meta key missing
- resolver 규칙 확장:
  - `focusAction` 우선
  - `discourseActions` fallback
  - actionHint 충돌 진단(`RSV007`)

## 9. B2 Edge-Case Parity Mapping (2026-02-14)

TS `intent-ir` 테스트 축(`schema/canonical/keys/resolver/lexicon/lower`) 대비 Java 회귀 매핑:

- lexicon:
  - selectional restriction 위반(`LXC009`)
  - role type 누락 시 selectional 검사 불가(`LXC008`)
- resolver:
  - focus/discourse/actionHint 우선순위 복구
  - 단서 부재 시 unresolved 진단(`RSV003`, `RSV010`)
- lower:
  - unresolved action(`unknown`) lowering warning(`LRW001`)
  - blank 필드는 normalizer 단계에서 즉시 거절(예외)
