**05. 모듈 및 폴더 구성 가이드 (TS 최신 기준 반영)**
이 장은 현재 TS 기준 패키지와 Java 포팅 상태를 함께 보여줍니다.

**최상위 폴더 구조**
- `docs/`: 공식 문서
- `manifesto-*`: Java 모듈
- `scripts/`: 동기화/검증 스크립트
- `build/`, `gradle/`: 빌드 설정/산출물

**TS 최신 패키지 기준 (2026-02-11)**
- `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`

**Java 현재 구현 모듈**
- `manifesto-core`
- `manifesto-host`
- `manifesto-app`
- `manifesto-compiler`
- `manifesto-world`

**Java 신규 포팅 예정 모듈**
- `manifesto-intent-ir` (미생성)
- `manifesto-translator` (미생성)
- `manifesto-codegen` (미생성)

**모듈 역할 요약**
- `core`: 순수 계산 엔진
- `host`: effect 실행 런타임
- `app`: 파사드 및 런타임 조립
- `compiler`: MEL 파이프라인/로워링
- `world`: 거버넌스/라인리지/승인
- `intent-ir`(예정): 자연어 의도 중간표현
- `translator`(예정): NL -> IR/Intent 파이프라인
- `codegen`(예정): Schema 기반 코드 생성

**현재 구현 상태 요약 (2026-02-11 기준)**
- `manifesto-core`: 결정성 정렬 및 hash/validation 기반 유지
- `manifesto-host`: retry/timeout + 오류 기록 경로 보강
- `manifesto-app`: world 연계 + session snapshot store 1차 구현
- `manifesto-compiler`: strict runtime patch API 1차 구현
- `manifesto-world`: 승인/거절/lineage 경로 + edge 테스트 확장
- 후속 핵심: `$mel` 런타임 경계, semantic hash 모드, app/world store 정합

**체크포인트 질문**
1. TS 최신 패키지 중 Java에 아직 없는 것은 무엇인가요.
2. 현재 구현 모듈과 예정 모듈은 어떻게 구분되나요.
3. 다음 사이클 P0 과제는 무엇인가요.
