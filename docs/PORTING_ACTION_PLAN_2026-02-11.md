# Java Porting Action Plan (2026-02-11)

## Scope
- TS reference: `/workspace/manifasto-ts-core`
- Java target: `/workspace/manifesto-java-core`

## TS Package Baseline
- Active packages in TS: `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`
- Translator is a package family:
  - `@manifesto-ai/translator`
  - `@manifesto-ai/translator-adapter-*`
  - `@manifesto-ai/translator-target-*`

## Java Current Modules
- `manifesto-core`, `manifesto-host`, `manifesto-app`, `manifesto-world`, `manifesto-compiler`

## Java Planned Modules
- `manifesto-intent-ir`
- `manifesto-translator`
- `manifesto-codegen`

## Cleanup Status
1. Removed out-of-scope modules from the Gradle build graph.
2. Removed out-of-scope spec/fdr documents from `docs/spec` and `docs/fdr`.
3. Rewritten README and learning docs to only track active/planned parity scope.

## Baseline Completed (2026-02-11)
1. P0 core parity 완료:
   - `Apply` 경계에서 플랫폼 namespace(`$mel` 포함) patch 허용
   - semantic/effective schema hash 정책 분리 반영
   - golden sync 기본 TS 경로를 `/workspace/manifasto-ts-core`로 고정
2. P1 store/world parity 완료:
   - app/world 저장 경계 canonicalization + 플랫폼 namespace filtering 반영
   - `ManifestoWorld` head/resume/branch persistence 계약 반영
3. P1 host parity 1차 완료:
   - mailbox/runner/job 경계 타입 도입 및 `HostRuntime` job 기반 실행 경로 반영

## Remaining Work (P1~P2)
1. Compiler strict parity 강화 (P1):
   - TS strict lowering/evaluation 오류 코드/검증 포인트 동치화
2. Host HCTS 계약 보강 (P1):
   - event-loop trace/reinjection/liveness invariant 확장
3. Planned module bootstrap (P2):
   - `manifesto-intent-ir` 스켈레톤 진입
   - `manifesto-translator` 프레임워크 비종속 인터페이스 계약 정의
   - `manifesto-codegen` 스켈레톤 및 출력 계약 정의

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
