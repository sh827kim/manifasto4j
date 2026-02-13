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
