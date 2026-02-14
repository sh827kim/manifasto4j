# Manifesto Java Compiler FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/compiler/docs/VERSION-INDEX.md` |
| Latest | `0.5.0` |
| Status | Draft (Java port) |
| Scope | MEL compiler design notes |

## 1. Goals

- Provide MEL → DomainSchema compilation
- Ensure deterministic lowering to core IR
- Emit clear diagnostics

## 2. Follow-ups

- Decide if Java port includes full MEL compiler or delegates to TS service
- Define IR compatibility tests against core

## 3. Current Action Checklist (2026-02-13)

- [x] TS 대비 evaluator 누락 연산 보강: `substring`, `field`, `keys`, `values`, `entries`
- [x] `at(record, string)` 경로 조회 동작 보강
- [x] runtime patch skip reason 계약 정렬(`false|null|non-boolean`)
- [x] compiler vectors/unit test에 parity case 추가
- [x] `MelSourceLoader`(file/classpath) + `CompilerFacade` 연계 API 추가
- [x] renderer edge-case 회귀 테스트 추가(unknown/malformed/newline/indent)
- [ ] TS 최신 구조 기준 `checkGoldenSync` vector 경로 재정의
