# 09. 모듈 상세: manifesto-host

## 모듈 역할
`manifesto-host`는 core 계산 결과의 `Requirement(Effect)`를 실제로 실행하는 런타임입니다.

핵심 책임:
- compute/effect 재진입 루프
- effect retry/timeout/failure 처리
- runtime trace 축적
- host namespace 일관성 유지

## 패키지 트리 (root tree)

```text
ai.manifesto.host
└─ runtime
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.host` | host public API 및 effect 실행 경계 |
| `ai.manifesto.host.runtime` | job/mailbox/runner 기반 실행 루프 |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `HostRuntime` | host 실행 파사드 |
| `HostRuntimeOptions` | retry/timeout/context provider 옵션 |
| `EffectHandler` | effect 처리 SPI |
| `EffectExecutor` | effect 실행/재시도/오류 래핑 |
| `HostRunner` | job drain 실행기 |
| `HostMailbox` | 실행 큐 인터페이스 |
| `InMemoryHostMailbox` | 기본 메모리 큐 |
| `HostJob`/`StartIntentJob`/`FulfillRequirementsJob` | 단계별 실행 단위 |

## 런타임 흐름 요약
1. intent 시작 job 등록
2. core compute 호출
3. pending requirements 추출
4. effect 실행 + 결과 patch 생성
5. apply/재compute 반복
6. terminal 상태 도달 시 종료

## 신입 개발자 추천 읽기 순서
1. `HostRuntime`, `HostRuntimeOptions`
2. `EffectHandler`, `EffectExecutor`, `EffectExecutionOutcome`
3. `HostRunner`, `HostJob` 계열
4. host 테스트(`compliance`, `golden`)로 동작 확인

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [10. 모듈 상세: manifesto-sdk + manifesto-runtime](./10-module-app.md)
<!-- NEXT_DOC_END -->
