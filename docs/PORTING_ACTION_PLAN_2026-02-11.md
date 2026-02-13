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
4. P1 compiler runtime 계층 정식화 완료 (2026-02-13):
   - `LoweringLite` → `Lowering`, `RuntimePatchEvaluatorLite` → `RuntimePatchEvaluator`, `IrGeneratorLite` → `IrGenerator`로 명칭 통일
   - strict 경계 입력 검증 보강(`null` 방어, 옵션/컨텍스트 필수 검증)
   - `RuntimePatchEvaluator` 결과 타입 불변화(방어적 복사 + unmodifiable 반환)
5. P1 compiler evaluator TS parity 보강 완료 (2026-02-13):
   - 누락 연산(`substring`, `field`, `keys`, `values`, `entries`) 구현
   - `at(record, string)` 지원 추가
   - runtime patch skip reason 계약을 TS와 동일하게 정렬(`false|null|non-boolean`)
6. P2 module bootstrap 완료 (2026-02-13):
   - `manifesto-intent-ir` 모듈 생성 및 정규화 계약(`IntentIrNormalizer`) 추가
   - `manifesto-translator` 모듈 생성 및 프레임워크 비종속 인터페이스(`TranslatorMessageAdapter`, `Translator`) 추가
   - `manifesto-codegen` 모듈 생성 및 출력 계약(`CodeGenerator`, `GeneratedArtifact`) 추가

## Remaining Work (P1~P2)
1. Host HCTS 계약 보강 (P1):
   - event-loop trace/reinjection/liveness invariant 확장
2. Planned module implementation (P2):
   - `manifesto-intent-ir` canonical serialization/hash 경계 구현
   - `manifesto-translator` stage 파이프라인(interpret/verify/refine) 기본 구현
   - `manifesto-codegen` 첫 타깃(Java DTO/typed client) 구현

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
