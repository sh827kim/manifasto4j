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
7. P1 host HCTS 계약 보강 완료 (2026-02-13):
   - trace 기반 single-runner invariant 검증 테스트 추가
   - chained reinjection/liveness invariant 테스트 추가
   - host golden `trace-invariants-chained-reinjection` 케이스 추가
8. P2 intent-ir canonical/hash 경계 구현 완료 (2026-02-13):
   - `IntentIrCanonicalizer`, `IntentIrHashing` 추가
   - canonical serialization/hash 안정성 테스트 추가
9. P2 translator 파이프라인 1차 구현 완료 (2026-02-13):
   - `DefaultTranslator` 기반 interpret/verify/refine 파이프라인 추가
   - rule-based interpreter + 기본 verifier/refiner 추가
   - translator 단위 테스트 추가
10. P2 codegen 첫 타깃 1차 구현 완료 (2026-02-13):
   - `JavaDtoCodeGenerator` 추가(`java-dto` target)
   - `schema.state.fields` 기반 `StateDto.java` 생성 계약 반영
   - codegen 단위 테스트 추가
11. P2 translator adapter/verifier 강화 완료 (2026-02-13):
   - `TranslatorAdapterCapabilityValidator`/`Report` 추가
   - adapter capability 단위 테스트 추가
   - verifier 정책(`verified`, `verificationScore`, TRV 규칙) 강화
12. P2 codegen typed client 1차 구현 완료 (2026-02-13):
   - `JavaTypedClientCodeGenerator` 추가(`java-typed-client` target)
   - action별 `<Action>Input` + `<Domain>Client` 산출 계약 반영
   - typed client 단위 테스트 추가
13. P2 translator 도메인 정책 룰셋 1차 완료 (2026-02-13):
   - `TranslatorPolicyProvider`/`InMemoryTranslatorPolicyProvider` 추가
   - verifier에 도메인 정책 검사(TRV101/TRV102) 반영
   - 도메인 정책 단위 테스트 추가

## Remaining Work (P1~P2)
1. Planned module implementation (P2):
   - `manifesto-translator` 정책 소스 외부화 및 hot-reload 전략 정리
   - `manifesto-codegen` 템플릿 전략(직접 렌더링 vs 템플릿 엔진) 확정

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
