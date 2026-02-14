# TS Parity Progress Report (2026-02-14)

## 1. Baseline & Method
- TS baseline: `/workspace/manifasto-ts-core` at `3b40070`
- Java baseline: `/workspace/manifesto-java-core` current `main`
- This report is based on **current TS code shape** (`packages/*/src` structure, exported responsibilities, test surface), not recent commit history alone.

### 1.1 Shape Snapshot (Source/Test file count)
| Module | TS src | TS test | Java src | Java test |
| --- | ---: | ---: | ---: | ---: |
| app | 123 | 32 | 9 | 3 |
| core | 45 | 11 | 102 | 17 |
| host | 54 | 33 | 17 | 4 |
| world | 47 | 9 | 66 | 11 |
| compiler | 49 | 7 | 88 | 13 |
| intent-ir | 34 | 6 | 12 | 3 |
| translator (core+adapters+targets) | 82 | 12 | 22 | 5 |
| codegen | 19 | 9 | 9 | 3 |

## 2. Parity Assessment (TS shape-based)

| Module | Progress | Assessment |
| --- | ---: | --- |
| core | 82% | 계산 엔진/표현식/적용 경계는 강함. TS 대비 고수준 정합 양호. |
| host | 68% | mailbox/runner/job 경계와 trace 계약은 반영. TS host의 더 넓은 테스트/컨텍스트 조합은 추가 필요. |
| world | 74% | proposal/authority/lineage/persistence 축은 비교적 탄탄. TS world의 세부 ingress/이벤트 조합 확장 여지 존재. |
| compiler | 76% | lexer/parser/analyzer/lowering/evaluation 핵심 축 반영. TS compiler의 API/CLI/loader 주변 기능은 추가 여지. |
| app | 34% | 최근 Action lifecycle + session/branch/hook 계약은 진전. TS app의 runtime/memory/system/session/facade 폭 대비 아직 큰 격차. |
| intent-ir | 46% | canonical/hash + key derivation + lexicon/resolver 최소 코어 추가. TS의 schema/lower/keys 전체 스펙 폭 대비는 중간 이하. |
| translator | 42% | interpret-verify-refine + pipeline/plugin + policy provider/reload 반영. TS의 strategy/invariants/helpers + adapter/target family 대비 격차 큼. |
| codegen | 58% | java-dto/java-typed-client + plugin runner 구조 도입. TS codegen의 virtual-fs/path-safety/plugin ecosystem 대비 미완. |

## 3. Overall Progress
- Weighted by TS module source shape: **약 55% 완료**
- 의미:
  - `core/host/world/compiler`는 실사용 가능한 포팅 기반 확보
  - `app/intent-ir/translator/codegen`은 구조는 올라왔으나 TS 전체 형상 대비 확장 단계

## 4. Major Gaps vs TS Shape
1. App
- TS `packages/app/src`의 `runtime`, `memory`, `system`, `hooks`, `execution`, `storage` 폭 대비 Java는 아직 최소 facade 중심.

2. Translator
- TS translator는 `pipeline/plugins/strategies/invariants/helpers` + adapter/target 패밀리 구조.
- Java는 core pipeline은 생겼지만 invariant/helper 전략, target exporter, adapter family는 제한적.

3. Intent-IR
- TS는 `schema`, `keys`, `lexicon`, `resolver`, `lower`가 완결된 패키지 구조.
- Java는 lower/schema 스펙 전체 구현보다 key/lexicon/resolver 최소 코어 중심.

4. Codegen
- TS는 plugin ecosystem + virtual-fs + path-safety + stable-hash를 포함.
- Java는 runner/registry까지 도달했지만 virtual fs/safety/hash 레이어는 미구현.

## 5. Recommended Next Sequence
1. App (P1)
- session API 고도화, hook 에러 격리/우선순위, system/memory facade 경계 확대

2. Translator (P2)
- invariant/helper 레이어 추가, target exporter 계약 추가, adapter/target 모듈 분화

3. Intent-IR (P2)
- lower/schema 계약 확장, key/lexicon/resolver와 translator 연동 테스트 강화

4. Codegen (P2)
- path safety/stable hash/virtual fs 계층 도입, plugin 옵션 계약 확장

## 6. Confidence
- Confidence: **Medium**
- Reason: 구조/계층/테스트 표면 기반으로 정량화했으며, 모든 TS 세부 동작을 라인 단위로 1:1 실행 비교한 값은 아님.
