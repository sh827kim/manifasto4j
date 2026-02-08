# IMPL_RESULT_COMPILER

## Scope
TypeScript `packages/compiler` 대비 Java `manifesto-compiler` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/compiler/src`

## 대상 (Java)
- `manifesto-compiler/src/main/java/ai/manifesto/compiler`

## 구현됨 (요약)
- MEL 파이프라인: Lexer → Parser → ScopeAnalyzer → SemanticValidator → AstIrGenerator
- 컴파일 진입점: `MelCompiler` (풀 파이프라인), `SimpleCompiler` (라인 기반 MEL-lite)
- Patch 컴파일: `MelPatchCompiler` (action flow → runtime patch ops)
- 렌더링: `renderer/MelRenderer` + PatchFragment/SchemaPatch 렌더러
- 벡터 기반 Lower/Eval: `LoweringLite`, `RuntimePatchEvaluatorLite`
- `onceIntent` contextual keyword 반영 완료 (parser/analyzer/IR/renderer/test)
- 테스트: 벡터(JSON) 기반 호환성 테스트 + Golden 테스트 추가
  - `manifesto-compiler/src/test/resources/golden/compiler-e2e.json`
  - `manifesto-compiler/src/test/java/ai/manifesto/compiler/CompilerGoldenTest.java`

## 미구현
### 1) Compile API (Patch) 정식 스펙 정합
- TS `compileMelPatch`는 스펙상 API 정의되어 있으나 **현재 TS 구현은 stub**
- Java `MelPatchCompiler`는 동작하지만 **정식 API 형태는 미구현**

### 2) Lowering 계층 (정식)
- TS는 `lowerExprNode`, `lowerRuntimePatches`, `lowerPatchFragments`를 **컨텍스트/제약/오류 코드**와 함께 제공
- Java는 `LoweringLite` 중심 (정식 API/오류 코드/shape 검증은 제한적)

### 3) Evaluation 계층 (정식)
- TS는 `evaluateExpr`, `evaluateRuntimePatches`, `evaluateRuntimePatchesWithTrace` 제공
- Java는 `RuntimePatchEvaluatorLite`만 존재 (trace/스킵 사유 분류 미지원)

### 4) CLI/Formatter
- TS CLI/formatter 모듈 존재 → Java 구현 없음

## 불일치
### 1) Lowering/Eval 계층의 스펙 완전성
- TS는 스펙 기반 컨텍스트 제약 및 오류 코드 제공
- Java Lite 구현은 벡터 호환 중심으로 제한됨

### 2) 진단 메시지/위치 정합성
- 진단 구조는 존재하지만 TS와 완전 동치 여부는 골든 테스트로 보강 필요

## 정리
- **컴파일 파이프라인은 Java에 구현됨**
- 그러나 lowering/eval/patch 컴파일 계층은 **Lite 수준**이며 스펙 정합 강화가 필요

## 다음 작업 후보 (compiler 기준)
1. Lowering 정식 API 확장 (컨텍스트/오류 코드/shape 검증)
2. Runtime patch 평가 trace/스킵 사유 추가
3. Golden 벡터 자동 동기화 파이프라인 구축

## 최근 업데이트 (2026-02-08)
- `onceIntent` 지원 및 golden 정합화 완료
