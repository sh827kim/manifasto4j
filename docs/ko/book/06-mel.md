# 06. MEL 입문과 컴파일러 관점

`MEL`은 도메인 규칙을 텍스트로 정의하는 언어입니다. Java에서는 `manifesto-compiler`가 이 MEL을 읽어 `DomainSchema`/IR로 변환합니다.

## 1) MEL이 필요한 이유
- 코드 수정 없이 규칙을 선언형으로 관리
- 사람이 읽기 쉬운 도메인 정책 정의
- 컴파일 후에는 `core`가 동일한 방식으로 계산

## 2) 매우 단순한 예제

```mel
domain Counter {
  state { count: number = 0 }

  action increment() {
    when true {
      patch count = add(count, 1)
    }
  }
}
```

의미:
- `state`: 저장할 상태 필드
- `action`: 실행 가능한 도메인 동작
- `when`: 조건 가드
- `patch`: 상태 변경 명령

## 3) Java 컴파일러 파이프라인
1. `Lexer`: 토큰화
2. `Parser`: AST 생성
3. `Analyzer`: 스코프/의미 검증
4. `Lowering`: 코어 IR로 변환
5. `RuntimePatchEvaluator`: patch/expr 평가 보조

## 4) CLI 사용
현재 Java 컴파일러는 CLI entrypoint를 제공합니다.
- `compile`
- `format`
- `check`

예시:

```bash
./gradlew :manifesto-compiler:test
java -cp manifesto-compiler/build/classes/java/main:manifesto-core/build/classes/java/main ai.manifesto.compiler.CompilerCli check --source=/path/to/domain.mel
```

## 5) 신입 개발자가 주의할 점
- MEL에서 effect를 선언해도 실행은 Host에서만 일어납니다.
- 조건/타입 오류는 컴파일 단계 진단으로 잡는 것이 우선입니다.
- MEL 변경 후에는 관련 모듈 회귀 테스트를 반드시 돌려야 합니다.

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [07. 이 프로젝트로 할 수 있는 일 (LLM 활용 포함)](./07-llm-integration.md)
<!-- NEXT_DOC_END -->
