# Cycle 2 Execution Tasks (2026-02-14)

Scope:
- Phase 2 (Intent-IR 완성)

Cycle Status:
- 완료 (2026-02-14)

Cycle Goal:
- `manifesto-intent-ir`를 TS shape(`schema/canonical/keys/lexicon/resolver/lower`) 기준으로 확장해,
  현재 baseline(키/lexicon/resolver 최소 구현)을 실행 가능한 중간표현 계층으로 승격한다.

## A. Schema Layer 확장

### A1. 타입 모델 추가
- [x] TS `schema/*` 대응 Java 타입 초안 정의 (heads/term/pred/event/resolved)
- [x] `IntentIrDocument`와 호환되는 매핑 규칙 정의
- [x] canonical 입력으로의 projection 규칙 정의

### A2. 유효성 검증
- [x] schema structural validation 추가
- [x] invalid node/feature diagnostic code 추가
- [x] schema 검증 단위 테스트 추가

## B. Lower Layer 구현

### B1. Lowering 계약 정의
- [x] `IntentIrLowerer` 인터페이스 추가
- [x] lower result model(success + diagnostics) 추가
- [x] translator 연결 포인트 계약 추가

### B2. 기본 lower 구현
- [x] action/domain/input/meta lowering 기본 경로 구현
- [x] unresolved action/fallback diagnostics 반영
- [x] round-trip 테스트 추가

## C. Keys / Lexicon / Resolver 고도화

### C1. Key pipeline 강화
- [x] strict/semantic key 입력 정규화 규칙 보강
- [x] sim key 거리 기반 near-duplicate 테스트 보강

### C2. Lexicon 기능 강화
- [x] domain-action 외 feature check(필수 role/slot) 확장
- [x] 진단 코드 체계 정리

### C3. Resolver 기능 강화
- [x] discourse/focus 우선순위 해석 규칙 추가
- [x] actionHint + lexicon fallback 충돌 규칙 추가

## D. Translator 경계 통합

### D1. translator plugin 연계
- [x] `IntentIrResolutionPlugin`에서 lower 경로 선택 가능화
- [x] lexicon/resolver diagnostics를 translator diagnostics와 안정 병합

### D2. 통합 테스트
- [x] `manifesto-translator`와의 integration test 추가 (intent-ir lower 포함)

## E. 문서/검증

### E1. 문서 업데이트
- [x] `docs/spec/spec-intent-ir.md` 업데이트
- [x] `docs/fdr/fdr-intent-ir.md` 업데이트
- [x] `docs/TS_PARITY_MATRIX_2026-02-14.md` intent-ir 상태 갱신

### E2. 테스트
- [x] `./gradlew :manifesto-intent-ir:test` 통과
- [x] `./gradlew :manifesto-translator:test` 통합 케이스 통과
- [x] `./gradlew test` 전체 통과

## Exit Criteria
1. intent-ir에 schema/lower 계층이 도입되어 TS shape 주요 축을 모두 보유
2. translator 연동 경계가 lower 포함 시나리오까지 검증됨
3. 문서(spec/fdr/matrix)가 최신 코드 상태와 동기화됨
