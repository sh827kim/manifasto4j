# Manifesto Java Core

> **Manifesto**는 결정론적 계산과 완전한 책임성을 제공하는 의미론적 상태 계층이며,
> 이 저장소는 TypeScript 구현을 **순수 Java**로 포팅한 버전입니다.

문서: https://docs.manifesto-ai.dev
데모: https://taskflow.manifesto-ai.dev

---

## Manifesto란?

Manifesto는 AI 네이티브 애플리케이션에서 **모든 상태 변화가 추적 가능하고, 되돌릴 수 있으며, 명시적 권한으로 거버넌스**되도록 만드는 상태 계층입니다.

**Core computes. Host executes. World governs.**

```
Intent → Core (compute) → Patches + Effects → Host (execute) → New Snapshot
                                    ↓
                              World (govern)
                                    ↓
                        Proposal → Authority → Decision
```

---

## 핵심 통찰

Manifesto는 도메인 상태를 **의미 공간(semantic space)**의 좌표로 봅니다.

| 개념 | Manifesto에서의 의미 |
|------|----------------------|
| Domain Schema | 의미 공간 정의 (차원, 유효 영역, 이동 규칙) |
| Snapshot | 공간 내 한 점 (현재 상태) |
| Intent | 다음 좌표로 이동 명령 |
| Computation | 다음 유효 좌표 계산 |

```
compute(schema, snapshot, intent) → snapshot'
        ↓        ↓         ↓           ↓
      space   current   navigation    next
      defn    coord     command       coord
```

---

## 이것은 아니다

| Manifesto는 아니다 | 대신 이것이다 |
|--------------------|----------------|
| AI 에이전트 프레임워크 | AI가 읽고 수정 가능한 의미론적 상태 계층 |
| 워크플로/오케스트레이션 엔진 | 선언적 흐름을 가진 결정론적 계산 시스템 |
| DB/ORM | 순수 계산 레이어 (저장은 Host가 담당) |
| React/Redux 대체 | 모든 UI에 의미론적 상태를 제공하는 보완 레이어 |

---

## 핵심 개념

| 개념 | 한 줄 설명 |
|------|------------|
| Snapshot | 시점의 전체 상태, 단일 진실의 원천 |
| Intent | 도메인 액션 요청 |
| Patch | Snapshot을 바꾸는 유일한 방법 |
| Effect | Host가 실행할 외부 작업 선언 |
| World | 거버넌스 메타데이터를 가진 불변 Snapshot |

---

## 이 저장소

TypeScript 구현을 기준으로 **Java 핵심 런타임(Core)**를 포팅합니다.

- `manifesto-core`: Java Core 구현 (계산, 검증, 평가, 추적)
- `manifesto-examples`: 예제 및 데모

---

## 빌드 및 테스트

```bash
# 전체 테스트
./gradlew test

# Core 모듈만 테스트
./gradlew :manifesto-core:test
```

---

## 프로젝트 구조

```
manifesto-java-core/
├── manifesto-core/       # Java Core 구현
├── manifesto-examples/   # 예제
└── README.md
```

---

## 문서

- 공식 문서: https://docs.manifesto-ai.dev
- 아키텍처 개요: https://docs.manifesto-ai.dev/architecture/
- 스펙: https://docs.manifesto-ai.dev/specifications/

---

## 라이선스

MIT License
