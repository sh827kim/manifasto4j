# Cycle 5 Execution Tasks (2026-02-14)

Scope:
- Phase 5 (Codegen 완성)

Cycle Status:
- 완료 (2026-02-14)

Cycle Goal:
- TS `packages/codegen`의 핵심 유틸리티(`path-safety`, `stable-hash`, `header`, `virtual-fs`)와 runner 통합 계약을 Java codegen에 반영한다.

## Task 1. Utility 계층 구현

### 1.1 Path Safety
- [x] 상대 경로 검증 + 정규화 유틸 구현
- [x] traversal/absolute/null-byte/drive-letter 차단

### 1.2 Stable Hash + Header
- [x] canonical 기반 stable hash 유틸 구현
- [x] deterministic generated header 정책 구현(stamp 옵션 포함)

## Task 2. Virtual FS 계층 구현

### 2.1 FilePatch/VFS
- [x] set/delete patch 모델 추가
- [x] collision/warning 규칙(FP-5~7 유사) 구현
- [x] deterministic 파일 순서 보장

## Task 3. Runner/Plugin 옵션 계약 확장

### 3.1 Runner 확장
- [x] `CodegenRunResult`(files/diagnostics/hash/options) 추가
- [x] 기존 `generate()` 호환 유지 + 상세 실행 API 추가
- [x] header/path-safety/vfs를 runner 파이프라인에 통합

### 3.2 Plugin 옵션 모델
- [x] naming/nullability/style 옵션 계약 추가
- [x] 옵션 검증 실패 진단 처리

## Task 4. 테스트 확장

### 4.1 Utility/VFS 테스트
- [x] path safety 테스트
- [x] stable hash 테스트
- [x] virtual fs 테스트

### 4.2 Runner 통합 테스트
- [x] header + deterministic 결과 테스트
- [x] collision/error gating 테스트
- [x] plugin options 테스트

## Task 5. 문서/리포트/검증

### 5.1 문서 업데이트
- [x] `docs/spec/spec-codegen.md` 업데이트
- [x] `docs/fdr/fdr-codegen.md` 업데이트
- [x] `docs/INDEX.md` Cycle 5 문서 반영

### 5.2 parity 리포트 업데이트
- [x] `docs/TS_PARITY_MATRIX_2026-02-14.md` codegen 상태 갱신
- [x] `docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` 진행률 갱신

### 5.3 검증
- [x] `./gradlew :manifesto-codegen:test` 통과
- [x] `./gradlew test` 통과

## Exit Criteria
1. codegen 유틸(`path-safety/stable-hash/header/virtual-fs`)이 Java에 구현된다.
2. runner가 utility 계층과 plugin options를 통합해 deterministic 결과를 제공한다.
3. codegen 테스트/문서/리포트가 최신 상태로 동기화된다.
