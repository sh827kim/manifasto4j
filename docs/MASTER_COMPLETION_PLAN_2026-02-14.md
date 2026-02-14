# Master Completion Plan (2026-02-14)

## 1. 목적
- TS 기준 코드 형상(`3b40070`) 대비 Java 포팅을 **기능/계약/테스트/문서**까지 완료한다.
- 현재 진행률(형상 기준 약 55%)을 단계적으로 90%+까지 끌어올린다.

## 2. 기준선
- TS 기준 저장소: `/workspace/manifasto-ts-core`
- Java 대상 저장소: `/workspace/manifesto-java-core`
- 기준 문서:
  - 진행률 리포트: `TS_PARITY_PROGRESS_REPORT_2026-02-14.md`
  - 모듈 spec/fdr: `docs/spec/*`, `docs/fdr/*`

실행 상태(2026-02-14):
- Cycle 1~7 코드/테스트 작업 완료
- 잔여 Blocker: `checkGoldenSync`가 TS compiler vector 경로 변경으로 실패

## 3. 전체 완료 정의 (Global DoD)
1. TS 패키지 `app/core/host/world/compiler/intent-ir/translator/codegen`의 공개 계약이 Java에서 대응된다.
2. 모듈별 핵심 시나리오 회귀 테스트가 존재한다.
3. `./gradlew test` 상시 통과 + 골든/정합 검증 유지.
4. 문서(`README`, `docs/*`, `ko/book`)가 코드 상태와 일치한다.
5. TS 업데이트 발생 시 parity 매트릭스를 선갱신하고 영향 범위를 반영한다.

## 4. 워크스트림
- WS-A: App API 정합
- WS-B: Intent-IR 정합
- WS-C: Translator 정합
- WS-D: Codegen 정합
- WS-E: Host/World 고도화
- WS-F: Compiler/Core 잔여 갭 제거
- WS-G: 통합/골든/문서 마감

## 5. 단계별 실행 계획

### Phase 0. Parity Matrix 확정 (착수 기준 단계)
목표:
- TS export/디렉토리 형상을 Java 대응표로 고정하고, 이후 모든 작업의 판단 기준으로 사용한다.

작업:
1. TS `src/index.ts` 및 하위 디렉토리 책임 목록화
2. Java 대응 타입/API 매핑표 작성
3. 항목별 상태(`완료/부분/미구현`) 표시
4. 모듈별 수치 목표치 설정

산출물:
- `docs/TS_PARITY_MATRIX_2026-02-14.md`

완료 조건:
- 8개 모듈 전부에 대해 API/기능/테스트 매트릭스가 존재

---

### Phase 1. App 완성 (우선순위 P1)
현재 이행:
- lifecycle phase/update, session/branch/hook 기본 계약 반영 완료

남은 작업:
1. `AppStatus`, `ActionResult` 계열 타입 명시화
2. `ActionHandle` 비동기/대기/취소 계약 강화
3. Session API 확장(고정 actor + context 전파)
4. Branch API 확장(fork/list/switch semantics 정밀화)
5. System facade, Memory facade 최소/표준 구현
6. Hook 시스템 확장(우선순위, 에러 격리, 필터)
7. app 통합 시나리오 테스트 확대

검증:
- `./gradlew :manifesto-app:test`

완료 조건:
- TS app shape 대비 주요 facade/runtime 계약 80% 이상

---

### Phase 2. Intent-IR 완성 (P1~P2)
현재 이행:
- canonical/hash + key(strict/semantic/sim) + lexicon/resolver 최소 코어 반영

남은 작업:
1. schema 계층 정식화(heads/term/pred/event/resolved/specs)
2. lower 경계 구현(Intent-IR -> 실행 경계 payload)
3. lexicon feature check 체계 확장
4. resolver discourse/focus 문맥 모델 확장
5. key derivation 경계 테스트 강화

검증:
- `./gradlew :manifesto-intent-ir:test`

완료 조건:
- TS intent-ir 구조 항목 대부분 대응(85% 목표)

---

### Phase 3. Translator Core 완성 (P1~P2)
현재 이행:
- pipeline/plugin, policy provider reload, intent-ir resolution plugin 반영

남은 작업:
1. core type 확장(chunk/intent-graph/execution-plan/diagnostics)
2. strategy 계층 확장(decompose/translate/merge)
3. invariants(helper 포함) 구현
4. diagnostics bag/parallel executor 정합
5. verifier/refiner 정책 룰 체계 고도화

검증:
- `./gradlew :manifesto-translator:test`

완료 조건:
- TS translator core shape 대비 80% 이상

---

### Phase 4. Translator Adapter/Target 계층 정합 (P2)
목표:
- framework 종속 구현을 코어에서 분리한 채 TS 패밀리 구조를 맞춘다.

작업:
1. adapter 모듈 경계 문서화 및 인터페이스 고정
2. target exporter(JSON/Manifesto/OpenAPI) 계약 구현
3. adapter capability 테스트를 exporter 조합까지 확장

검증:
- translator integration 테스트 + 계약 테스트

완료 조건:
- TS translator adapters/targets 형상에 대응 가능한 확장 구조 확보

---

### Phase 5. Codegen 완성 (P2)
현재 이행:
- plugin/registry/runner + java-dto/java-typed-client 편입 완료

남은 작업:
1. path safety 계층 추가
2. stable hash + 헤더 정책 정합
3. virtual fs 계층 추가
4. plugin 옵션 계약(네이밍/nullability/style) 정식화
5. integration snapshot 테스트 확장

검증:
- `./gradlew :manifesto-codegen:test`

완료 조건:
- TS codegen shape 대비 85% 목표

---

### Phase 6. Host/World 고도화 (P2)
목표:
- 실행 안정성 및 거버넌스 경계를 TS shape 수준으로 보강한다.

Host 작업:
1. context-provider/execution-context 분리 강화
2. effect registry/executor 에러 경계 정밀화
3. trace invariant 회귀 케이스 증설

World 작업:
1. ingress/epoch 경계 테스트 보강
2. authority(HITL/tribunal/policy) 상태 전이 확장
3. event/query/persistence 계약 보강

검증:
- `./gradlew :manifesto-host:test :manifesto-world:test`

완료 조건:
- host/world 모두 85% 목표

---

### Phase 7. Compiler/Core 잔여 갭 제거 (P2)
목표:
- compiler/core에서 TS 대비 남은 edge behavior를 정리한다.

작업:
1. compiler api/loader/renderer 주변 갭 제거
2. core explain/validate edge case 회귀 강화
3. parity vector 확장

검증:
- `./gradlew :manifesto-compiler:test :manifesto-core:test`

완료 조건:
- compiler/core 모두 90% 근접

---

### Phase 8. 통합 마감 (Release Readiness)
작업:
1. 전체 테스트 + 골든 정합 통과
2. 문서 전체 동기화(README/spec/fdr/book/report)
3. 최종 parity 리포트 갱신
4. 릴리즈 체크리스트/태그 준비

검증:
- `./gradlew test`
- `./gradlew checkGoldenSync`

완료 조건:
- 코드/테스트/문서 기준 완료 선언 가능

## 6. 실행 순서 (권장 사이클)
1. Cycle 1: Phase 0 + Phase 1
2. Cycle 2: Phase 2
3. Cycle 3: Phase 3
4. Cycle 4: Phase 4
5. Cycle 5: Phase 5
6. Cycle 6: Phase 6
7. Cycle 7: Phase 7 + Phase 8

## 7. 리스크 및 대응
1. TS 변경 추종 리스크:
- 대응: 사이클 시작 전 TS diff 스캔 + parity matrix 갱신 의무화

2. 모듈 간 계약 드리프트:
- 대응: spec/fdr 동시 갱신, cross-module integration test 추가

3. 테스트 부채 증가:
- 대응: 각 phase 완료 조건에 테스트 증설을 강제

## 8. 운영 규칙
1. 기능 추가는 반드시 테스트와 같은 커밋 시퀀스로 반영
2. 문서는 사이클 종료마다 동기화
3. 우선순위 이탈 금지(현재 phase 완료 전 다음 phase 착수 금지)
