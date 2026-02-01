# manifasto4j

Manifesto의 TypeScript 구현을 순수 Java로 포팅하는 저장소입니다.
목표는 Java 중심의 런타임/툴링을 제공하면서 TS 기준과 동일한 출력 호환성을 유지하는 것입니다.

## Manifesto 기본 개념
Manifesto는 상태 기반 애플리케이션을 위한 결정론적 도메인 런타임 스택입니다.
- **Schema**: 상태/계산값/액션 정의
- **Intent**: 액션 실행 요청
- **Core**: 액션 평가 → effect/patch → 새 Snapshot 생성
- **Host**: compute/effect 루프 실행기
- **App/Bridge**: 고수준 API 및 이벤트/프로젝션 바인딩
- **Compiler/Builder**: MEL/DSL 도구

공식 문서:
```
https://docs.manifesto-ai.dev/
```

## 저장소 문서 위치
- `docs/INDEX.md` (spec/fdr 인덱스)

## 모듈
- `manifesto-core` — core runtime (schema/expr/flow/compute)
- `manifesto-host` — compute/effect 루프
- `manifesto-app` — 고수준 API
- `manifesto-bridge` — projection/event 바인딩
- `manifesto-compiler` — MEL 컴파일러 + lowering
- `manifesto-builder` — schema/expr/flow DSL
- `manifesto-effect-utils` — effect handler 유틸
- `manifesto-examples` — 예제

## 빌드/테스트
```bash
./gradlew test
```
