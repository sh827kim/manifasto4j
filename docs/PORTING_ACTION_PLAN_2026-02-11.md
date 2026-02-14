# Java Porting Action Plan (2026-02-11)

## Scope
- TS reference: `/workspace/manifasto-ts-core`
- Java target: `/workspace/manifesto-java-core`
- Latest checked TS commit: `3b40070` (reviewed on 2026-02-14)

## TS Package Baseline
- Active packages in TS: `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`
- Translator is a package family:
  - `@manifesto-ai/translator`
  - `@manifesto-ai/translator-adapter-*`
  - `@manifesto-ai/translator-target-*`

## Recent TS Delta Check
- Compared range: `754d860..3b40070`
- Result: docs/ADR/SPEC changes only
  - Added Runtime SPEC draft (`packages/runtime/docs/runtime-SPEC-v0.1.0.md`)
  - Added SDK SPEC draft (`packages/sdk/docs/sdk-SPEC-v0.1.0.md`)
- No direct behavior change in `packages/*/src` for parity-critical modules

## Java Modules
- Active parity modules:
  - `manifesto-core`, `manifesto-host`, `manifesto-app`, `manifesto-world`, `manifesto-compiler`
- Planned modules (baseline already bootstrapped):
  - `manifesto-intent-ir`, `manifesto-translator`, `manifesto-codegen`

## Baseline Completed (through 2026-02-13)
1. P0 core parity 완료
2. P1 store/world parity 완료
3. P1 host parity 1차 완료
4. P1 compiler runtime/evaluator parity 보강 완료
5. P2 module bootstrap 완료 (`intent-ir`, `translator`, `codegen`)
6. P1 host HCTS 계약 보강 완료
7. P2 intent-ir canonical/hash 경계 구현 완료
8. P2 translator 파이프라인 1차 구현 완료
9. P2 translator verifier/policy 룰셋 1차 구현 완료
10. P2 codegen `java-dto` / `java-typed-client` 1차 구현 완료

## In-Progress Priorities (2026-02-14)
1. P1 App parity (완료):
   - ActionHandle phase/update lifecycle 계약 추가
   - world/non-world 경로 상태 전이 기록
2. P1 Intent-IR parity (완료):
   - strict/semantic/sim key derivation
   - lexicon/resolver 최소 코어
3. P1 Translator parity (완료):
   - pipeline/plugin 기반 구조로 확장
   - interpreter/verifier/refiner 조합을 stage hook으로 확장 가능화

## Remaining Work
1. P2 Translator:
   - 정책 소스 외부화(DB/Config Service) 및 hot-reload 전략
2. P2 Codegen:
   - plugin runner 중심 구조(직접 렌더링/템플릿 엔진 전략 확정)
3. P1 App:
   - session/branch/hook API 계약 확장(현재는 lifecycle 최소 계약 중심)

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
