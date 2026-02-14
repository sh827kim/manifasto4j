# 08. 모듈 상세: manifesto-core

## 모듈 역할
`manifesto-core`는 이 프로젝트의 계산 엔진입니다.
- 입력: `DomainSchema`, `Snapshot`, `Intent`
- 출력: `ComputeResult`, `Patch`, `Requirement`, `Trace`

핵심 원칙:
- 결정성 유지
- 외부 I/O 금지
- 상태 전이는 patch 기반

## 패키지 트리 (root tree)

```text
ai.manifesto.core
├─ core
├─ evaluator
├─ expr
│  ├─ arithmetic
│  ├─ collection
│  ├─ comparison
│  ├─ conditional
│  ├─ literal
│  ├─ logical
│  ├─ object
│  ├─ string
│  └─ type
├─ flow
├─ schema
├─ trace
└─ utils
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.core` | 핵심 도메인 모델(`Snapshot`, `Intent`, `Patch`, `Requirement`) |
| `ai.manifesto.core.core` | `Compute`, `Apply`, `Validate`, `Explain` 조합 로직 |
| `ai.manifesto.core.evaluator` | flow/expr 평가 실행기 |
| `ai.manifesto.core.expr.*` | 함수형 식 노드(산술/문자열/컬렉션/비교 등) |
| `ai.manifesto.core.flow` | flow 노드 정의(`Seq`, `If`, `Patch`, `Effect`, `Fail`, `Halt`) |
| `ai.manifesto.core.schema` | 도메인 스키마 모델 |
| `ai.manifesto.core.trace` | trace 기록/재구성 도구 |
| `ai.manifesto.core.utils` | canonical/hash/path 등 공통 유틸 |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `ManifestoCoreImpl` | core facade 구현체 |
| `Compute` | compute 파이프라인 수행 |
| `Apply` | patch를 snapshot에 적용 |
| `Validate` | schema 검증 규칙 실행 |
| `Explain` | path 단위 설명(trace) 생성 |
| `FlowEvaluator` | flow 노드 평가 |
| `ExprEvaluator` | expr 노드 평가 |
| `ValidationUtils` | canonical/hash/검증 보조 |

## 신입 개발자 추천 읽기 순서
1. `Snapshot`, `Intent`, `Patch`
2. `ComputeResult`, `ComputeStatus`
3. `Compute` -> `FlowEvaluator`
4. `Apply` -> `ValidationUtils`
5. `TraceNode`, `TraceGraph`

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [09. 모듈 상세: manifesto-host](./09-module-host.md)
<!-- NEXT_DOC_END -->
