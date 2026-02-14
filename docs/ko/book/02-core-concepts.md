# 02. 핵심 개념 (Java 입문자 버전)

아래 개념은 모든 모듈에서 반복해서 등장합니다. 먼저 용어를 확실히 잡으면 코드 이해 속도가 크게 빨라집니다.

## 핵심 용어 사전

| 개념 | 쉬운 설명 | Java 코드에서 자주 보이는 타입 |
| --- | --- | --- |
| `DomainSchema` | "이 시스템의 규칙서" | `DomainSchema`, `ActionSpec`, `FieldSpec` |
| `Snapshot` | "현재 상태 전체 사진" | `Snapshot` |
| `Intent` | "사용자/시스템의 요청" | `Intent` |
| `Flow` | "요청 처리 절차를 표현한 로직 트리" | `FlowNode` |
| `Expr` | "값 계산 식" | `ExprNode` 계열 |
| `Patch` | "상태 변경 명령(set/unset/merge)" | `Patch`, `PatchOp` |
| `Requirement` | "Host가 실행해야 할 외부 작업 티켓" | `Requirement` |
| `Effect` | "외부 세계 작업 선언" | `FlowNode.Effect`, `EffectHandler` |
| `Trace` | "왜 이 결과가 나왔는지 기록" | `TraceNode`, `TraceGraph` |
| `World` | "승인/거절/라인리지 거버넌스" | `ManifestoWorld`, authority/schema 모델 |

## Snapshot 중심 사고
이 프로젝트는 "메서드가 객체 상태를 직접 바꾸는 방식"이 아니라, 아래 방식으로 동작합니다.
1. 현재 `Snapshot`을 읽는다.
2. `Intent`를 계산한다.
3. `Patch` 목록을 만든다.
4. `Patch`를 적용해 새 `Snapshot`을 만든다.

이 방식의 장점:
- 변경 이력을 설명하기 쉽다.
- 테스트 재현성이 높다.
- 롤백/시뮬레이션에 유리하다.

## Core와 Host 분리
- `Core`는 계산 엔진: 입력 -> 결과를 순수 계산
- `Host`는 실행 엔진: Effect를 실제로 실행(API/DB/메시지)

이 분리가 중요한 이유:
- 계산 로직은 테스트가 쉬워진다.
- 외부 장애(API 실패, 네트워크 타임아웃)가 계산 규칙을 오염시키지 않는다.

## World의 역할
`World`는 "실행 전에 승인할지"를 결정하는 레이어입니다.
- 누가 요청했는지(`Actor`)
- 어떤 권한 규칙인지(`AuthorityPolicy`)
- 최종 승인/거절 결정(`FinalDecision`)

즉, "실행 가능성"과 "계산 가능성"을 분리합니다.

## Intent-IR / Translator / Codegen의 의미
- `Intent-IR`: 자연어 의도를 구조화한 중간 표현
- `Translator`: 자연어 -> Intent-IR/실행 그래프
- `Codegen`: 스키마 기반 코드 생성(DTO/client)

LLM을 붙일 때도 결국 목적은 같습니다.
- 자연어를 구조화한다.
- 구조화된 결과를 검증한다.
- 검증된 결과만 실행한다.

## 신입 개발자가 자주 헷갈리는 지점
- `Patch`와 `Effect` 차이:
  - `Patch`: 상태를 바꾸는 명령
  - `Effect`: 외부 작업 요청
- `compute`와 `apply` 차이:
  - `compute`: 전체 흐름 계산
  - `apply`: 이미 계산된 patch 적용
- `Error`와 `Diagnostic` 차이:
  - `Error`: 실패 결과
  - `Diagnostic`: 경고/오류 진단 정보

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [03. 핵심 개념 연관관계와 전체 실행 흐름](./03-sequence.md)
<!-- NEXT_DOC_END -->
