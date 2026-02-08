**04. Core API 개념 가이드 (Java)**
이 장은 Core의 역할을 **Java 개발자가 이해하기 쉬운 형태로** 정리한 개념 가이드입니다. 실제 클래스/메서드 명은 모듈 구현에 따라 다를 수 있습니다.

**Core의 책임**
- Intent를 계산하여 다음 Snapshot을 산출
- Patch를 적용하여 Snapshot을 갱신
- 스키마 검증과 값의 근거 설명 제공

**핵심 API (개념적 인터페이스 예시)**
아래는 이해를 돕기 위한 **개념 예시**입니다.

```java
public interface Core {
    ComputeResult compute(Schema schema, Snapshot snapshot, Intent intent, Context context);
    ApplyResult apply(Schema schema, Snapshot snapshot, List<Patch> patches, Context context);
    ValidationResult validate(Schema schema);
    ExplainResult explain(Schema schema, Snapshot snapshot, String path);
}
```

**기본 사용 흐름**
1. Core 인스턴스를 생성합니다.
2. 스키마와 Snapshot, Intent를 준비합니다.
3. `compute` 결과로 Patch와 Requirement를 받습니다.
4. Host가 Effect를 실행하면 Patch를 반영하고 다시 계산합니다.

**compute 결과가 의미하는 것**
- `snapshot`: 계산 결과 Snapshot
- `requirements`: Host가 실행해야 하는 Effect 목록
- `trace`: 계산 과정의 설명 로그

**apply는 언제 쓰나요**
- Effect 실행 결과 Patch를 Snapshot에 적용할 때 사용합니다.
- apply는 computed 값을 다시 계산해 새 Snapshot을 만듭니다.

**validate는 무엇을 검증하나요**
- 스키마가 Manifesto 규칙을 만족하는지 확인합니다.
- 실제 서비스에 적용하기 전에 스키마 안정성을 체크합니다.

**explain의 활용**
- 특정 값이 왜 그렇게 되었는지 근거를 제공합니다.
- 디버깅과 감사(Audit)에 유리합니다.

**Java 구현 시 주의사항**
- Core 안에서 I/O는 금지됩니다.
- Core 안에서 `System.currentTimeMillis()` 같은 벽시계 접근은 허용되지 않습니다.
- 계산 도중 예외를 던지지 않고 “값으로 표현된 오류”를 반환하는 쪽이 안전합니다.

**현재 구현 보강 메모 (2026-02-03 기준)**  
- Java Core에는 `validateSnapshot` 계열 보조 API가 추가되어 있습니다.  
- Core 내부에 시스템 시간을 직접 참조하는 구간이 남아 있어 결정성 보장이 약화됩니다.  
  - `HostContext`에서 시간 입력을 제공하도록 정리하는 것이 목표입니다.

**체크포인트 질문**
1. compute와 apply의 역할 차이는 무엇인가요.
2. Core가 Effect를 직접 실행하면 어떤 문제가 생기나요.
3. explain은 어떤 상황에서 특히 유용할까요.
