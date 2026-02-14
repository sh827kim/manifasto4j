# Cycle 3 Execution Tasks (2026-02-14)

Scope:
- Phase 3 (Translator Core 완성)

Cycle Goal:
- `manifesto-translator`를 TS core shape(`core/helpers/invariants/pipeline/plugins/strategies`) 기준으로 확장해,
  현재 baseline(pipeline/plugin/policy/lower bridge)을 실행 전략/검증 규칙 중심 구조로 고도화한다.

## A. Core Type 확장

### A1. Intent Graph 모델
- [ ] chunk/span 모델 정식화
- [ ] intent graph node/edge/meta 모델 추가
- [ ] validation result/diagnostics 모델 정리

### A2. Execution Plan 모델
- [ ] execution step/dependency 모델 추가
- [ ] graph -> execution plan 변환 계약 정의
- [ ] 테스트 벡터 추가

## B. Strategies 계층 확장

### B1. Decompose/Translate/Merge 전략 인터페이스 정비
- [ ] 전략 옵션 모델 추가
- [ ] 기본 deterministic 전략 강화
- [ ] strategy 조합 구성 API 추가

### B2. 확장 전략 구현
- [ ] sentence/window decompose 전략 추가
- [ ] conservative/aggressive merge 전략 분기 구현
- [ ] 전략별 회귀 테스트 추가

## C. Helpers + Invariants 구현

### C1. Helper 함수
- [ ] chunk validation helper
- [ ] graph validation helper
- [ ] execution plan builder helper

### C2. Invariant 규칙
- [ ] causal integrity
- [ ] completeness/statefulness
- [ ] referential identity / abstract dependency
- [ ] invariant diagnostics 코드 정리

## D. Pipeline 고도화

### D1. Pipeline 옵션 확장
- [ ] diagnostics bag 개선(aggregation policy)
- [ ] stage별 설정 옵션 도입
- [ ] parallel executor 확장 포인트 정비

### D2. Plugin 연계 강화
- [ ] inspector/transformer 성격 분리
- [ ] plugin ordering 규칙 문서화
- [ ] plugin 회귀 테스트 추가

## E. 문서/검증

### E1. 문서 업데이트
- [ ] `docs/spec/spec-translator.md` 업데이트
- [ ] `docs/fdr/fdr-translator.md` 업데이트
- [ ] `docs/TS_PARITY_MATRIX_2026-02-14.md` translator 상태 갱신
- [ ] `docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` 진행률 갱신

### E2. 테스트
- [ ] `./gradlew :manifesto-translator:test` 통과
- [ ] `./gradlew :manifesto-intent-ir:test` 통합 영향 통과
- [ ] `./gradlew test` 전체 통과

## Exit Criteria
1. translator core에 strategies/helpers/invariants 축이 도입됨
2. pipeline/plugin/diagnostics가 확장 가능한 형태로 정리됨
3. translator 관련 문서/매트릭스/진행률 리포트가 최신화됨
