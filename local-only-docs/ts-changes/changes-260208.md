# TS 변경 요약 (2026-02-08 기준)

대상 리포: `/workspace/core`
조회 범위: 2026-02-03 이후 core/compiler/app/world 코드 변경 커밋

---

## 1) fix(core,app): ensure deterministic trace timestamps and prevent memory leak
**커밋**: `64d20a2`

**핵심 변화**
- Core/App 경로에서 trace timestamp 결정성을 강화
- Host 제공 시간(now) 기반 평가 컨텍스트 사용을 명확화

**주요 변경 파일**
- `packages/core/src/core/compute.ts`
- `packages/core/src/evaluator/context.ts`
- `packages/app/src/execution/host-executor/app-host-executor.ts`

**Java 포팅 영향**
- Core의 시간 소스는 `HostContext.now`로 단일화 필요
- compute duration fallback에서 시스템 시계 의존 제거 필요
- trace timestamp/ID 생성 규칙의 결정성 검증 필요

---

## 2) Evaluate computed values at genesis snapshot (READY-8)
**커밋**: `539b5b8`

**핵심 변화**
- genesis snapshot 시점에 computed 값을 평가해 초기 상태 일관성 강화

**주요 변경 파일**
- `packages/app/src/bootstrap/app-bootstrap.ts`
- `packages/app/src/__tests__/spec-compliance.test.ts`

**Java 포팅 영향**
- App bootstrap 시 computed 평가 시점/순서 점검 필요
- 초기 snapshot 생성 정책 문서화 필요

---

## 3) 기타 (참고)
- release/deps/docs 계열 커밋은 Java 코드 직접 영향이 낮아 우선순위 하향
