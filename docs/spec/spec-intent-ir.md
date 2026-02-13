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
