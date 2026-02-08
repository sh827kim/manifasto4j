# Java Porting Docs

이 폴더는 Java 포팅 프로젝트의 SPEC/FDR 정리를 위한 문서 공간입니다.

## 현재 기준
- 기준일: `2026-02-08`
- 적용 모듈: `core/host/app/bridge/compiler/builder/effect-utils/world`
- `world` 문서(`spec/spec-world.md`, `fdr/fdr-world.md`)를 포함해 모듈 문서셋이 구성되어 있습니다.

## 기준 문서 (TypeScript 원본)
- SPEC: 각 패키지 내 SPEC 문서 (없으면 [https://github.com/manifesto-ai/core/blob/main/archives/](https://github.com/manifesto-ai/core/blob/main/archives/) 참고)
- FDR: 각 패키지 내 FDR 문서 (없으면 [https://github.com/manifesto-ai/core/blob/main/archives/](https://github.com/manifesto-ai/core/blob/main/archives/) 참고)

## 현재 작성 문서
- [docs/spec/spec-core.md](spec/spec-core.md)
- [docs/fdr/fdr-core.md](fdr/fdr-core.md)
- [docs/spec/spec-host.md](spec/spec-host.md)
- [docs/fdr/fdr-host.md](fdr/fdr-host.md)
- [docs/spec/spec-app.md](spec/spec-app.md)
- [docs/fdr/fdr-app.md](fdr/fdr-app.md)
- [docs/spec/spec-bridge.md](spec/spec-bridge.md)
- [docs/fdr/fdr-bridge.md](fdr/fdr-bridge.md)
- [docs/spec/spec-compiler.md](spec/spec-compiler.md)
- [docs/fdr/fdr-compiler.md](fdr/fdr-compiler.md)
- [docs/spec/spec-builder.md](spec/spec-builder.md)
- [docs/fdr/fdr-builder.md](fdr/fdr-builder.md)
- [docs/spec/spec-effect-utils.md](spec/spec-effect-utils.md)
- [docs/fdr/fdr-effect-utils.md](fdr/fdr-effect-utils.md)
- [docs/spec/spec-world.md](spec/spec-world.md)
- [docs/fdr/fdr-world.md](fdr/fdr-world.md)
- [docs/spec/spec-translator.md](spec/spec-translator.md)
- [docs/fdr/fdr-translator.md](fdr/fdr-translator.md)
- [docs/spec/spec-memory.md](spec/spec-memory.md)
- [docs/fdr/fdr-memory.md](fdr/fdr-memory.md)
- [docs/spec/spec-lab.md](spec/spec-lab.md)
- [docs/fdr/fdr-lab.md](fdr/fdr-lab.md)
- [docs/spec/spec-intent-ir.md](spec/spec-intent-ir.md)
- [docs/fdr/fdr-intent-ir.md](fdr/fdr-intent-ir.md)

## 제외
- react (UI 바인딩 전용, Java 포팅 범위 제외)

## 상태
- 모듈별 spec/fdr 문서 세트 정리 완료
- 한글 학습 문서(`docs/ko/book`) 운영 중
- compiler: strict runtime-patch API 및 golden/vector 동기화 점검 경로 반영
- bridge: framework-neutral adapter 계약, projection result 모델 반영

## 인덱스
- [docs/INDEX.md](INDEX.md)
- [docs/ko/book/index.md](ko/book/index.md)

## Latest 표기
- SPEC/FDR 문서 상단에 `> Latest:`로 최신 버전 명시

## 로컬 전용 문서
- `local-only-docs/`는 로컬 작업 노트이며 Git 추적 범위에서 제외됩니다.
