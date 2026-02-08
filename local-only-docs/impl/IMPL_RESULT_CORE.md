# IMPL_RESULT_CORE

## 목적/범위
TypeScript `packages/core` 대비 Java `manifesto-core` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치** 중심으로 기록한다.

## 기준 (Source of Truth)
- https://github.com/manifesto-ai/core.git `packages/core/src`

## 대상 (Java)
- `manifesto-core/src/main/java/ai/manifesto/core`

## 현재 상태 요약
- Core 계산 파이프라인/표현식/Flow/Apply/Validate/Explain 구현은 **대체로 정합**
- 결정성 관점의 주요 이슈였던 **System time 직접 사용은 1차 정리 완료**

## 구현 완료 (핵심)
- Core API: `ManifestoCore`, `Compute`, `Apply`, `Validate`, `Explain`
- Evaluators: `ExprEvaluator`, `FlowEvaluator`, `ComputedEvaluator`
- Schema/Runtime: `DomainSchema`, `ActionSpec`, `FieldSpec`, `ComputedFieldDef`, `Snapshot`, `SystemState`, `Intent`, `Requirement`, `TraceGraph/TraceNode`, `HostContext`
- Utils: `PathUtils`, `UuidUtils`, `DagUtils`, `ValidationUtils` (canonical JSON 기반 schema hash)

## TS 정합성 체크리스트 (부분 완료)
- Expr: TS core expr set과 1:1 정렬
- Path: data 경로는 **무접두사**, computed 키는 **full `computed.*`**
- Validation: code/message/path 포맷 정렬
- Schema hash 계산 규칙: 정합

## 불일치 (TS 대비)
### 1) 결정성 관련 잔여 검증
- Core 내부 직접 시간 사용 제거는 반영됐으나
- TS fixture 기반 장기 결정성 회귀 자동화는 아직 미완료

## Java 전용 보조 API
- `Validate.validateSnapshot(DomainSchema, Snapshot)`
- `Validate.isSnapshotValid(...)`

## 다음 작업 후보 (core 기준)
1. TS fixture 기반 결정성 회귀 자동화 강화
2. 골든 벡터 기반 회귀 테스트 지속 강화

## 최근 업데이트 (2026-02-08)
- Core 결정성 정합화 1차 완료 (직접 System time 사용 제거)
