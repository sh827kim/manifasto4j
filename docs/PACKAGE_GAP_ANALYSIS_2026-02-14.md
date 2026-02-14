# TS-Java Package Gap Analysis (2026-02-14)

## 1. Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `1245a5b`
- Method:
  - package source/test volume comparison
  - exported surface (`src/index.ts`) vs Java public type surface comparison
  - test scenario density comparison

## 2. Quantitative Snapshot

| Package | TS impl | TS test | Java impl | Java test | Gap Signal |
| --- | ---: | ---: | ---: | ---: | --- |
| app | 91 | 32 | 40 | 4 | **High** |
| host | 21 | 33 | 27 | 7 | **High** |
| world | 38 | 9 | 70 | 14 | Medium |
| intent-ir | 28 | 6 | 23 | 4 | Medium |
| translator | 65 | 12 | 106 | 10 | Medium |
| codegen | 10 | 9 | 26 | 7 | Medium |
| compiler | 42 | 7 | 90 | 16 | Low-Medium |
| core | 34 | 11 | 102 | 18 | Low |

## 3. Package-by-Package Gap Findings

### 3.1 `app` (Priority: P0)
- Evidence:
  - TS: `packages/app/src/index.ts`, `packages/app/src/__tests__/*`
  - Java: `manifesto-app/src/main/java/ai/manifesto/app/*`, `manifesto-app/src/test/java/ai/manifesto/app/*`
- Gaps:
  - TS public runtime/sdk-facing surface가 훨씬 넓음 (host-executor, policy presets, app-ref/hook callbacks, world-store 옵션군).
  - error taxonomy breadth 차이 큼.
  - 테스트 격차가 가장 큼 (32 vs 4).
- Next focus:
  - API parity map을 먼저 확정하고, 누락 계약을 타입/예외/옵션으로 정식화.

### 3.2 `host` (Priority: P0)
- Evidence:
  - TS: `packages/host/src/__tests__/compliance/suite/*`, `__tests__/golden/*`
  - Java: `manifesto-host/src/test/java/ai/manifesto/host/*`
- Gaps:
  - Java도 runtime 구성요소는 있으나 compliance/golden scenario 밀도 부족.
  - TS 수준의 mailbox/job/runner/liveness/ordering 재현 테스트가 아직 적음.
- Next focus:
  - TS compliance suite 카테고리를 Java 테스트 클래스로 1:1 매핑.

### 3.3 `world` (Priority: P1)
- Evidence:
  - TS: `packages/world/src/persistence/interface.ts`, `packages/world/src/world.ts`
  - Java: `manifesto-world/src/main/java/ai/manifesto/world/*`
- Gaps:
  - ingress/query hardening은 진행 완료되었지만, TS의 `EdgeQuery`, `ObservableWorldStore`, `getStats` 등 일부 persistence 보조 계약이 미흡.
  - store event subscription 계층이 TS 대비 단순.
- Next focus:
  - persistence query/event utility parity 보강.

### 3.4 `intent-ir` (Priority: P1)
- Evidence:
  - TS: `packages/intent-ir/src/index.ts`, `packages/intent-ir/src/__tests__/*`
  - Java: `manifesto-intent-ir/src/main/java/ai/manifesto/intentir/*`
- Gaps:
  - 핵심 구성(canonical/keys/lexicon/resolver/lower)은 존재하나 TS 노출 타입/유틸 breadth가 좁음.
  - resolver/lexicon edge case 테스트 밀도 보강 여지.
- Next focus:
  - TS 테스트 케이스 매핑 기반 회귀 벡터 추가.

### 3.5 `translator` (Priority: P1)
- Evidence:
  - TS: `packages/translator/core/src/index.ts`, `packages/translator/targets/*`, `packages/translator/adapters/*`
  - Java: `manifesto-translator/src/main/java/ai/manifesto/translator/*`
- Gaps:
  - Java는 core/targets/spi/profile이 풍부하지만, TS의 concrete adapter(OpenAI/Ollama/Claude)와 동일한 실행 경로는 아직 없음.
  - conformance plugin test는 있으나 TS conformance suite 항목별 매핑 문서가 부족.
- Next focus:
  - adapter는 프레임워크 종속 없이 SPI + transport 추상화 경계에서 parity 확보.

### 3.6 `codegen` (Priority: P1)
- Evidence:
  - TS: `packages/codegen/src/runner.ts`, `packages/codegen/src/plugins/*`
  - Java: `manifesto-codegen/src/main/java/ai/manifesto/codegen/*`
- Gaps:
  - Java는 target-dispatch 중심, TS는 multi-plugin sequential runner 중심.
  - 옵션 반영(P1-C)은 완료되었으나 TS의 plugin artifacts 집계/연쇄 실행 모델과는 설계 차이 존재.
- Next focus:
  - Java에서 multi-plugin mode를 옵션으로 추가할지 결정 필요.

### 3.7 `compiler` (Priority: P2)
- Evidence:
  - TS: `packages/compiler/src/cli/*`, `packages/compiler/src/__tests__/*`
  - Java: `manifesto-compiler/src/main/java/ai/manifesto/compiler/*`
- Gaps:
  - Java는 내부 컴파일/렌더링 풍부하나, TS `cli` 모듈처럼 독립 실행면은 제한적.
  - `checkGoldenSync`는 strict 모드 추가 완료, TS vector 재도입 전까지는 기본 N/A 정책.
- Next focus:
  - 실제 CLI entrypoint(실행 커맨드) + strict CI lane 분리.

### 3.8 `core` (Priority: P2)
- Evidence:
  - TS: `packages/core/src/index.ts`
  - Java: `manifesto-core/src/main/java/ai/manifesto/core/*`
- Gaps:
  - 기능 격차보다는 API 표현 차이(함수형 export vs Java class 중심) 수준.
  - 현재는 높은 우선순위 갭 없음.
- Next focus:
  - cross-module integration에서 회귀 감시.

## 4. Stepwise Task Breakdown (Execution Order)

### Stage A — P0 (핵심 격차 해소)
1. `TASK-A1 (app)` TS app export contract parity map 작성 + Java 누락 API/오류/옵션 계약 추가.
2. `TASK-A2 (app)` app 테스트 확장: lifecycle/action/session/branch/policy/memory/resume-recovery.
3. `TASK-A3 (host)` host compliance suite 포팅: mailbox/job/runner/ordering/liveness/handler/context.
4. `TASK-A4 (host)` host golden scenarios 정규화: determinism/trace-snapshot/complex-effects/todo-workflow.

### Stage B — P1 (설계 깊이/확장성)
5. `TASK-B1 (world)` persistence parity: `EdgeQuery`, observable events, stats API.
6. `TASK-B2 (intent-ir)` TS test parity 기반 resolver/lexicon/lower edge-case 회귀 확장.
7. `TASK-B3 (translator)` conformance matrix 문서화 + adapter SPI/transport 실행 경계 보강.
8. `TASK-B4 (codegen)` multi-plugin sequential mode 설계 결정 및 PoC.

### Stage C — P2 (보조면/운영면)
9. `TASK-C1 (compiler)` CLI entrypoint 제공(compile/format/check) + strict golden lane.
10. `TASK-C2 (core)` core/app/host/world 통합 회귀 시나리오 정리 및 안정화.

## 5. Immediate Next Action Recommendation
- 바로 착수 순서:
  1) `TASK-A1` (app contract map)
  2) `TASK-A3` (host compliance suite skeleton)
  3) `TASK-B1` (world persistence parity)

