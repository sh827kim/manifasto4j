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
https://docs.manifesto-ai.dev/

## 현재 상태 (2026-02-08)
- `core/host/app/bridge/compiler/builder/effect-utils/world` 모듈 포팅이 진행 중입니다.
- `world` 모듈은 authority/lineage 포함 경로와 edge 테스트가 반영되어 있습니다.
- `bridge`는 라우팅 projection과 `ProjectionResult`(`intent` 또는 `none(reason)`)를 지원합니다.
- `host`는 retry/timeout 옵션과 `$host` 오류 기록 경로를 포함합니다.
- `compiler`는 strict runtime-patch API 및 golden/vector 동기화 점검 경로를 포함합니다.

## 모듈
- `manifesto-core` — core runtime (schema/expr/flow/compute)
- `manifesto-host` — compute/effect 루프
- `manifesto-app` — 고수준 API
- `manifesto-bridge` — projection/event 바인딩
- `manifesto-compiler` — MEL 컴파일러 + lowering
- `manifesto-builder` — schema/expr/flow DSL
- `manifesto-effect-utils` — effect handler 유틸
- `manifesto-world` — world/authority/lineage 거버넌스 런타임
- `manifesto-examples` — 예제

## 저장소 문서 위치
- `docs/INDEX.md` (spec/fdr 인덱스)
- `docs/ko/book/index.md` (자바 개발자용 학습 문서)
- `docs/spec/spec-*.md`, `docs/fdr/fdr-*.md` (패키지별 레퍼런스)

## 로컬 전용 문서 정책
- `local-only-docs/`는 Git 추적 대상이 아니며, 로컬 작업 기록 용도입니다.

## 빌드/테스트
```bash
./gradlew test
```

## 자주 쓰는 검증 명령
```bash
./gradlew :manifesto-bridge:test :manifesto-app:test
./gradlew :manifesto-world:test
./gradlew :manifesto-compiler:test
./gradlew checkGoldenSync
```
