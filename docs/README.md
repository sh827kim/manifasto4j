# Java Porting Docs

이 폴더는 Java 포팅 문서의 단일 인덱스 공간입니다.

## 기준
- 기준일: `2026-02-14`
- TS 소스 기준: `/workspace/manifasto-ts-core`
- TS 최신 확인 커밋: `3b40070`
- 최근 비교 범위: `754d860..3b40070` (문서/ADR/SPEC 변경, 런타임 소스 변경 없음)
- 최신 분석/액션: [PORTING_ACTION_PLAN_2026-02-11.md](PORTING_ACTION_PLAN_2026-02-11.md)

## 문서 분류
1. Active parity (TS와 직접 정합 대상)
2. Planned packages (Java 신규 포팅 대상, 현재 baseline 구현 포함)

## Active Parity
- Core: [spec/spec-core.md](spec/spec-core.md), [fdr/fdr-core.md](fdr/fdr-core.md)
- Host: [spec/spec-host.md](spec/spec-host.md), [fdr/fdr-host.md](fdr/fdr-host.md)
- App: [spec/spec-app.md](spec/spec-app.md), [fdr/fdr-app.md](fdr/fdr-app.md)
- Compiler: [spec/spec-compiler.md](spec/spec-compiler.md), [fdr/fdr-compiler.md](fdr/fdr-compiler.md)
- World: [spec/spec-world.md](spec/spec-world.md), [fdr/fdr-world.md](fdr/fdr-world.md)

## Planned Packages
- Intent-IR: [spec/spec-intent-ir.md](spec/spec-intent-ir.md), [fdr/fdr-intent-ir.md](fdr/fdr-intent-ir.md)
- Translator: [spec/spec-translator.md](spec/spec-translator.md), [fdr/fdr-translator.md](fdr/fdr-translator.md)
- Codegen: [spec/spec-codegen.md](spec/spec-codegen.md), [fdr/fdr-codegen.md](fdr/fdr-codegen.md)

## 인덱스
- [INDEX.md](INDEX.md)
- [ko/book/index.md](ko/book/index.md)

## 로컬 전용 문서
- `local-only-docs/`는 Git 추적 대상이 아닙니다.
