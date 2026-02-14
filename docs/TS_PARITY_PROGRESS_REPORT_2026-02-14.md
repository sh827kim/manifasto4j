# TS Parity Progress Report (2026-02-14)

## 1. Baseline & Method
- TS baseline: `/workspace/manifasto-ts-core` at `3b40070`
- Java baseline: `/workspace/manifesto-java-core` current `main`
- This report is based on **current TS code shape** (`packages/*/src` structure, exported responsibilities, test surface), not recent commit history alone.

### 1.1 Shape Snapshot (Source/Test file count)
| Module | TS src | TS test | Java src | Java test |
| --- | ---: | ---: | ---: | ---: |
| app | 123 | 32 | 25 | 3 |
| core | 45 | 11 | 102 | 17 |
| host | 54 | 33 | 17 | 4 |
| world | 47 | 9 | 66 | 11 |
| compiler | 49 | 7 | 88 | 13 |
| intent-ir | 34 | 6 | 12 | 3 |
| translator (core+adapters+targets) | 82 | 12 | 101 | 9 |
| codegen | 19 | 9 | 26 | 7 |

## 2. Parity Assessment (TS shape-based)

| Module | Progress | Assessment |
| --- | ---: | --- |
| core | 82% | 계산 엔진/표현식/적용 경계는 강함. TS 대비 고수준 정합 양호. |
| host | 68% | mailbox/runner/job 경계와 trace 계약은 반영. TS host의 더 넓은 테스트/컨텍스트 조합은 추가 필요. |
| world | 74% | proposal/authority/lineage/persistence 축은 비교적 탄탄. TS world의 세부 ingress/이벤트 조합 확장 여지 존재. |
| compiler | 76% | lexer/parser/analyzer/lowering/evaluation 핵심 축 반영. TS compiler의 API/CLI/loader 주변 기능은 추가 여지. |
| app | 48% | ActionResult/AppStatus, session/branch alias, hook priority/filter/error mode, system/memory facade baseline 반영. |
| intent-ir | 62% | schema validator + lower layer + key/lexicon/resolver 고도화 반영. lower 통합 범위 확장이 다음 과제. |
| translator | 78% | core + adapter SPI + target exporters(json/manifesto/openapi) + translate/export 통합 경로 반영. exporter 회귀 고도화가 핵심 과제. |
| codegen | 80% | java-dto/java-typed-client + plugin runner + path-safety/stable-hash/header/virtual-fs + detailed run contract 반영. plugin 옵션 실반영 확대가 다음 과제. |

## 3. Overall Progress
- Weighted by TS module source shape: **약 70% 완료**
- 의미:
  - `core/host/world/compiler`는 실사용 가능한 포팅 기반 확보
  - `app/intent-ir/translator/codegen`은 구조는 올라왔으나 TS 전체 형상 대비 확장 단계

## 4. Major Gaps vs TS Shape
1. App
- TS `packages/app/src`의 `runtime`, `memory`, `system`, `hooks`, `execution`, `storage` 폭 대비 Java는 아직 최소 facade 중심.

2. Translator
- TS translator는 `pipeline/plugins/strategies/invariants/helpers` + adapter/target 패밀리 구조.
- Java는 adapter SPI + json/manifesto/openapi exporter까지 반영되어 구조 갭이 축소되었고, 잔여 과제는 회귀/정밀도 영역.

3. Intent-IR
- TS는 `schema`, `keys`, `lexicon`, `resolver`, `lower`가 완결된 패키지 구조.
- Java는 schema/lower baseline은 반영했으나 feature-level 모델 확장이 필요.

4. Codegen
- TS는 plugin ecosystem + virtual-fs + path-safety + stable-hash를 포함.
- Java는 utility/runtime 계층(path-safety/stable-hash/header/virtual-fs)과 detailed runner contract까지 반영됨.
- 잔여 과제는 plugin ecosystem 확장(ts/zod 등)과 옵션 실반영 범위 확대.

## 5. Recommended Next Sequence
1. Host/World (P2)
- Phase 6 기준 context-provider/effect 경계 + authority/ingress/event/persistence 테스트 보강

2. App (P2)
- runtime/session/branch/hook/system 테스트 표면 확대

3. Intent-IR (P2)
- lower/schema 계약 확장, key/lexicon/resolver와 translator 연동 테스트 강화

4. Compiler/Core (P2)
- loader/renderer edge-case와 explain/validate parity vector 보강

## 6. Confidence
- Confidence: **Medium**
- Reason: 구조/계층/테스트 표면 기반으로 정량화했으며, 모든 TS 세부 동작을 라인 단위로 1:1 실행 비교한 값은 아님.
