# Manifesto Java Codegen FDR (Porting)

| Field | Value |
| --- | --- |
| Status | Bootstrap completed (Java skeleton) |
| Scope | codegen 설계 메모 |

## 1. Goals

- schema 기반 코드 생성 공통 계약을 Java 모듈로 고정
- 타깃별 구현체를 모듈 외부/추가 모듈에서 주입 가능하도록 유지

## 2. Follow-ups

- 첫 타깃(Java DTO/Client 등) 우선순위 확정
- 템플릿 엔진 선택(직접 렌더링 vs 템플릿 라이브러리) 및 표준화
