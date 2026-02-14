# Cycle 1 Execution Tasks (2026-02-14)

Scope:
- Phase 0 (Parity Matrix)
- Phase 1 (App 완성 착수)

## A. Phase 0 - Parity Matrix

### A1. TS App/Core/Host/World/Compiler/Intent-IR/Translator/Codegen Export 수집
- [ ] TS 각 패키지 `src/index.ts` export 목록 추출
- [ ] 하위 디렉토리 책임(예: runtime, hooks, strategies, plugins) 맵핑
- [ ] Java 대응 클래스/패키지 매핑표 생성

### A2. 상태 분류
- [ ] `완료/부분/미구현` 분류
- [ ] 분류 근거(코드 경로 + 테스트 경로) 기입
- [ ] 모듈별 목표 퍼센트 지정

### A3. 산출물
- [ ] `docs/TS_PARITY_MATRIX_2026-02-14.md` 작성

## B. Phase 1 - App 완성 착수

### B1. 타입/결과 모델 정비
- [ ] `AppStatus` 타입 추가
- [ ] `ActionResult` 계열 모델 추가(completed/failed/rejected/preparation_failed)
- [ ] `RuntimeKind` 등 결과 메타 타입 추가

### B2. ActionHandle 계약 강화
- [ ] await/timeout/cancel 경계 API 설계
- [ ] update stream/observer API 정리
- [ ] 기존 phase history와 결합

### B3. Session/Branch 확장
- [ ] Session 인터페이스(actor 고정 컨텍스트) 추가
- [ ] Branch fork/list/switch semantics 고도화
- [ ] AppFactory 조립 경로 정리

### B4. System/Memory facade 최소 구현
- [ ] System facade 계약 추가
- [ ] Memory facade 계약 추가(no-op/disabled 기본 구현)

### B5. Hook 시스템 확장
- [ ] hook 우선순위 지원
- [ ] hook 예외 격리 정책 추가
- [ ] hook 필터(이벤트 타입 기반) 추가

### B6. 테스트
- [ ] app lifecycle 통합 테스트 확장
- [ ] session/branch/hook 오류 격리 테스트 추가
- [ ] `./gradlew :manifesto-app:test` 통과

## C. Cycle 1 Exit Criteria
1. `TS_PARITY_MATRIX_2026-02-14.md` 존재
2. App 타입/핸들/세션/브랜치/훅 확장 1차 완료
3. app 테스트 통과
4. 관련 spec/fdr 문서 동기화
