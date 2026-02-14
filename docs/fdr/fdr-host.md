# Manifesto Java Host FDR (Porting)


| Field | Value |
| --- | --- |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/host/docs/VERSION-INDEX.md` |
| Latest | `2.0.2` |
| Status | Draft (Java port) |
| Scope | host integration design notes |

## 1. Goals

- Ensure deterministic compute-effect loop
- Keep core pure; host handles IO/effects
- Align with TS Host Spec v1.1

## 2. Key Decisions

### 2.1 Concrete Patch Boundary
Host converts all conditional/expr patches to concrete values before calling core.apply().
This keeps core free of evaluation side effects.

### 2.2 Single intentId Across Loop
The same intentId must be used for lowering, evaluation, and compute.
This ensures stable once-markers and idempotency.

### 2.3 $system.* Restriction
Translator evaluation path forbids $system.* to prevent bypassing system lifecycle.

## 3. Risks / Follow-ups

- Compiler integration is required for Translator/MEL workflows.
- Host needs strict serialization guarantees for compute cycles.
- HCTS trace/reinjection/liveness invariant 보강 완료(2026-02-13):
  - single-runner invariant 검증(trace 기반)
  - chained reinjection 시 continue enqueue/liveness 검증
  - host golden에 trace invariant 확장 케이스 추가

- Host effect 경계 고도화 완료(2026-02-14, Cycle 6):
  - context provider + context-aware effect handler 도입
  - effect executor(retry/timeout/error 분류) 도입
  - effect trace 이벤트(attempt/retry/success/failure) 확장
  - host runtime 테스트에 context/trace 회귀 케이스 추가

- Host golden 시나리오 표준화 완료(2026-02-14, TASK-A4):
  - 시나리오 파일 분리(deteminism/trace-snapshot/complex-effects/todo-workflow)
  - 공통 loader/assertion harness(`HostGoldenVectorHarness`) 다중 fixture 지원
  - 단일 테스트 진입점(`HostGoldenTest`)으로 시나리오 검증 경로 통합
