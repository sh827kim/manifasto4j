# Manifesto Java 학습서 (신입 Java 개발자용)

이 학습서는 `manifesto-java-core`를 처음 보는 개발자가 **프로젝트의 목적, 구조, 핵심 개념, 모듈별 코드 위치**를 빠르게 이해하도록 만든 교육 문서입니다.

## 이 문서의 목표
- 이 프로젝트가 왜 존재하는지 설명할 수 있다.
- `core/host/app/world/compiler/intent-ir/translator/codegen` 각 모듈의 역할을 구분할 수 있다.
- 핵심 개념(`Intent`, `Snapshot`, `Patch`, `Effect`, `Trace`)이 어떻게 연결되는지 이해할 수 있다.
- 실제 코드에서 어디를 먼저 읽어야 하는지 알 수 있다.

## 권장 학습 순서
1. [01. 프로젝트 의미와 핵심 가치](./01-overview.md)
2. [02. 핵심 개념 (Java 입문자 버전)](./02-core-concepts.md)
3. [03. 핵심 개념 연관관계와 전체 실행 흐름](./03-sequence.md)
4. [04. 모듈별 역할 요약 (아키텍처 지도)](./04-core-api.md)
5. [05. 코드베이스 읽는 순서와 실무 온보딩 가이드](./05-packages.md)
6. [06. MEL 입문과 컴파일러 관점](./06-mel.md)
7. [07. 이 프로젝트로 할 수 있는 일 (LLM 활용 포함)](./07-llm-integration.md)
8. [08. 모듈 상세: manifesto-core](./08-module-core.md)
9. [09. 모듈 상세: manifesto-host](./09-module-host.md)
10. [10. 모듈 상세: manifesto-app](./10-module-app.md)
11. [11. 모듈 상세: manifesto-world](./11-module-world.md)
12. [12. 모듈 상세: manifesto-compiler](./12-module-compiler.md)
13. [13. 모듈 상세: manifesto-intent-ir](./13-module-intent-ir.md)
14. [14. 모듈 상세: manifesto-translator](./14-module-translator.md)
15. [15. 모듈 상세: manifesto-codegen](./15-module-codegen.md)

## 현재 코드 기준 범위 (2026-02-14)
- TS baseline 패키지: `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`
- Java 모듈도 동일한 8개 모듈을 모두 보유
- 현재 다음 중점 작업은 `TASK-C2`(cross-module integration regression) 단계

## 학습 전 준비
- Java 17 문법(클래스/인터페이스/record/컬렉션)
- Gradle 기본 실행법
- 최소 실행 명령:

```bash
./gradlew test
```

## 함께 보면 좋은 문서
- 패키지별 SPEC: `docs/spec`
- 패키지별 FDR: `docs/fdr`

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [01. 프로젝트 의미와 핵심 가치](./01-overview.md)
<!-- NEXT_DOC_END -->
