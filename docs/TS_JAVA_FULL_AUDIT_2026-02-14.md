# TS/Java Full Audit Report (2026-02-14)

## 1. Baseline
- TS baseline: `/workspace/manifasto-ts-core` @ `3b40070`
- Java baseline: `/workspace/manifesto-java-core` @ `517d3ac`
- Method:
  - module directory/package shape comparison
  - exported surface comparison (`index.ts` and package structure)
  - test surface comparison (test file and scenario shape)

## 2. Module Snapshot

| Module | TS (src/test) | Java (src/test) | Assessment |
| --- | ---: | ---: | --- |
| app | 91 / 31 | 25 / 3 | High gap |
| core | 34 / 11 | 102 / 18 | Low gap |
| host | 21 / 33 | 24 / 4 | High gap (test/runtime contract) |
| world | 38 / 9 | 67 / 13 | Medium-low gap |
| compiler | 42 / 15 | 89 / 15 | Low-medium gap |
| intent-ir | 28 / 6 | 23 / 4 | Medium gap |
| translator | 65 / 12 | 101 / 9 | Medium gap |
| codegen | 10 / 9 | 26 / 7 | Medium gap |

## 3. Key Gap Findings

### 3.1 P0
1. App parity gap (highest)
- TS app exports very broad runtime/policy/memory/branch/recovery surface.
- Java app currently focuses on minimal facade + world integration baseline.
- Gap area:
  - plugin/policy/world-store/schema-compatibility/resume-recovery contract
  - memory hub/backfill/recall advanced APIs
  - broad error taxonomy parity

2. Host compliance gap
- TS host has large compliance/golden/unit suite.
- Java host has runtime core + limited tests.
- Gap area:
  - compliance suite equivalent coverage
  - explicit Host error model and scenario contracts

### 3.2 P1
1. Intent-IR semantic depth
- Java has canonical/keys/lexicon/resolver/lower structure, but rule depth is simpler than TS.
- Gap area:
  - schema feature richness and validator detail
  - resolver discourse/focus policy parity
  - lower contract edge-cases

2. Translator plugin/conformance depth
- Java has invariant/strategy/pipeline baseline and target exporters.
- Gap area:
  - TS plugin family coverage (coverage/dependency/or/task-enumeration style checks)
  - conformance scenario density

3. Codegen option materialization
- Java has options contract (`naming/nullability/style`) but partial runtime use.
- Gap area:
  - options -> generator output behavior end-to-end parity
  - snapshot-style regression expansion

### 3.3 P2
1. World ingress/query contract detail
- TS ingress/context and query contract detail is richer.
- Java world is largely implemented but needs typed ingress/query detail hardening.

2. Compiler auxiliary surface
- Java compiler core pipeline is strong.
- Remaining gap:
  - CLI/formatter/vite-like auxiliary integration surface
  - `checkGoldenSync` currently N/A mode due TS vector relocation/removal

## 4. Immediate Priority Order
1. P0-1: App API/runtime/test surface expansion
2. P0-2: Host compliance + error contract expansion
3. P1-1: Intent-IR semantic rule depth
4. P1-2: Translator plugin/conformance depth
5. P1-3: Codegen options materialization
6. P2-1: World ingress/query detail hardening
7. P2-2: Compiler auxiliary surface and golden policy stabilization

## 5. Validation Commands
- `./gradlew :manifesto-app:test`
- `./gradlew :manifesto-host:test`
- `./gradlew :manifesto-intent-ir:test`
- `./gradlew :manifesto-translator:test`
- `./gradlew :manifesto-codegen:test`
- `./gradlew :manifesto-world:test`
- `./gradlew :manifesto-compiler:test`
- `./gradlew test`
