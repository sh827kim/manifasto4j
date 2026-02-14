# Manifesto Java Codegen FDR (Porting)

| Field | Value |
| --- | --- |
| Status | Bootstrap completed (Java skeleton) |
| Scope | codegen 설계 메모 |

## 1. Goals

- schema 기반 코드 생성 공통 계약을 Java 모듈로 고정
- 타깃별 구현체를 모듈 외부/추가 모듈에서 주입 가능하도록 유지

## 2. Follow-ups

- 구현 완료:
  - `JavaDtoCodeGenerator`로 첫 타깃(`java-dto`) 1차 구현
  - codegen 단위 테스트 추가
- 추가 완료:
  - `JavaTypedClientCodeGenerator`로 typed client 타깃(`java-typed-client`) 1차 구현
  - typed client 단위 테스트 추가
- 추가 완료 (2026-02-14):
  - `CodegenPlugin`/`CodegenPluginRegistry`/`CodegenRunner` 도입
  - 기존 Java DTO/typed-client 생성기를 plugin으로 편입
- 추가 완료 (2026-02-14, Cycle 5):
  - runtime utility 계층 도입(`path-safety`, `stable-hash`, `header`, `virtual-fs`)
  - `CodegenRunner.generateDetailed()` + `CodegenRunResult` 도입
  - plugin 옵션 계약(`CodegenPluginOptions`, `CodegenExecutionOptions`) 도입
  - utility/runner 통합 테스트(path safety/hash/vfs/integration) 증설
- 추가 완료 (2026-02-14, TASK-B4):
  - multi-plugin sequential mode PoC(`CodegenRunner.generateComposite`) 추가
  - ordered target 실행 결과를 단일 VFS에서 합성하는 경계 구현
  - composite 실행 통합 테스트 추가
- 다음 단계:
  - snapshot/golden 테스트를 target별 fixture 기반으로 확장
