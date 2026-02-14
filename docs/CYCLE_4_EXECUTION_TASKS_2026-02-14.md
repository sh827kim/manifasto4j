# Cycle 4 Execution Tasks (2026-02-14)

Scope:
- Phase 4 (Translator Adapter/Target 계층 정합)

Cycle Status:
- 완료 (2026-02-14)

Cycle Goal:
- TS `packages/translator/adapters/*`, `packages/translator/targets/*` 형상을 Java에서 provider-neutral 계약 + target exporter 계약으로 대응한다.

## Task 1. TS ↔ Java 매핑 확정

### 1.1 Adapter 계층 매핑
- [x] TS `core/interfaces/llm-port.ts` 계약 분석
- [x] TS adapter 패밀리(openai/ollama/claude) 구성 확인
- [x] Java SPI 계약(`LlmPort`, request/response/error, provider profile) 설계 반영

### 1.2 Target 계층 매핑
- [x] TS `core/interfaces/exporter-port.ts` 계약 분석
- [x] TS target 패밀리(json/manifesto/openapi) 구성 확인
- [x] Java target 계약(`ExportInput`, `TargetExporter`) 설계 반영

## Task 2. Adapter SPI 계층 구현

### 2.1 공통 SPI
- [x] `LlmPort` 및 request/response/usage/error 타입 구현
- [x] provider capability profile + mapper/normalizer 계약 구현

### 2.2 Provider 프로파일 구현
- [x] OpenAI profile + mapper + normalizer
- [x] Ollama profile + mapper + normalizer
- [x] Claude profile + mapper + normalizer

### 2.3 테스트
- [x] adapter contract test 추가
- [x] provider mapper/normalizer 회귀 테스트 추가

## Task 3. Target Exporter 계층 구현

### 3.1 공통 Target 계약
- [x] `ExportInput`, `TargetExporter`, helper 구현

### 3.2 Exporter 구현
- [x] JSON exporter 구현
- [x] Manifesto exporter 구현
- [x] OpenAPI exporter 구현

### 3.3 테스트
- [x] target exporter contract/snapshot 테스트 추가

## Task 4. Translator 통합 경로 완성

### 4.1 Orchestration
- [x] `DefaultTranslator`에 strategy -> invariant -> pipeline -> exporter 경로 추가
- [x] plugin ordering/diagnostics aggregation과 exporter 입력 진단 병합

### 4.2 테스트
- [x] multi-target integration test 추가

## Task 5. 문서/매트릭스/진행률 동기화

### 5.1 문서
- [x] `docs/spec/spec-translator.md` 업데이트
- [x] `docs/fdr/fdr-translator.md` 업데이트
- [x] `docs/INDEX.md`에 Cycle 4 계획 문서 반영

### 5.2 Parity 리포트
- [x] `docs/TS_PARITY_MATRIX_2026-02-14.md` translator 상태 갱신
- [x] `docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` 진행률 갱신

### 5.3 검증
- [x] `./gradlew :manifesto-translator:test` 통과
- [x] `./gradlew test` 통과

## Exit Criteria
1. Java translator에 adapter/target 계층 계약이 고정된다.
2. `json/manifesto/openapi` exporter가 테스트로 검증된다.
3. translator parity가 adapter/target 영역까지 확장되어 문서/리포트와 일치한다.
