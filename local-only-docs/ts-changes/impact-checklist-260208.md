# TS 변경 영향 체크리스트 (2026-02-08)

기준 문서: `local-only-docs/ts-changes/changes-260208.md`
목표: 결정성/초기 snapshot 일관성 관점에서 Java 반영 상태를 추적한다.

---

## 1) deterministic trace timestamps (`64d20a2`)

**Core**
- [x] `System.currentTimeMillis()` 제거 여부 점검
- [x] `HostContext.getNow()` 기반으로 meta/trace 시간 사용 정렬
- [x] compute duration fallback의 시스템 시계 의존 제거
- [x] core 테스트 통과 (`:manifesto-core:test`)

**잔여 작업**
- [x] Host/App 경로에서 durationMs 주입 정책 명세화 (기본 timeout/iteration 정책 분리 + 문서화)

---

## 2) genesis snapshot computed (`539b5b8`)

**App**
- [x] Java app bootstrap에서 genesis computed 평가 여부 점검
- [x] 초기 snapshot 생성 시점/정책 문서화

---

## 3) 최종 점검

- [ ] TS fixture 기반 결정성 회귀 테스트(장기)
- [x] App/World 도입 시 시간 주입 계약(Contract) 1차 확정
