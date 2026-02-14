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

## 5. Plugin Runner Architecture (2026-02-14)

- `CodegenPlugin` 계약 추가:
  - `pluginId()`
  - `supports(target)`
  - `generate(request)`
- `CodegenPluginRegistry`로 plugin 등록/조회 경계 분리
- `CodegenRunner`로 target 기반 plugin dispatch 표준화
- 기본 runner(`CodegenRunner.withDefaults`)는 다음 plugin을 내장:
  - `JavaDtoCodeGenerator`
  - `JavaTypedClientCodeGenerator`

## 6. Utility Layer Parity (2026-02-14, Cycle 5)

- TS `path-safety`, `stable-hash`, `header`, `virtual-fs` 유틸을 Java에 대응 구현:
  - `runtime/PathSafety`, `PathValidationResult`
  - `runtime/StableHash`
  - `runtime/HeaderGenerator`, `HeaderOptions`
  - `runtime/VirtualFileSystem`, `FilePatch`
- 경로 정책:
  - POSIX 상대경로만 허용
  - `..` traversal, absolute path, drive-letter, null-byte 차단

## 7. Runner Detailed Execution Contract (2026-02-14, Cycle 5)

- `CodegenRunner.generateDetailed(request, options)` 추가:
  - schema stable hash 계산
  - generated header prepend(옵션)
  - path safety 검증
  - virtual-fs patch 충돌 규칙 적용
  - `CodegenRunResult(files/diagnostics/schemaHash/pluginOptions)` 반환
- 기존 `generate(request)`는 호환 유지:
  - 내부적으로 detailed API를 사용
  - error diagnostics 존재 시 예외로 실패 처리

## 8. Plugin Option Contract (2026-02-14, Cycle 5)

- `CodegenPluginOptions` 추가:
  - `naming` (`CAMEL_CASE`, `SNAKE_CASE`, `PASCAL_CASE`)
  - `nullability` (`STRICT`, `RELAXED`)
  - `style` (`STANDARD`, `COMPACT`)
- `CodegenExecutionOptions` 추가:
  - `sourceId`, `stamp`, `prependGeneratedHeader`, `pluginOptions`
- 옵션 누락/불량 입력은 diagnostics error로 수집

## 9. Multi-Plugin Sequential Mode (2026-02-14, TASK-B4)

- TS runner model과의 정렬을 위해 Java runner에 `generateComposite(...)`를 추가했다.
- 계약:
  - 입력으로 ordered target 목록을 받아 plugin을 순차 실행
  - 공통 `VirtualFileSystem`에 patch를 합성하고 collision/path-safety 규칙을 동일 적용
  - `CodegenRunResult`로 통합 files/diagnostics를 반환
- PoC 검증:
  - `CodegenRunnerIntegrationTest.generateCompositeRunsPluginsSequentially`
  - `java-dto` + `java-typed-client` 순차 실행 결과를 단일 산출물 집합으로 검증
