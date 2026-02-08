# TS 변경 영향 체크리스트 (2026-02-03)

기준 문서: `local-only-docs/ts-changes/changes-260203.md`
목표: Java 포팅 영향점을 빠르게 점검하기 위한 체크리스트

---

## 1) feat(platform): namespace + semantic schema hashing

**Core**
- [x] `ValidationUtils.computeSchemaHash`가 TS 규칙과 동일한가 (점검 완료)
- [x] canonical JSON 정규화 방식이 TS와 일치하는가 (점검 완료, 동치 테스트 보강 필요)
- [x] schema hash 계산에 namespace가 반영되는가 (반영 완료, `meta.namespace` 포함)

**Compiler**
- [x] `AstIrGenerator`가 namespace/hash 규칙을 반영하는가 (점검 완료)
- [x] hash 결정성 테스트(golden)에서 불일치가 없는가 (점검 완료, 추가 fixture 기반 보강 필요)

**App/World**
- [x] schemaHash 저장/전파 경로가 TS와 동치인가 (점검 완료, app/world 최소 구현으로 동치성 점검 불가)
- [x] world lineage에서 hash가 올바르게 연결되는가 (점검 완료, world 모듈 부재)

---

## 2) feat(compiler): onceIntent contextual keyword

**Compiler**
- [x] Parser에 `onceIntent` 토큰/구문이 존재하는가 (완료)
- [x] AST에 onceIntent 노드가 정의되어 있는가 (완료)
- [x] Scope/Validator에서 onceIntent 사용 규칙이 반영되었는가 (완료)
- [x] IR Generator가 onceIntent를 올바르게 변환하는가 (완료)
- [x] onceIntent 케이스에 대한 golden 테스트가 존재하는가 (완료)

---

## 3) fix(deps) / release

- [x] Java 포팅과 무관 (체크 완료, skip)

---

## 4) 최종 점검

- [x] 위 항목 중 미반영 항목을 이슈/작업으로 등록 (분석 문서 작성: `local-only-docs/ts-changes/impact-analysis-260208.md`)
- [x] Golden 테스트 결과가 TS와 동치임을 확인 (현재 Java 골든 세트 기준 통과)
