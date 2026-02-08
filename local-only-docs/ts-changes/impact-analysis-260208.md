# TS 변경 영향 분석 (2026-02-08)

기준 변경 문서
- `local-only-docs/ts-changes/changes-260203.md`
- `local-only-docs/ts-changes/impact-checklist-260203.md`

분석 목적
- TS 변경점 2건(`namespace + semantic schema hashing`, `onceIntent`)에 대한 Java 코드 상태를 확인하고 후속 작업을 확정한다.

## 1) feat(platform): namespace + semantic schema hashing

### 현재 구현 확인
- Core hash 계산 진입점: `manifesto-core/src/main/java/ai/manifesto/core/core/ValidationUtils.java:478`
- hash 입력 구성(`toSchemaMap`)은 `id`, `version`, `types`, `state`, `computed`, `actions`, `meta` 기준: `manifesto-core/src/main/java/ai/manifesto/core/core/ValidationUtils.java:485`
- DomainSchema 모델에 별도 namespace 필드 없음: `manifesto-core/src/main/java/ai/manifesto/core/schema/DomainSchema.java:20`
- App/World 측 schemaHash 저장/전파 로직은 현재 최소 구현 범위에서 확인 어려움 (`manifesto-world` 모듈 부재)

### 영향도 판정
- Core: `High`
- Compiler: `Medium`
- App/World: `High` (구현 공백)

### 갭 확정 (초기)
1. schema hash 계산에 namespace 반영 여부를 검증할 모델/필드가 현재 없음
2. semantic schema hashing 규칙이 TS와 완전 동치인지 검증 테스트가 부족함
3. App/World의 schema hash lifecycle(저장/전파/lineage) 정합성은 현재 점검 불가

### 후속 작업
1. hash 동치성 테스트 추가 (`TS fixture` 기반)
2. World 도입 전까지 App 계층의 schemaHash 전달 최소 명세 문서화

## 2) feat(compiler): onceIntent contextual keyword

### 현재 구현 확인
- Lexer keyword는 `once`만 존재: `manifesto-compiler/src/main/java/ai/manifesto/compiler/lexer/Lexer.java:372`
- TokenKind에 `ONCE_INTENT` 없음: `manifesto-compiler/src/main/java/ai/manifesto/compiler/lexer/TokenKind.java:14`
- Parser는 `once(...)` 구문만 파싱: `manifesto-compiler/src/main/java/ai/manifesto/compiler/parser/Parser.java:215`
- AST는 `OnceStmtNode`만 존재: `manifesto-compiler/src/main/java/ai/manifesto/compiler/parser/OnceStmtNode.java:10`
- Analyzer/IR도 `OnceStmtNode` 경로만 처리: `manifesto-compiler/src/main/java/ai/manifesto/compiler/analyzer/ScopeAnalyzer.java:94`, `manifesto-compiler/src/main/java/ai/manifesto/compiler/AstIrGenerator.java:326`
- 테스트는 `once(...)` 케이스만 존재: `manifesto-compiler/src/test/java/ai/manifesto/compiler/ParserFullTest.java:157`

### 영향도 판정
- Compiler: `High`

### 갭 확정 (초기)
1. `onceIntent` 토큰/파서/AST/검증/IR 경로가 모두 미구현
2. golden 테스트에 `onceIntent` 케이스가 없음

### 후속 작업
1. `onceIntent` edge case 골든 케이스 추가 보강
2. TS fixture 기반 회귀 테스트 확장

## 3) 결론

반영 완료 항목 (2026-02-08)
1. Core hash 계산 시 `meta.namespace` 반영
2. Compiler `onceIntent` end-to-end 지원 및 골든 테스트 통과 정합화
3. Core 내부 `System.currentTimeMillis()` 제거 (결정성 1차 정리)

주의
- `fix(deps)`, `release` 계열 변경은 Java 포팅 코드 직접 영향 없음으로 분류한다.
