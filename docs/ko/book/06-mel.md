**06. MEL 문법 입문 (포팅 관점)**
이 장은 MEL(Manifesto Expression Language)의 핵심 문법을 교재형으로 정리합니다. 자세한 전체 문법은 공식 문서를 참고하세요.

**MEL이란 무엇인가**
- Manifesto 도메인을 선언형으로 정의하는 언어입니다.
- 상태, 계산, 액션, 이펙트를 코드가 아닌 구조로 표현합니다.

**기본 구조**
MEL 파일은 하나의 `domain`만 정의합니다.

```mel
domain Counter {
  state { count: number = 0 }

  computed doubled = mul(count, 2)

  action increment() {
    when true {
      patch count = add(count, 1)
    }
  }
}
```

**State 선언**
- 모든 필드는 타입과 기본값을 가져야 합니다.
- 기본 타입, 배열, 레코드, 유니온을 사용할 수 있습니다.

```mel
state {
  count: number = 0
  name: string = ""
  status: "idle" | "done" = "idle"
  tags: Array<string> = []
  users: Record<string, User> = {}
}
```

**Computed**
- 상태에서 파생되는 값이며 항상 재계산됩니다.
- 부작용이 없고, 순수해야 합니다.

```mel
computed isEmpty = eq(len(items), 0)
computed label = gt(count, 0) ? "Positive" : "Non-positive"
```

**Action**
- 상태 변화를 선언합니다.
- 모든 변경은 `when` 또는 `once` 안에서만 가능합니다.

```mel
action addAmount(amount: number) {
  when gt(amount, 0) {
    patch count = add(count, amount)
  }
}
```

**when과 once**
- `when`은 조건이 참일 때만 실행합니다.
- `once`는 Intent마다 1회 실행되는 블록입니다.

```mel
action submit() {
  once(submittedAt) when isNull(submittedAt) {
    patch submittedAt = $meta.intentId
    effect api.submit({ data: form, into: result })
  }
}
```

**Patch 연산**
- `set`, `unset`, `merge` 세 가지가 전부입니다.

```mel
patch count = add(count, 1)
patch tasks[id] unset
patch user merge { name: "Alice" }
```

**Effect 선언**
- 외부 작업은 Effect로 선언하고 Host가 실행합니다.
- Effect 결과는 `into`로 상태에 반영합니다.

```mel
effect api.fetch({ url: "/tasks", into: tasks })
effect record.keys({ source: tasks, into: taskIds })
```

**오류와 종료**
- `fail`은 오류로 종료합니다.
- `stop`은 정상 종료지만 아무 일도 하지 않습니다.

```mel
action createUser(email: string) {
  when eq(trim(email), "") {
    fail "MISSING_EMAIL"
  }

  when eq(at(users, email), null) {
    patch users[email] = { email: email }
  }
}
```

**금지되는 패턴 요약**
- Computed 안에서 Effect 사용
- Guard 밖에서 patch/effect 실행
- 상태 기본값에 시스템 값 사용
- 직접 대입문 사용

**Java 포팅 관점 팁**
- MEL 컴파일 결과는 **Schema/Flow 데이터 구조**로 내려옵니다.
- Java 모듈에서는 이 구조를 “정적 모델”로 취급하고, **Core가 해석**합니다.
- MEL 자체를 런타임 파싱할지, 빌드 시점에 컴파일할지는 운영 전략에 따라 달라집니다.

**현재 컴파일러 구현 상태 (2026-02-03 기준)**  
- MEL 파이프라인은 구축되어 Lexer/Parser/Analyzer/IR 생성이 가능합니다.  
- lowering/evaluation 계층은 Lite 수준이며, 스펙 기반 오류 코드/제약 정합은 진행 중입니다.  
- `SimpleCompiler`는 MEL-lite 범위를 빠르게 다루는 용도로 제공됩니다.

**체크포인트 질문**
1. 왜 모든 patch는 when/once 안에 있어야 하나요.
2. computed에 effect가 들어가면 어떤 문제가 생기나요.
3. `stop`과 `fail`의 차이를 설명해보세요.
