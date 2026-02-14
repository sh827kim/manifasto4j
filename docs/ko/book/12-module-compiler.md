# 12. 모듈 상세: manifesto-compiler

## 모듈 역할
`manifesto-compiler`는 MEL 텍스트를 분석해 실행 가능한 구조(`DomainSchema`, patch IR)로 변환합니다.

## 패키지 트리 (root tree)

```text
ai.manifesto.compiler
├─ analyzer
├─ diagnostics
├─ lexer
├─ parser
└─ renderer
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.compiler` | 컴파일 파사드, lowering, runtime evaluator, CLI |
| `lexer` | 토큰화 |
| `parser` | AST 구성 |
| `analyzer` | 스코프/의미 규칙 검증 |
| `diagnostics` | 진단 코드/심각도/위치 모델 |
| `renderer` | MEL/patch fragment 재출력 |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `SimpleCompiler` | 기본 compiler 구현 |
| `MelCompiler`/`MelPatchCompiler` | MEL 컴파일 진입점 |
| `Lowering` | AST -> core IR 로워링 |
| `RuntimePatchEvaluator` | patch expr 실행 평가 |
| `CompilerCli` | `compile/format/check` CLI entrypoint |
| `CompilerCliSupport` | CLI 옵션 파싱/format 보조 |
| `MelSourceLoader` | 파일/클래스패스 로더 |
| `Parser`/`Lexer` | 문법 분석 핵심 |

## 실무에서 중요한 포인트
- compile 오류는 가능한 진단(`Diagnostic`)으로 표준화
- renderer를 통한 deterministic 포맷 유지
- TS 벡터 동기화 정책(`checkGoldenSync`)과 함께 관리

## 신입 개발자 추천 읽기 순서
1. `CompilerFacade`, `CompilationResult`
2. `Lexer` -> `Parser`
3. `Lowering`, `RuntimePatchEvaluator`
4. `CompilerCli`, `CompilerCliSupport`

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [13. 모듈 상세: manifesto-intent-ir](./13-module-intent-ir.md)
<!-- NEXT_DOC_END -->
