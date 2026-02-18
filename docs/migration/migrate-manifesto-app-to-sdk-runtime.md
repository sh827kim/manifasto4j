# Migration: `manifesto-app` -> `manifesto-sdk` / `manifesto-runtime`

이 문서는 `manifesto-app` 제거 이후 Java 사용자 마이그레이션 절차를 정리합니다.

## 1) 의존성 변경

- 이전: `ai.manifesto:manifesto-app`
- 이후(권장): `ai.manifesto:manifesto-sdk`
- 내부 구현 모듈: `ai.manifesto:manifesto-runtime` (애플리케이션 소비자는 직접 의존하지 않는 것을 권장)

## 2) import 변경

- 이전:
```java
import ai.manifesto.app.App;
import ai.manifesto.app.AppFactory;
import ai.manifesto.app.AppConfig;
```

- 이후:
```java
import ai.manifesto.sdk.App;
import ai.manifesto.sdk.AppFactory;
import ai.manifesto.sdk.AppConfig;
```

## 3) 기본 생성 경로

```java
App app = AppFactory.createApp(
    schema,
    Map.of("status", "seed"),
    effects
);
app.ready();
```

또는

```java
App app = AppFactory.createApp(
    AppConfig.sdk(schema, Map.of("status", "seed"), effects)
);
app.ready();
```

## 4) 메모리 provider/verifier 설정(선택)

```java
AppConfig config = new AppConfig(
    schema,
    null,
    null,
    Map.of(),
    effects,
    memoryProvider,
    memoryVerifier,
    false
);
App app = AppFactory.createApp(config);
```

## 5) 호환성 주의사항

- `ai.manifesto.runtime.*` 타입은 SDK 내부 위임 대상이며, 애플리케이션 코드에서 직접 참조하지 않는 것을 권장합니다.
- SDK 공개 surface는 `ai.manifesto.sdk.*` 기준으로 유지됩니다.
