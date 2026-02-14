# TS-Java Package Gap Analysis (Rebaseline, 2026-02-14)

## 1. Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `a5f951b`
- Re-analysis scope:
  - TS export surface (`packages/*/src/index.ts`)와 Java 공개 타입 비교
  - TS test suite 카테고리와 Java 회귀 테스트 밀도 비교
  - 완료된 `TASK-C2` 이후 남은 구조적 갭 식별

## 2. Quantitative Snapshot

| Package | TS impl | TS test | Java impl | Java test | Gap Signal |
| --- | ---: | ---: | ---: | ---: | --- |
| app | 96 | 30 | 54 | 6 | **High** |
| host | 25 | 5 | 27 | 7 | Medium |
| world | 39 | 9 | 76 | 14 | Low-Medium |
| intent-ir | 29 | 6 | 23 | 4 | Medium |
| translator | 71 | 12 | 106 | 10 | Medium |
| codegen | 11 | 8 | 26 | 7 | Medium |
| compiler | 43 | 15 | 91 | 16 | Low-Medium |
| core | 35 | 11 | 102 | 18 | Low |

## 3. Package-by-Package Findings

### 3.1 `app` (Priority: P0)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/app/src/core/types/app.ts`
  - TS: `/workspace/manifasto-ts-core/packages/app/src/index.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-app/src/main/java/ai/manifesto/app/App.java`
- Remaining gaps:
  - TS App public contract 대비 Java App의 world query/head/session/handle lookup surface가 좁다.
  - TS memory/provider/verifier/context-freezing 축이 Java에서 단순화되어 있다.
  - TS app 회귀군(정책/구독/publish-boundary/timing/spec-compliance) 대비 Java 테스트 밀도 차이가 크다.

### 3.2 `host` (Priority: P1)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/host/src/index.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-host/src/main/java/ai/manifesto/host/runtime`
- Remaining gaps:
  - Java host는 mailbox/runner/job 경계는 확보했지만 TS의 `ApplyPatchesJob` 분리와 동급의 job-stage 표현은 약하다.
  - TS trace-replay 성격의 장기 회귀 축 대비 Java 회귀는 시나리오 중심으로만 검증한다.

### 3.3 `world` (Priority: P2)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/world/src/persistence/interface.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-world/src/main/java/ai/manifesto/world/persistence/WorldStore.java`
- Remaining gaps:
  - Java는 query/event/stats는 반영했지만 TS persistence 계약의 `BatchResult`, 비동기 저장소 추상 계층, 세분화된 world error taxonomy는 단순화되어 있다.

### 3.4 `intent-ir` (Priority: P1)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/intent-ir/src/index.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-intent-ir/src/main/java/ai/manifesto/intentir`
- Remaining gaps:
  - TS canonical API(strict/semantic 분리)와 utility breadth 대비 Java 공개 API가 축약되어 있다.
  - lexicon/resolver/lower edge-case coverage는 확장되었지만 TS 스펙 전체 축 대비 public helper 집합이 적다.

### 3.5 `translator` (Priority: P1)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/translator/core/src`
  - TS: `/workspace/manifasto-ts-core/packages/translator/adapters/*`
  - Java: `/workspace/manifesto-java-core/manifesto-translator/src/main/java/ai/manifesto/translator`
- Remaining gaps:
  - Java는 framework-agnostic SPI 경계를 유지하고 있으나, TS의 concrete adapter package와 동등한 E2E payload fixture 검증 축은 없다.
  - transport/provider capability 조합 회귀 벡터를 더 늘릴 필요가 있다.

### 3.6 `codegen` (Priority: P1)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/codegen/src/runner.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-codegen/src/main/java/ai/manifesto/codegen/CodegenRunner.java`
- Remaining gaps:
  - Java는 `generateComposite`까지 반영했지만 TS runner의 outDir flush/clean 경로와 artifacts 누적 반환 계약은 아직 없다.
  - CI에서 실제 파일 산출물 경로를 검증하는 통합 시나리오가 부족하다.

### 3.7 `compiler` (Priority: P2)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/compiler/src/index.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-compiler/src/main/java/ai/manifesto/compiler/CompilerCli.java`
- Remaining gaps:
  - Java는 `compile/format/check` CLI는 확보했으나 TS API의 `parse/tokens` 노출 축은 별도 엔트리로 제공되지 않는다.
  - `checkGoldenSync` strict lane은 정상적으로 실패 경로가 확인되었지만(소스 부재), 실제 TS vector source 복구 전까지는 N/A 상태다.

### 3.8 `core` (Priority: P2)
- Evidence:
  - TS: `/workspace/manifasto-ts-core/packages/core/src/index.ts`
  - Java: `/workspace/manifesto-java-core/manifesto-core/src/main/java/ai/manifesto/core/core`
- Remaining gaps:
  - 구조적 큰 갭은 없고 cross-module regression 중심으로 유지 관리 단계.

## 4. New Cross-Package Watch Items
- TS에 신규 문서 패키지 등장:
  - `/workspace/manifasto-ts-core/packages/runtime/docs/runtime-SPEC-v0.1.0.md`
  - `/workspace/manifasto-ts-core/packages/sdk/docs/sdk-SPEC-v0.1.0.md`
- 현재는 문서만 존재(구현 코드 부재). Java는 즉시 포팅 대상이 아니라 **설계 대응 준비 항목**으로 관리하는 것이 적절하다.

## 5. Recommended Next-Cycle Focus
1. `app` 공개 계약 + 회귀 밀도 추가 확장
2. `host` job-stage parity 보강 + trace replay 축 보강
3. `intent-ir` canonical/utility API 확대
4. `codegen` 산출물 flush/artifacts 계약 보강
5. `compiler` parse/tokens 엔트리 및 strict lane 운영 문서/스크립트 고도화
