**05. 모듈 및 폴더 구성 가이드**
이 장은 **Java 포팅 리포지토리 구조**와 각 모듈의 역할을 교재형으로 안내합니다.

**최상위 폴더 구조**
- `docs/`: 문서
- `manifesto-*`: 핵심 모듈들
- `scripts/`: 빌드/도구 스크립트
- `build/`, `gradle/`: Gradle 설정/산출물

**주요 모듈 (디렉터리 기준)**
- `manifesto-app`: 전체 스택을 감싸는 고수준 파사드
- `manifesto-builder`: 타입 안전 도메인 정의 DSL
- `manifesto-compiler`: MEL을 DomainSchema로 컴파일
- `manifesto-core`: 순수 계산 엔진
- `manifesto-host`: Effect 실행 런타임
- `manifesto-bridge`: UI/외부 입력 ↔ Intent 변환
- `manifesto-effect-utils`: Effect 관련 유틸리티
- `manifesto-world`: 승인/거절/라인리지/거버넌스 실행 기록
- `manifesto-examples`: 예제/샘플

**모듈 역할 요약**
- `core`: 계산 담당(순수 함수 영역)
- `host`: I/O 실행 담당(Effect 처리)
- `compiler`: MEL → 스키마 컴파일
- `builder`: 타입 안전한 스키마/Flow 정의
- `world`: 승인/거버넌스 개념(이 레이어는 구현 상황에 따라 별도 모듈로 존재할 수 있습니다)

**Java 포팅 관점 팁**
- 모듈 간 의존성을 **Core → Host 역방향으로** 섞지 마세요.
- Core는 Host를 몰라야 합니다.
- `Snapshot`, `Intent`, `Patch`는 **불변 모델**로 설계하는 것이 안전합니다.
- `Trace`는 표준 로깅과 분리하고, “도메인 설명” 전용으로 유지하세요.

**현재 구현 상태 요약 (2026-02-08 기준)**  
- `manifesto-core`: 핵심 계산/검증/설명 정합 + 결정성 1차 정리 + validate golden 추가  
- `manifesto-compiler`: MEL 파이프라인 구축 + `onceIntent` + golden 케이스 확장  
- `manifesto-host`: compute/effect/apply 최소 루프 + 경계 하드닝 + `$host` 1차 반영 + host golden  
- `manifesto-app`: 최소 API + world 통합 경로 + READY-8 반영  
- `manifesto-world`: 정식 구현 진행 중(승인/거절/라인리지/authority/실행 흐름) + world golden  
- `manifesto-bridge`: Projection 실행기 수준 (확장 필요)  
- `manifesto-builder`: DomainSchema 빌더 최소 기능  
- `manifesto-effect-utils`: basic handler 유틸만 제공  
- `manifesto-examples`: 샘플/테스트 보조  

**체크포인트 질문**
1. Core와 Host의 책임은 어떻게 분리되어 있나요.
2. MEL을 쓰는 이유는 무엇인가요.
3. World가 없다면 어떤 문제가 생길까요.
