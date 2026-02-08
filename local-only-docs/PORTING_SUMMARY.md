# manifasto4j - PORTING_SUMMARY

**기준일**: 2026-02-08
**TS 원본 기준**: /workspace/core
**Java 포팅 위치**: /workspace/manifesto-java-core

---

## 📌 프로젝트 개요
- **목표**: TypeScript `manifesto-ai/core`를 Java로 포팅하여 **동일한 동작/스펙 준수** 확보
- **포함 범위(필수)**: core, host, app, bridge, compiler, builder, effect-utils, world
- **미진행 범위**: translator, memory, lab, intent-ir
- **재검토 범위**: (현 시점 없음)
- **제외**: react (UI 바인딩 전용)

---

## ✅ 핵심 완료 사항 (전체 범위)

### 1) Core 엔진 구현
- 데이터 구조/Flow/Expr/Evaluator/Compute/Apply/Validate/Explain 구현 완료
- TS 최신 동작 반영 진행 중 (HostContext/system patch/error code 규칙)
- Schema 해시/정규화 규칙 정합 (canonical schema에서 hash 제외)
- Validation 결과 구조화 (code/message/path)
- Expr 스펙 정합: TS에 없는 확장 expr 제거 및 core expr 규칙 정렬
- data/computed 경로 규칙 정합 (data는 무접두사, computed는 full path)
- **Validate API 범위 정렬**: `validate`는 스키마 전용, snapshot 검증은 별도 API
- **결정성 정합화 완료 (2026-02-08)**: Core 내부 `System.currentTimeMillis()` 직접 사용 제거
  - `HostContext`, `SnapshotMeta`, `TraceNode`, `Requirement`, `DagUtils`, `Compute` 반영

### 2) 최소 구현 완료
- host/app/bridge/compiler/builder/effect-utils 스켈레톤 + 최소 동작
- HostRuntime/DefaultApp/BridgeRuntime 기본 동작 테스트
- SimpleCompiler(MEL-lite) + DomainBuilder + handler 유틸

### 3) Compiler 기반 구성
- lexer/TokenKind/SourceLocation 기반 구축
- parser/AST/precedence 기본 구현
- analyzer(Scope/Semantic) 기본 구현
- renderer: AST 기반 domain/action + import 출력
- renderer: patch fragment/schema patch 렌더링 추가
- compile API: CompileDomainOptions/CompilePatchOptions 적용 (fnTableVersion/allowSysPaths 전달)
- IR generator: DomainSchema types/meta 생성 강화 (meta.name 반영)
- Golden 테스트 추가 (compiler/core)
- `onceIntent` contextual keyword 지원 완료 (parser/analyzer/IR/renderer/test)
- Golden 정합성 보완 완료
  - computed 필드 key 정합 (`computed.*`)
  - number literal 정규화 (`0` vs `0.0`)
  - `isNotNull`/`notNull` lowering 지원
  - literal union type의 base type 정합(`string`/`number`/`boolean`)

---

## ✅ 모듈별 현황 요약

### core
- **상태**: 기능 구현 완료, 결정성 리스크 1차 해소
- **비고**: snapshot 검증은 별도 API로 분리되어 있음 (TS와 동일)

### compiler
- **상태**: MEL 파이프라인 기반 구축 완료, 벡터/골든 테스트 가능
- **완료**: parser/analyzer/IR generator 초안, patch fragment/schema patch 렌더링
- **아직 부족**: lowering/evaluation 정식 계층과 오류/제약/trace 정합
- **최근 진행**: `onceIntent` 지원 + compiler golden 통과 정합화

### host
- **상태**: compute-loop/patch 적용/requirement 처리 최소 구현

### app
- **상태**: ready/act/subscribe 등 최소 API 제공

### bridge
- **상태**: projection/intent 변환 최소 구조 제공

### builder
- **상태**: DomainSchema DSL 최소 구현

### effect-utils
- **상태**: effect handler 유틸 최소 구현

### world
- **상태**: 미구현 (필수 범위로 승격)
- **필요**: Proposal/Decision/Lineage/Authority MVP

---

## 🚧 진행 중 (핵심 차이/미완료)

### compiler (가장 큰 격차)
- Lowering 정합화 필요 (컨텍스트 제약/shape 검증/오류 코드 완전화)
- Evaluation 정합화 필요 (trace/skip reason/total-eval 규칙)
- IR generator v0.5.0 타입/필드 확장 필요

### world (필수 신규 구현)
- Proposal/Decision/Lineage/Authority MVP 필요

### host/app/bridge
- ActionHandle 진행 상태/구독 옵션 보강 필요

---

## ⏸️ 미진행 범위
- translator / memory / lab / intent-ir

## 🔎 재검토 범위
- (현 시점 없음)

---

## 🧭 TS 최신 변경 점검 (요약)
- SPEC 경로 변경: `docs/specifications` 제거 → `packages/*/docs` 또는 `archives`
- compiler: `generator/ir.ts` 변경 반영 필요
- bridge/effect-utils: TS에서 제거되어 archives로 이관됨

---

## ▶️ 다음 작업 제안 (요약)

1) **world MVP 구현**
2) **golden 벡터 동기화 자동화**
3) **compiler lowering/evaluation 정식 계층 정합화**
4) **app bootstrap genesis computed 정책 점검**

---

## 📦 패키지 역할 요약 (TS 기준)

- **core**: 순수 계산 엔진 (Snapshot/Flow/Expr/Compute/Trace)
- **host**: Effect 실행기 (compute-loop, requirement 처리, patch 적용)
- **app**: 사용자 진입점 SDK (ready/act/subscribe/branch 등)
- **bridge**: 이벤트 → Intent 변환 계층 (ui/api/agent/system)
- **compiler**: MEL → DomainSchema 컴파일러 (parser/IR/lowering/diagnostics)
- **builder**: 스키마 DSL (코드 기반 DomainSchema 생성)
- **world**: 실행 결과 기록(월드/라인리지/히스토리)
- **translator**: 자연어 → Intent/patch 변환 파이프라인
- **memory**: 컨텍스트/회상/검증 계층
- **effect-utils**: effect 핸들러 유틸/테스트 보조
- **lab**: 실험/거버넌스/HITL 도구
- **intent-ir**: Intent 중간 표현(IR) 계층
- **react**: React 바인딩 (UI 전용) → Java 포팅 제외
