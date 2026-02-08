# TODAY PLAN (2026-02-08)

목표: TS 변경점(`namespace + semantic schema hashing`, `onceIntent`)을 Java 포팅 코드에 반영하기 위한 당일 실행 계획.

## 범위
- 기준 문서
  - `local-only-docs/ts-changes/changes-260203.md`
  - `local-only-docs/ts-changes/impact-checklist-260203.md`
- 기준 코드
  - `manifesto-core`
  - `manifesto-compiler`
  - (참고) `manifesto-app`, `manifesto-world`

## 오늘 작업

### 1. TS 변경점 영향도 확정 (P0)
- 산출물: `local-only-docs/ts-changes/impact-analysis-260208.md`
- 작업
  - hash/namespace 관련 현재 구현 점검
  - onceIntent 관련 parser/AST/scope/validator/IR 점검
  - 모듈별 갭/리스크/후속 작업 정리

### 2. Core schema hash 정합화 착수 (P0)
- 작업
  - `ValidationUtils.computeSchemaHash` 규칙 점검
  - namespace 반영 여부 확인 및 수정안 확정
  - 테스트 케이스 추가 계획 수립

### 3. Compiler onceIntent 지원 착수 (P0)
- 작업
  - Lexer/TokenKind/Parser/AST 확장 설계
  - Scope/Validator/AstIrGenerator 반영 포인트 확정
  - 최소 테스트 케이스 초안 작성

### 4. 결과 문서 반영 (P1)
- `local-only-docs/PORTING_SUMMARY.md`
- `local-only-docs/plans/NEXT_ACTIONS.md`

## 완료 기준 (오늘)
- [x] 1번 영향 분석 문서 완료
- [x] 2번 또는 3번 중 최소 1개 구현 PR 단위 변경 착수
- [x] 변경점이 PORTING_SUMMARY/NEXT_ACTIONS에 반영됨

## 진행 상태
- 2026-02-08: 계획 문서 생성
- 2026-02-08: 1번 완료 (`local-only-docs/ts-changes/impact-analysis-260208.md` 작성, 체크리스트 상태 갱신)
- 2026-02-08: 2번 완료 (Core schema hash에 meta namespace 반영, Validate 테스트 추가)
- 2026-02-08: 3번 완료 (Compiler `onceIntent` parser/analyzer/IR/renderer + 테스트 반영)
- 2026-02-08: 컴파일러 골든 테스트 실패 원인 보완 완료 (`computed.*` 경로, number literal 정규화, `isNotNull` lowering, literal union type 정합)
