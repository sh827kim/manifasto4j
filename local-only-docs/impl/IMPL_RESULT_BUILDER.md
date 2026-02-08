# IMPL_RESULT_BUILDER

## Scope
TypeScript `packages/builder` 대비 Java `manifesto-builder` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/builder/src`

## 대상 (Java)
- `manifesto-builder/src/main/java/ai/manifesto/builder`

## 구현됨 (요약)
- `DomainBuilder`: `DomainSchema` 생성용 최소 빌더

## 미구현
### 1) DSL/Builder 레이어
- TS는 `defineDomain`, `setupDomain`, `validateDomain` 등 고수준 DSL 제공
- Java는 단순 `DomainBuilder`만 존재

### 2) Expr/Flow DSL
- TS는 `expr`/`flow` 빌더, guard/once helpers 제공
- Java는 Expr/Flow DSL 부재

### 3) Typed References
- TS는 FieldRef/ComputedRef/ActionRef/FlowRef 등 타입 안전 참조 제공
- Java는 reference 타입 부재

### 4) Accessor/State DSL
- TS는 StateAccessor/RecordAccessor/ArrayAccessor 및 buildAccessor 제공
- Java는 해당 기능 없음

### 5) Diagnostics
- TS는 DAG/Path validator 및 Diagnostic 모델 제공
- Java는 diagnostics 기능 없음

### 6) Zod 기반 타입 연동
- TS는 Zod → FieldSpec 변환 및 타입 기반 설계 제공
- Java는 대응 기능 없음

## 불일치
- (현재까지 발견된 주요 불일치 없음 — 구현 범위가 제한되어 미구현이 대부분)

## 정리
- Java builder는 **DomainSchema 조립용 최소 유틸** 수준
- TS builder의 핵심 가치(DSL, 타입 안전성, 진단, Zod 연동)는 대부분 미구현

## 다음 작업 후보 (builder 기준)
1. 최소 DSL 스코프 정의 (Expr/Flow/Domain)
2. Typed refs / Accessor 계층 도입
3. Diagnostics 및 DAG/Path validator 도입
4. Zod 대체(예: Jakarta Validation/JSON Schema 기반) 전략 확정

## 최근 업데이트 (2026-02-08)
- 별도 기능 확장 없음 (최소 DomainBuilder 상태 유지)
