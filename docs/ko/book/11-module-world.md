# 11. 모듈 상세: manifesto-world

## 모듈 역할
`manifesto-world`는 거버넌스 계층입니다.
- proposal 생성/상태 전이
- authority 정책 평가
- 승인/거절 기록
- lineage(event/edge) 저장

## 패키지 트리 (root tree)

```text
ai.manifesto.world
├─ authority
├─ events
├─ factories
├─ ingress
├─ lineage
├─ persistence
├─ proposal
├─ registry
├─ schema
└─ types
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.world` | world 엔트리(`ManifestoWorld`) 및 결과 모델 |
| `authority` | 승인 정책 평가 핸들러(auto/hitl/rules/tribunal) |
| `events` | world event sink/journal |
| `persistence` | world 저장소 계약과 메모리 구현 |
| `proposal` | proposal 상태기계/큐 |
| `schema` | world 도메인 모델(actor/authority/policy/decision) |
| `registry` | actor registry |
| `ingress` | ingress context 계약 |
| `lineage` | lineage 계산/조회 보조 |
| `types` | host 실행 연동 타입 |
| `factories` | hashing 및 생성 유틸 |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `ManifestoWorld` | world 파사드 |
| `AuthorityEvaluator` | 정책 핸들러 체인 실행 |
| `ProposalStateMachine` | proposal 상태 전이 규칙 |
| `ProposalQueue` | proposal 큐 관리 |
| `MemoryWorldStore` | 기본 메모리 저장소 |
| `WorldStore` | 저장소 인터페이스 |
| `EdgeQuery`/`ProposalQuery`/`WorldQuery` | 조회 필터 계약 |
| `StoreEvent`/`ObservableWorldStore` | 저장 이벤트 구독 계약 |

## 신입 개발자 추천 읽기 순서
1. `schema` 패키지(도메인 모델)
2. `ManifestoWorld`
3. `AuthorityEvaluator` + handler들
4. `ProposalStateMachine`
5. `MemoryWorldStore` 및 query/event API

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [12. 모듈 상세: manifesto-compiler](./12-module-compiler.md)
<!-- NEXT_DOC_END -->
