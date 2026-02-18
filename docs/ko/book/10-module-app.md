# 10. 모듈 상세: manifesto-sdk + manifesto-runtime

## 모듈 역할
`manifesto-sdk`는 공개 진입 API를 제공하고, `manifesto-runtime`은 실제 App 실행/상태 관리 구현을 담당합니다.

주요 기능:
- sdk canonical entry(`AppFactory`, `AppConfig`)
- app lifecycle(`ready`, `dispose`)
- action 실행(`act`)과 상태 구독(`subscribe`)
- session/branch 관리
- hook 확장 포인트
- memory/world store 연계

## 패키지 트리 (root tree)

```text
ai.manifesto.sdk
ai.manifesto.app
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.sdk` | canonical app 생성 진입점, sdk config |
| `ai.manifesto.app` | runtime 구현/계약, 옵션, 예외, hook, session, memory/world store |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `ai.manifesto.sdk.AppFactory` | sdk canonical 생성 API |
| `ai.manifesto.sdk.AppConfig` | sdk 구성 계약 |
| `App` | 앱 공개 인터페이스 |
| `DefaultApp` | 기본 런타임 구현 |
| `AppFactory` | app 조립 팩토리 |
| `ActionHandle` | action 비동기 결과/phase 추적 핸들 |
| `AppSession`/`DefaultAppSession` | session 관리 |
| `ActOptions`/`SubscribeOptions`/`SessionOptions` | 실행/구독/세션 옵션 |
| `AppHook`/`HookContext` | 이벤트 훅 확장 계약 |
| `AppWorldStore`/`AppSnapshotStore` | 영속 경계 계약 |

## App이 있는 이유
직접 `core + host + world`를 조합하면 서비스 코드가 복잡해집니다.
`sdk + runtime` 조합은 실행 흐름을 표준화해 다음 이점을 줍니다.
- API 일관성
- lifecycle 통제
- 회귀 테스트 기준점 확보

## 신입 개발자 추천 읽기 순서
1. `App` 인터페이스
2. `DefaultApp` 구현
3. `ActionHandle`, `ActionResult` 계열
4. `AppHook`, `Hookable`
5. session/branch 관련 예외 및 옵션 타입

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [11. 모듈 상세: manifesto-world](./11-module-world.md)
<!-- NEXT_DOC_END -->
