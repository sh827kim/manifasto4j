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

## Must Update (P0)
1. `onceIntent` runtime path compatibility (`$mel`):
   - Java `Apply` path validation must accept platform namespace, not only `$host`.
2. Schema hash policy:
   - Align with TS semantic hash policy (`$`-prefixed fields excluded for semantic mode).
3. Golden sync default path:
   - Use `/workspace/manifasto-ts-core` as default TS repo root.

## Must Update (P1)
1. App runtime/store parity:
   - Canonicalization and platform namespace filtering in storage/delta pipeline.
2. Host runtime parity:
   - Align toward mailbox/runner/job model boundaries.
3. World parity:
   - Head query / resume / branch persistence contracts.

## Progress Update (2026-02-11)
1. P1-1 (App runtime/store parity) 1차 반영 완료:
   - `InMemoryAppSnapshotStore` 저장 시 snapshot canonicalization 적용
   - `InMemoryAppSnapshotStore` load 경계 방어적 복사 적용
   - `MemoryWorldStore` snapshot save/load 경계에도 동일 정책 적용
   - 플랫폼 네임스페이스(`$*`)는 저장 경계에서 필터링
   - 회귀 테스트 추가:
     - `InMemoryAppSnapshotStoreTest`
     - `MemoryWorldStoreTest` platform namespace case
2. P1-3 (World parity: head/resume/branch contract) 1차 반영 완료:
   - `ManifestoWorld`에 head tracker 성격의 API 추가:
     - `getCurrentHeadWorldId()`
     - `getGenesisWorldId()`
     - `isInitialized()`
     - `resume(WorldId)`
   - world 생성/실행/branch 전환 시 head 갱신 규칙 반영
   - 회귀 테스트 추가:
     - `ManifestoWorldTest.resumeSetsCurrentHeadToExistingWorld`
     - `ManifestoWorldTest.resumeRejectsMissingWorld`
3. P1-2 (Host runtime parity: mailbox/runner/job boundary) 1차 반영 완료:
   - `manifesto-host.runtime` 경계 타입 추가:
     - `ExecutionKey`
     - `HostMailbox` / `InMemoryHostMailbox`
     - `HostJob` (`StartIntent`, `ContinueCompute`, `FulfillRequirements`)
     - `HostRunner` / `HostRunnerState`
   - `HostRuntime` 실행 경로를 job 기반으로 리팩터링:
     - StartIntent enqueue
     - ContinueCompute(반복 상한/compute 경계)
     - FulfillRequirements(effect 실행 + patch apply + continue enqueue)
   - 기존 host 동작 계약 유지:
     - missing handler 시 `PENDING` 반환
     - effect 실패 시 `$host.lastError/errors` 기록 후 `ERROR` 반환
   - 회귀 테스트 추가:
     - `InMemoryHostMailboxTest`
     - `HostRunnerTest`

## New Work (P1~P2)
1. Add `manifesto-intent-ir` module skeleton.
2. Add `manifesto-translator` module skeleton.
3. Add `manifesto-codegen` module skeleton.

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
