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
- **최근 진행(추가)**:
  - `RuntimePatchEvaluatorLite` trace 확장 (`evaluateWithTrace`)
  - skip reason code 정규화 + applied/skipped/dropped trace 이벤트 기록
  - 관련 단위 테스트 추가

### host
- **상태**: compute-loop/patch 적용/requirement 처리 최소 구현
- **최근 진행**:
  - 비수렴 effect 루프 가드 반영(반복 상한 기반 fail-fast)
  - host/app 테스트 경로를 data 무접두사 규칙으로 정렬(`status`)
  - `HostRuntimeOptions` 도입으로 timeout/maxIterations 정책 분리
  - pending 경계 보강:
    - missing handler 시 pending 유지 반환
    - pending + empty requirements 시 조기 반환
  - `data.$host` 1차 반영:
    - Core `Apply`에서 `$host` 예약 경로 허용
    - HostRuntime에서 intent slot 기록(`$host.currentIntentId`, `$host.intentSlots.*`)
  - 관련 회귀 테스트 보강 (`HostRuntimeTest`)
  - `data.$host` 정합화 설계 초안 문서화
    - `local-only-docs/design/host-runtime-boundary-hardening.ko.md`

### app
- **상태**: ready/act/subscribe 등 최소 API 제공
- **최근 진행**: READY-8 정합화 1차 완료
  - `DefaultApp.ready()`에서 genesis snapshot computed 평가 반영
  - world 통합 테스트에 genesis computed 회귀 케이스 추가

### bridge
- **상태**: projection/intent 변환 최소 구조 제공

### builder
- **상태**: DomainSchema DSL 최소 구현

### effect-utils
- **상태**: effect handler 유틸 최소 구현

### world
- **상태**: 정식 구현 진행 중 (Phase 0~6 1차 완료)
- **완료**:
  - `manifesto-world` 모듈 생성 및 멀티모듈 빌드 연결
  - schema/types 1차 골격 구현 (`World/Proposal/Decision/Actor/Authority/Binding`)
  - Proposal 상태기계 + ProposalQueue 1차 구현
  - `WORLD-HASH-*` 1차 구현 (`computeSnapshotHash`, `computeWorldId`)
  - Factory 1차 구현 (`createGenesisWorld`, `createWorldFromExecution`, `createDecisionRecord`)
  - ActorRegistry 구현 (등록/조회/바인딩 갱신/불변식)
  - WorldLineage DAG 구현 (genesis/edge/ancestors/descendants)
  - `WorldStore`/`MemoryWorldStore` 구현 (world/snapshot/edge/proposal/decision/binding)
  - Authority 서브시스템 1차 구현 (`auto/policy/hitl/tribunal`, evaluator)
  - `ManifestoWorld` 오케스트레이터 1차 구현
    - `createGenesis`
    - `submitProposal`
    - `processHITLDecision`
    - tribunal vote 처리 경로
    - 승인 시 HostExecutor 실행 및 world/lineage/store 반영
  - P0 하드닝 1차 완료 (2026-02-08)
    - `submitProposal` 입력 검증 보강(base world pending/origin actor/intentKey)
    - `executeProposal` executor 예외 경계 보강(failed world + failed event + failed terminal)
    - 관련 회귀 테스트 추가
  - execution key 정책 주입 구조 1차 반영
    - `ExecutionKeyPolicy` 인터페이스 추가
    - `ManifestoWorld` 기본 정책 + custom 정책 주입 경로 제공
  - escalation 정책 고도화 1차 반영
    - 멀티 홉 escalation 경로 처리
    - escalation 실패 fallback 처리 + `proposal:escalation_failed` 이벤트
    - escalation hop limit 가드 추가
  - TS world 시나리오 포팅 확대
    - unregistered actor / non-existent base world / duplicate genesis 거부 테스트 추가
    - escalation multi-hop/fallback 시나리오 추가
    - lineage depth/isDescendant/findPath 시나리오 추가
  - `:manifesto-world:test` 통과
- **다음 필요**:
  - TS `world.test.ts` 시나리오 포팅 지속 확대(coverage 확장)
  - authority escalation 정책 고도화(현재는 authorityId 기반 단일 홉 + loop guard)
  - world/app 경계 장애 시나리오(재시도/중복 실행키) 하드닝
  - `submitProposal` 입력 검증 정합화(base world pendingRequirements/origin actor/intentKey)
  - `executeProposal` executor 예외 경계 정합화(failed world + failed event + terminal failed)
  - READY-8(app bootstrap genesis computed) 정합화

점검 근거:
- `local-only-docs/reports/next-cycle-review-2026-02-08.md`

---

## 🚧 진행 중 (핵심 차이/미완료)

### compiler (가장 큰 격차)
- Lowering 정합화 필요 (컨텍스트 제약/shape 검증/오류 코드 완전화)
- Evaluation 정합화 필요 (trace/skip reason/total-eval 규칙)
- IR generator v0.5.0 타입/필드 확장 필요

### world (정식 구현 진행 중)
- Phase 8 하드닝 1차 완료, 잔여 edge case 보강 필요

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
- 문서 구조 변경 확인:
  - 기존 `docs/ko/book` 경로는 현재 없음
  - 기준 문서는 `docs/*` 및 `packages/*/docs`, 그리고 `local-only-docs/git-pull/changes-2026-02-08.md`

---

## ▶️ 다음 작업 제안 (요약)

1) **world 정식 구현 (Phase 8+)**
2) **golden 벡터 동기화 자동화**
3) **compiler lowering/evaluation 정식 계층 정합화**
4) **app bootstrap genesis computed 정책 점검**

최근 진행 (2026-02-08 추가):
- Golden 동기화 스크립트 초안 추가: `scripts/sync-golden.sh`
- TS compiler 벡터/골든 경로 자동 탐색 + 복사/누락 보고 지원
- `scripts/check-golden-sync.sh` + Gradle task(`checkGoldenSync`, `syncGoldenVectors`) 추가

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
