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
| core | pure compute/apply/validate/explain + expr/evaluator | broad expr/evaluator/schema/runtime utils implemented | Strong Partial | 84% -> 90% |
| host | mailbox/runner/job/context/effects/errors | runtime+job+runner+trace + context-provider/effect-executor boundary implemented | Strong Partial | 76% -> 86% |
| world | authority/registry/proposal/lineage/ingress/events/persistence | major governance runtime + event query/persistence filter contract implemented | Strong Partial | 80% -> 88% |
| compiler | lexer/parser/analyzer/generator/lowering/eval/api/renderer | broad compiler pipeline implemented | Strong Partial | 79% -> 90% |
| intent-ir | schema/canonical/keys/lexicon/resolver/lower | schema/validator/lower + keys/lexicon/resolver advanced baseline | Partial | 62% -> 85% |
| translator | core pipeline/plugins/strategies/invariants/helpers + adapters/targets | core models + strategies/helpers/invariants + adapter SPI + target exporters + export orchestration | Strong Partial | 78% -> 88% |
| codegen | plugin runner/types/path-safety/hash/header/virtual-fs/plugins | plugin runner + 2 Java plugins + runtime utilities(path/hash/header/vfs) + detailed run contract | Strong Partial | 80% -> 90% |

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
| effect registry/executor | Yes | dedicated `EffectExecutor` + retry/timeout/error boundary | Partial |
| context-provider/execution-context | Yes | `EffectContextProvider` + `EffectExecutionContext` | Partial |
| host error model | Yes | error code + effect failure details(payload) | Partial |

Priority actions:
1. context-aware handler 도입 범위를 plugin/adapter 경계까지 확대
2. effect diagnostics를 typed trace/report 구조로 확장
3. host context-provider를 app/session 정책과 연동

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
| events/query/store contract | Yes | event journal + proposal status filter까지 반영 | Partial |

Priority actions:
1. ingress epoch edge-case를 golden fixture로 확장
2. authority timeout/escalation 전이 케이스 추가
3. world query API의 정렬/limit/filter 계약 명시화

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
- core: interpreter/verifier/refiner + pipeline/plugins + policy providers + intent-ir lower bridge + strategies/helpers/invariants
- adapter SPI: provider profile/mapper/normalizer(OpenAI/Ollama/Claude) with provider-neutral contracts
- targets: json/manifesto/openapi exporter implementations + `DefaultTranslator.translateAndExport()`

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| pipeline stages | Yes | Yes | Partial |
| plugin hooks | Yes | Yes | Partial |
| policy verification | Yes | Yes | Partial |
| strategies (decompose/translate/merge) | Yes | Yes (baseline) | Partial |
| invariants/helpers | Yes | Yes (baseline) | Partial |
| adapter family | Yes | provider-neutral SPI + profile/mapper/normalizer | Partial |
| target exporters | Yes | json/manifesto/openapi implemented | Partial |

Priority actions:
1. exporter 결과의 snapshot/golden 회귀 테스트 강화
2. adapter 실연동 구현체를 외부 모듈로 분리하고 계약 테스트 연동
3. manifesto exporter lowering failure taxonomy/diagnostics 정밀화

---

### 3.8 Codegen (`packages/codegen/src`)

TS shape:
- core: `types`, `runner`, `plugins`, `path-safety`, `stable-hash`, `header`, `virtual-fs`

Java mapping:
- module: `manifesto-codegen`
- `CodegenPlugin`, `CodegenPluginRegistry`, `CodegenRunner`, `JavaDto`, `JavaTypedClient`
- runtime utility: `PathSafety`, `StableHash`, `HeaderGenerator`, `VirtualFileSystem`
- detailed run contract: `CodegenExecutionOptions`, `CodegenPluginOptions`, `CodegenRunResult`

| Capability | TS | Java | Status |
| --- | --- | --- | --- |
| plugin runner | Yes | Yes | Partial |
| plugin implementations | Yes | 2 plugins | Partial |
| path safety | Yes | Implemented | Partial |
| stable hash | Yes | Implemented | Partial |
| header policy | Yes | Implemented | Partial |
| virtual fs | Yes | Implemented | Partial |

Priority actions:
1. plugin 옵션을 각 generator에 실제 반영(현재 계약/검증 중심)
2. TS plugin ecosystem(`ts/zod`) 대응 generator 확장

## 4. Test Shape Gap

| Module | TS test signal | Java test signal | Gap |
| --- | --- | --- | --- |
| app | 32 | 3 | High |
| core | 11 | 17 | Low/Medium |
| host | 33 | 4 | High |
| world | 9 | 13 | Low/Medium |
| compiler | 7 | 13 | Low/Medium |
| intent-ir | 6 | 3 | Medium/High |
| translator | 12 | 9 | Medium |
| codegen | 9 | 7 | Medium |

## 5. Status Labels
- `Complete`: TS shape 핵심 계약 대응 완료
- `Strong Partial`: 주요 축 구현 + edge/maturity 보강 필요
- `Partial`: 핵심 축 일부 구현, 확장 필요
- `Missing`: 대응 구조/기능 부재

## 6. Immediate Execution Focus (Post Cycle 7)
1. `checkGoldenSync` N/A 정책 문서화 및 TS vector 재도입 대비 복구 절차 정의
2. App/Host 고갭 테스트 표면 확장 (TS test shape 대비)
3. Intent-IR/Translator/Codegen 잔여 계약 테스트 강화
