# IMPL_RESULT_EFFECT_UTILS

## Scope
TypeScript `packages/effect-utils` 대비 Java `manifesto-effect-utils` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/effect-utils/dist` (TS 소스 미포함, dist 기준)

## 대상 (Java)
- `manifesto-effect-utils/src/main/java/ai/manifesto/effectutils`

## 구현됨 (요약)
- `Handlers.empty()` / `Handlers.constant()` 최소 유틸

## 미구현
### 1) Combinators
- TS: `withTimeout`, `withRetry`, `withFallback`, `parallel`, `race`, `sequential`
- Java: 없음

### 2) Transforms
- TS: `toPatch`, `toPatches`, `toErrorPatch`, `collectErrors`, `collectFulfilled`
- Java: 없음

### 3) Schema-driven handler factory
- TS: `defineEffectSchema`, `createHandler`, zod 기반 입력/출력 검증
- Java: 없음

### 4) Error 모델
- TS: `EffectUtilsError`, `TimeoutError`, `RetryError`, `ValidationError` 등
- Java: 없음

### 5) 타입/옵션 구조
- TS: `TimeoutOptions`, `RetryOptions`, `ParallelOptions`, `RaceOptions`, `SequentialOptions`, `Settled` 등
- Java: 없음

## 불일치
- (현재까지 발견된 주요 불일치 없음 — 구현 범위가 제한되어 미구현이 대부분)

## 정리
- Java effect-utils는 **최소 핸들러 헬퍼** 수준
- TS effect-utils의 대부분 기능(조합기/변환/스키마/에러/옵션)은 미구현

## 다음 작업 후보 (effect-utils 기준)
1. 최소 combinator 세트 도입 (`withTimeout`, `withRetry`)
2. Patch 변환 유틸 (`toPatch`, `toPatches`) 추가
3. Schema 기반 handler 생성(Validation 대안 포함) 전략 수립

## 최근 업데이트 (2026-02-08)
- 별도 기능 확장 없음 (최소 handler helper 상태 유지)
