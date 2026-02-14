# TS Parity Matrix (2026-02-14)

## 1. Baseline
- TS source baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java source baseline: `/workspace/manifesto-java-core` @ `main`
- Assessment axis:
  1. Public API/export surface
  2. Runtime behavior boundary
  3. Test coverage shape

## 2. Module Summary

| Module | TS Surface Signal | Java Surface Signal | Status | Progress Target |
| --- | --- | --- | --- | --- |
| app | `runtime/hooks/execution/storage` + broad types | lifecycle/session/branch/hook/system/memory baseline | Partial | 48% -> 80% |
| core | pure compute/apply/validate/explain + expr/evaluator | broad expr/evaluator/schema/runtime utils implemented | Strong Partial | 82% -> 90% |
| host | mailbox/runner/job/context/effects/errors | runtime+job+runner+trace contract implemented | Partial | 68% -> 85% |
| world | authority/registry/proposal/lineage/ingress/events/persistence | major governance runtime implemented | Strong Partial | 74% -> 85% |
| compiler | lexer/parser/analyzer/generator/lowering/eval/api/renderer | broad compiler pipeline implemented | Strong Partial | 76% -> 90% |
| intent-ir | schema/canonical/keys/lexicon/resolver/lower | schema/validator/lower + keys/lexicon/resolver advanced baseline | Partial | 62% -> 85% |
| translator | core pipeline/plugins/strategies/invariants/helpers + adapters/targets | pipeline/plugins/policy/provider + intent-ir lower bridge baseline | Partial | 48% -> 80% |
| codegen | plugin runner/types/path-safety/hash/header/virtual-fs/plugins | plugin runner + 2 Java plugins baseline | Partial | 58% -> 85% |

## 3. Detailed Mapping

### 3.1 App (`packages/app/src`)

TS shape:
- top dirs: `bootstrap`, `core`, `execution`, `hooks`, `runtime`, `storage`, `errors`
- exported role: createApp facade + lifecycle/action/session/branch/state/hook/memory/system contracts

Java mapping:
- module: `manifesto-app`
- files: `App`, `DefaultApp`, `AppFactory`, `ActionHandle`, `ActionPhase`, `ActionUpdate`, `AppHook`, `AppSnapshotStore`

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| App facade (`createApp`) | Yes | Yes | Complete |
| lifecycle ready/dispose/status | Broad | `ready/dispose/status` baseline | Partial |
| action handle phases/updates | Yes | Yes (history + phase) | Partial |
| session abstraction | Yes | basic session id + persistence flag | Partial |
| branch API | broad | list/current/switch baseline | Partial |
| hook system | broad | priority/filter/error mode baseline | Partial |
| memory facade | broad | enabled/disabled baseline | Partial |
| system facade | broad | `system.*` dispatch baseline | Partial |

Priority actions:
1. `ActionResult` -> 세부 failure/rejection semantics 고도화
2. session context가 runtime 정책에 연결되는 경로 확장
3. branch alias를 world lineage fork semantics로 연결
4. memory/system facade TS 스펙 수준 확장

---

### 3.2 Core (`packages/core/src`)

TS shape:
- exported role: `compute`, `computeSync`, `apply`, `validate`, `explain` + schema/utils/evaluator

Java mapping:
- module: `manifesto-core`
- broad coverage of expr nodes, evaluator, flow, schema, trace, hashing, canonicalization

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| compute/apply/validate/explain API | Yes | Yes | Complete |
| schema model | Yes | Yes | Complete |
| evaluator + expr ops | Yes | Yes (광범위) | Strong Partial |
| explain traceability | Yes | Yes | Partial |
| regression tests | moderate | strong local tests | Strong Partial |

Priority actions:
1. explain/validate edge-case TS parity vectors 추가
2. TS 신규 연산자 추가 시 parity check 자동화

---

### 3.3 Host (`packages/host/src`)

TS shape:
- top dirs: `types`, `job-handlers`, `effects`, mailbox/runner/context/execution APIs

Java mapping:
- module: `manifesto-host`
- `HostRuntime`, `HostRunner`, mailbox/job types, trace events

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| mailbox + runner model | Yes | Yes | Partial |
| job typed execution | Yes | Yes | Partial |
| effect registry/executor | Yes | Basic handler map | Partial |
| context-provider/execution-context | Yes | Minimal | Partial |
| host error model | Yes | Limited | Partial |

Priority actions:
1. effect registry/executor 분리
2. host error code model 고도화
3. context-provider 경계 보강

---

### 3.4 World (`packages/world/src`)

TS shape:
- top dirs: `authority`, `events`, `ingress`, `lineage`, `persistence`, `proposal`, `registry`, `types`

Java mapping:
- module: `manifesto-world`
- governance runtime (proposal, decision, lineage, authority, store) implemented

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| proposal lifecycle | Yes | Yes | Strong Partial |
| authority handlers | Yes | Yes (auto/hitl/policy/tribunal) | Partial |
| lineage + branching | Yes | Yes | Strong Partial |
| ingress context/epoch | Yes | Basic epoch flows | Partial |
| events/query/store contract | Yes | Largely present | Partial |

Priority actions:
1. ingress context parity 강화
2. authority state transition edge-case 강화
3. query filtering/contract 고도화

---

### 3.5 Compiler (`packages/compiler/src`)

TS shape:
- top dirs: `lexer`, `parser`, `analyzer`, `diagnostics`, `generator`, `renderer`, `lowering`, `evaluation`, `api`, plus `cli/loader`

Java mapping:
- module: `manifesto-compiler`
- broad parser/analyzer/lowering/evaluation/renderer/compiler facade implemented

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| lexer/parser/analyzer | Yes | Yes | Strong Partial |
| diagnostics | Yes | Yes | Partial |
| lowering/evaluation | Yes | Yes | Strong Partial |
| compile API | Yes | Yes | Partial |
| cli/loader/module boundary | Yes | Limited | Partial |

Priority actions:
1. loader/module API parity 확대
2. renderer/cli edge-case 정합

---

### 3.6 Intent-IR (`packages/intent-ir/src`)

TS shape:
- top dirs: `schema`, `canonical`, `keys`, `lexicon`, `resolver`, `lower`

Java mapping:
- module: `manifesto-intent-ir`
- `IntentIrDocument`, normalizer, canonicalizer, hashing, key deriver, lexicon/resolver, schema validator, lowerer

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| canonicalization | Yes | Yes | Partial |
| hashing | Yes | Yes | Complete |
| keys (strict/semantic/sim) | Yes | Yes | Partial |
| lexicon | Yes | Basic | Partial |
| resolver | Yes | Basic | Partial |
| lower | Yes | Yes (baseline) | Partial |
| full schema model | Yes | head/term/predicate/event/resolved baseline | Partial |

Priority actions:
1. schema feature model(role/theta/selectional restriction) 확장
2. lower 결과를 host/app/world 경계와 통합
3. lexicon/resolver discourse 정책 고도화

---

### 3.7 Translator (`packages/translator`)

TS shape:
- core dirs: `core`, `helpers`, `invariants`, `pipeline`, `plugins`, `strategies`
- package family: adapters(openai/ollama/claude), targets(json/manifesto/openapi)

Java mapping:
- module: `manifesto-translator`
- core: interpreter/verifier/refiner + pipeline/plugins + policy providers + intent-ir lower bridge

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| pipeline stages | Yes | Yes | Partial |
| plugin hooks | Yes | Yes | Partial |
| policy verification | Yes | Yes | Partial |
| strategies (decompose/translate/merge) | Yes | Limited | Missing/Partial |
| invariants/helpers | Yes | Limited | Missing |
| adapter family | Yes | interface-level only | Missing/Partial |
| target exporters | Yes | Missing | Missing |

Priority actions:
1. strategy set 확장
2. invariant/helper 구현
3. adapter/target module 분리 구현

---

### 3.8 Codegen (`packages/codegen/src`)

TS shape:
- core: `types`, `runner`, `plugins`, `path-safety`, `stable-hash`, `header`, `virtual-fs`

Java mapping:
- module: `manifesto-codegen`
- `CodegenPlugin`, `CodegenPluginRegistry`, `CodegenRunner`, `JavaDto`, `JavaTypedClient`

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| plugin runner | Yes | Yes | Partial |
| plugin implementations | Yes | 2 plugins | Partial |
| path safety | Yes | Missing | Missing |
| stable hash | Yes | Missing | Missing |
| header policy | Yes | Missing | Missing |
| virtual fs | Yes | Missing | Missing |

Priority actions:
1. path safety/stable hash/header 계층 추가
2. virtual fs + integration snapshot test 추가

## 4. Test Shape Gap

| Module | TS test signal | Java test signal | Gap |
| --- | --- | --- | --- |
| app | 32 | 3 | High |
| core | 11 | 17 | Low/Medium |
| host | 33 | 4 | High |
| world | 9 | 11 | Medium |
| compiler | 7 | 13 | Low/Medium |
| intent-ir | 6 | 3 | Medium/High |
| translator | 12 | 5 | Medium/High |
| codegen | 9 | 3 | High |

## 5. Status Labels
- `Complete`: TS shape 핵심 계약 대응 완료
- `Strong Partial`: 주요 축 구현 + edge/maturity 보강 필요
- `Partial`: 핵심 축 일부 구현, 확장 필요
- `Missing`: 대응 구조/기능 부재

## 6. Immediate Execution Focus (Cycle 1)
1. App 세부 semantics 고도화(action result/rejection/session runtime wiring)
2. Parity matrix를 기준으로 각 기능 단위 test TODO 생성
3. 다음 사이클에서 intent-ir lower + translator invariants 착수
