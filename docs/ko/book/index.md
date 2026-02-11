**Manifesto Java 포팅 학습서 (초안)**
이 문서는 TypeScript 기반 Manifesto의 핵심 개념을 **Java 개발자 관점**에서 이해하기 쉽게 정리한 학습서입니다. "계산(Core)과 실행(Host)을 분리한다"는 큰 원칙을 유지한 채, JVM 생태계에서 어떻게 설계/구현/운영하면 좋은지에 초점을 맞춥니다.

**목표 독자**
- TypeScript Manifesto를 Java로 포팅하거나 유지보수하려는 개발자
- 결정적 계산, 거버넌스, 감사 추적을 JVM 환경에서 구현하려는 팀
- Manifesto의 스키마/Flow/Effect 모델을 빠르게 이해하려는 사람

**읽는 순서**
1. [01. Manifesto 한눈에 보기 (Java 관점)](./01-overview.md)
2. [02. 핵심 개념 지도 (Java 개발자 버전)](./02-core-concepts.md)
3. [03. Intent부터 Snapshot까지: 전체 실행 흐름](./03-sequence.md)
4. [04. Core API 개념 가이드 (Java)](./04-core-api.md)
5. [05. 모듈 및 폴더 구성 가이드](./05-packages.md)
6. [06. MEL 문법 입문 (포팅 관점)](./06-mel.md)
7. [07. LLM 통합 시 기대 효과](./07-llm-integration.md)

**Java 포팅 관점의 핵심 키워드**
- 불변(Immutable) 모델링과 순수 함수 기반 계산
- I/O 금지 영역(Core)과 I/O 허용 영역(Host)의 분리
- `Snapshot -> Patch -> Snapshot` 반복 구조
- 승인/거절/감사를 위한 World 레이어

**현재 구현 범위 요약 (2026-02-11 기준)**
- `core`: 계산/검증/설명/트레이스 정합 + 결정성 1차 정리 완료
- `compiler`: MEL 파이프라인 구축 + `onceIntent` 반영 + golden 케이스 확장
- `host`: 최소 실행기 기반 경계 하드닝 + `$host` 1차 반영 + host golden 추가
- `app`: `ready/act/subscribe` 최소 API + world 통합 경로 + READY-8 반영
- `world`: 승인/거절/라인리지/authority/실행 경로 구현 + world golden 추가
- `intent-ir`, `translator`, `codegen`: TS 최신 기준 신규 포팅 대상

**참고**
- 이 문서는 개념 중심입니다. 실제 클래스/메서드 명은 모듈 구현에 따라 다를 수 있습니다.
