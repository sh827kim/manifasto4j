# Java Porting Docs

이 폴더는 Java 포팅 프로젝트의 SPEC/FDR 정리를 위한 문서 공간입니다.

## 기준 문서 (TypeScript 원본)
- SPEC: `https://github.com/manifesto-ai/core/blob/main/packages/*/docs/` (일부 패키지는 `archives/` 참고)
- FDR: 각 패키지 내 FDR 문서 (없으면 `https://github.com/manifesto-ai/core/blob/main/archives/` 참고)

## 현재 작성 문서
- `docs/spec-core.md`
- `docs/fdr-core.md`
- `docs/spec-host.md`
- `docs/fdr-host.md`
- `docs/spec-app.md`
- `docs/fdr-app.md`
- `docs/spec-bridge.md`
- `docs/fdr-bridge.md`
- `docs/spec-compiler.md`
- `docs/fdr-compiler.md`
- `docs/spec-builder.md`
- `docs/fdr-builder.md`
- `docs/spec-effect-utils.md`
- `docs/fdr-effect-utils.md`
- `docs/spec-world.md`
- `docs/fdr-world.md`
- `docs/spec-translator.md`
- `docs/fdr-translator.md`
- `docs/spec-memory.md`
- `docs/fdr-memory.md`
- `docs/spec-lab.md`
- `docs/fdr-lab.md`
- `docs/spec-intent-ir.md`
- `docs/fdr-intent-ir.md`

## 제외
- react (UI 바인딩 전용, Java 포팅 범위 제외)

## 상태
- core/host/app 및 나머지 패키지 문서 개괄 정리 완료
- 필요 시 각 문서를 상세화 예정
- compiler: v0.5.0 목표 반영, IR generator lite 및 벡터 테스트 준비

## 인덱스
- `docs/INDEX.md`

## Latest 표기
- SPEC/FDR 문서 상단에 `> Latest:`로 최신 버전 명시
