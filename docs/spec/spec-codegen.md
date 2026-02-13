# Manifesto Java Codegen SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Bootstrap completed (Java skeleton) |
| Scope | DomainSchema 기반 코드 생성 계약 |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/codegen` |

## 1. Scope

Codegen은 schema 입력을 받아 대상 플랫폼 코드 산출물 목록으로 변환하는 계약을 정의합니다.

## 2. Responsibilities

- 코드 생성 타깃(`CodegenTarget`) 식별
- 생성 요청(`CodegenRequest`) 입력 검증
- 생성 산출물(`GeneratedArtifact`) 반환 계약 유지
- 템플릿/렌더러 구현과 계약(API) 분리

## 3. First Target Baseline (2026-02-13)

- `JavaDtoCodeGenerator`를 추가해 `java-dto` 타깃을 기본 지원한다.
- 입력 계약: `schema.state.fields` map 기반 최소 스키마.
- 출력 계약: `StateDto.java` 단일 산출물 생성(패키지 경로 기준 상대 경로).

## 4. Typed Client Baseline (2026-02-13)

- `JavaTypedClientCodeGenerator`를 추가해 `java-typed-client` 타깃을 지원한다.
- 입력 계약: `schema.actions.*.input.fields` map 기반 action 입력 스키마.
- 출력 계약:
  - `<Domain>Client.java` 인터페이스
  - `<Action>Input.java` DTO (action별 1개)
