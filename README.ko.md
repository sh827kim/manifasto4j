# manifasto4j

Manifesto의 TypeScript 구현을 순수 Java로 포팅하는 저장소입니다.
목표는 Java 중심의 런타임/툴링을 제공하면서 TS 기준과 동일한 출력 호환성을 유지하는 것입니다.

## Manifesto 기본 개념
Manifesto는 상태 기반 애플리케이션을 위한 결정론적 도메인 런타임 스택입니다.
- **Schema**: 상태/계산값/액션 정의
- **Intent**: 액션 실행 요청
- **Core**: 액션 평가 후 effect/patch 산출 및 Snapshot 생성
- **Host**: compute/effect 루프 실행기
- **App**: 고수준 런타임 API
- **World**: 승인/거절/라인리지 거버넌스 레이어
- **Compiler**: MEL 도구

공식 문서:
https://docs.manifesto-ai.dev/

## 현재 상태 (2026-02-14)
- TS 최신 기준 패키지는 `app/codegen/compiler/core/host/intent-ir/translator/world`입니다.
- Java 구현 모듈은 `core/host/app/compiler/world`입니다.
- Java 후속 포팅 대상이던 `intent-ir/translator/codegen`은 계약/스켈레톤 모듈 부트스트랩이 완료되었습니다.
- 문서와 빌드 그래프에서 스펙 외 패키지를 제거했습니다.

## 모듈
- `manifesto-core` - core runtime (schema/expr/flow/compute)
- `manifesto-host` - compute/effect 루프
- `manifesto-app` - 고수준 API
- `manifesto-compiler` - MEL 컴파일러 + lowering
- `manifesto-world` - world/authority/lineage 런타임

## 저장소 문서 위치
- `docs/INDEX.md` (spec/fdr 인덱스)
- `docs/MASTER_COMPLETION_PLAN_2026-02-14.md` (전체 완성 단계별 실행 계획)
- `docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` (TS 코드 형상 기준 진행률 리포트)
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
./gradlew :manifesto-core:test :manifesto-host:test :manifesto-app:test :manifesto-compiler:test :manifesto-world:test
./gradlew checkGoldenSync
```
