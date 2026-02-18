# 05. 코드베이스 읽는 순서와 실무 온보딩 가이드

## 1) 레포 루트 구조

```text
manifesto-java-core/
├─ manifesto-core/
├─ manifesto-host/
├─ manifesto-runtime/
├─ manifesto-sdk/
├─ manifesto-world/
├─ manifesto-compiler/
├─ manifesto-intent-ir/
├─ manifesto-translator/
├─ manifesto-codegen/
├─ docs/
└─ scripts/
```

## 2) 첫 1주 온보딩 플랜 (신입 기준)
1. `./gradlew test`로 전체 테스트를 1회 실행한다.
2. `manifesto-core`의 `Compute`, `Apply`, `FlowEvaluator`를 읽는다.
3. `manifesto-host`의 `HostRuntime`, `HostRunner`를 읽는다.
4. `manifesto-sdk`의 `AppFactory`, `AppConfig`와 `manifesto-runtime`의 `App`, `DefaultApp`, `ActionHandle`을 읽는다.
5. `manifesto-world`의 `ManifestoWorld`, `AuthorityEvaluator`를 읽는다.

## 3) 디버깅 시작점
- 계산 로직 이상: `manifesto-core`
- effect 실행 이상: `manifesto-host`
- API 사용 흐름 이상: `manifesto-sdk`, `manifesto-runtime`
- 승인/거절 이상: `manifesto-world`
- MEL 입력 문제: `manifesto-compiler`
- 자연어 해석 문제: `manifesto-intent-ir`, `manifesto-translator`
- 생성 코드 품질 문제: `manifesto-codegen`

## 4) 테스트 실행 팁

```bash
./gradlew :manifesto-core:test
./gradlew :manifesto-host:test
./gradlew :manifesto-runtime:test
./gradlew :manifesto-sdk:test
./gradlew :manifesto-world:test
./gradlew :manifesto-compiler:test
./gradlew :manifesto-intent-ir:test
./gradlew :manifesto-translator:test
./gradlew :manifesto-codegen:test
```

## 5) 문서 읽기 순서
- 개념: `docs/ko/book`
- 사양: `docs/spec`
- 설계 결정/후속: `docs/fdr`
- 실행 계획/갭 분석: `local-only-docs/plans`, `local-only-docs/reports` (로컬 전용)

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [06. MEL 입문과 컴파일러 관점](./06-mel.md)
<!-- NEXT_DOC_END -->
