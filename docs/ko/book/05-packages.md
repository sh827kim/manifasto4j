**05. 모듈 및 폴더 구성 가이드 (TS 최신 기준 반영)**
이 장은 현재 TS 기준 패키지와 Java 포팅 상태를 함께 보여줍니다.

**최상위 폴더 구조**
- `docs/`: 공식 문서
- `manifesto-*`: Java 모듈
- `scripts/`: 동기화/검증 스크립트
- `build/`, `gradle/`: 빌드 설정/산출물

**TS 최신 패키지 기준 (2026-02-13)**
- `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`

**Java 현재 구현 모듈**
- `manifesto-core`
- `manifesto-host`
- `manifesto-app`
- `manifesto-compiler`
- `manifesto-world`
- `manifesto-intent-ir` (bootstrap)
- `manifesto-translator` (bootstrap)
- `manifesto-codegen` (bootstrap)

**Java 후속 구현 예정 모듈 작업**
- `manifesto-intent-ir`: canonical serialization/hash 경계 구현
- `manifesto-translator`: interpret/verify/refine 파이프라인 구현
- `manifesto-codegen`: 첫 타깃(Java DTO/typed client) 구현

**모듈 역할 요약**
- `core`: 순수 계산 엔진
- `host`: effect 실행 런타임
- `app`: 파사드 및 런타임 조립
- `compiler`: MEL 파이프라인/로워링
- `world`: 거버넌스/라인리지/승인
- `intent-ir`(bootstrap): 자연어 의도 중간표현 + 정규화 계약
- `translator`(bootstrap): NL -> IR/Intent 파이프라인 인터페이스 계약
- `codegen`(bootstrap): Schema 기반 코드 생성 계약

**현재 구현 상태 요약 (2026-02-13 기준)**
- `manifesto-core`: 결정성 정렬 및 hash/validation 기반 유지
- `manifesto-host`: retry/timeout + 오류 기록 경로 보강
- `manifesto-app`: world 연계 + session snapshot store 1차 구현
- `manifesto-compiler`: strict/runtime evaluator TS parity 1차 완료
- `manifesto-world`: 승인/거절/lineage 경로 + edge 테스트 확장
- `manifesto-host`: HCTS trace/reinjection/liveness invariant 테스트 보강 완료
- `manifesto-intent-ir`: bootstrap 계약 추가
- `manifesto-intent-ir`: canonical serialization/hash 경계 1차 구현 완료
- `manifesto-translator`: interpret/verify/refine 파이프라인 1차 구현 완료
- `manifesto-translator`: adapter capability contract + verifier 정책 점수화 구현 완료
- `manifesto-translator`: 도메인 정책 룰셋 주입/검증(TRV101/TRV102) 1차 구현 완료
- `manifesto-codegen`: `java-dto` 첫 타깃(StateDto 생성) 1차 구현 완료
- `manifesto-codegen`: `java-typed-client` 타깃(Domain Client + Action Input DTO) 1차 구현 완료
- 후속 핵심: bootstrap 모듈 구현 단계(intent-ir/translator/codegen) 진입

**체크포인트 질문**
1. TS 최신 패키지 중 Java에 아직 없는 것은 무엇인가요.
2. 현재 구현 모듈과 예정 모듈은 어떻게 구분되나요.
3. 다음 사이클의 우선 과제(P1/P2)는 무엇인가요.
