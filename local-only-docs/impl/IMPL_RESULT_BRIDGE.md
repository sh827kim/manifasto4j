# IMPL_RESULT_BRIDGE

## Scope
TypeScript `packages/bridge` 대비 Java `manifesto-bridge` 구현 상태를 비교 정리한다. 이 문서는 **미구현/불일치**를 중심으로 정리한다.

## Source of Truth (TypeScript)
- https://github.com/manifesto-ai/core.git `packages/bridge/dist` (TS 소스 미포함, dist 기준)

## 대상 (Java)
- `manifesto-bridge/src/main/java/ai/manifesto/bridge`

## 구현됨 (요약)
- `Projection` 인터페이스 (SourceEvent + SnapshotView → Intent)
- `SnapshotView` (data + computed)
- `SourceEvent` (kind, eventId, payload, occurredAt)
- `BridgeRuntime` (Projection 실행기)
- `BridgeRuntime` routed projection 지원
  - event kind별 projection 라우팅
  - fallback projection

## 미구현
### 1) Bridge API 전체 부재
- TS `Bridge` 클래스 기능 다수 미구현:
  - subscribe/get/getSnapshot/getWorldId/refresh
  - dispatch(IntentBody), dispatchEvent(SourceEvent)
  - set(path,value) 편의 API
  - register/unregister projection, registry 접근
  - action catalog projection
  - dispose/구독 관리

### 2) World/Intent 연동
- TS는 `ManifestoWorld` 및 `IntentIssuer`를 통해 IntentInstance 발행
- Java는 World/Issuer 개념이 없고, Projection이 `Intent`를 직접 반환

### 3) Projection Registry / Recorder
- TS는 projection registry(등록/라우팅)와 recorder(감사/리플레이 로그) 제공
- Java는 기본 routed map 수준만 지원 (정식 registry/recorder는 미구현)

### 4) Schema/Validation/Zod 기반 타입
- TS는 `SourceEvent`, `SnapshotView`, `ProjectionResult` 등 Zod 스키마 기반 검증 제공
- Java는 단순 record/POJO (런타임 검증 없음)

### 5) Projection Result 타입
- TS는 `ProjectionResult`(none/intent)와 reason, scopeProposal 등을 포함
- Java는 `Intent` 단일 반환 (none/intent 구분 불가)

### 6) Action Catalog (v1.1)
- TS는 Action Catalog projector, pruning 옵션, hash 제공
- Java는 action catalog 관련 기능 없음

### 7) 오류 모델
- TS는 BridgeError 및 코드 집합 제공
- Java는 오류 모델 미구현

### 8) 이벤트 범위
- TS `SourceKind`는 `system/ui/api/agent` 모두 지원
- Java `SourceEvent.Kind`도 UI/API/AGENT/SYSTEM 있지만
  - Bridge/Projection/Issuer 경계에서 사용 정책 및 변환 로직 부재

## 불일치
- (현재까지 발견된 주요 불일치 없음 — 구현 범위가 제한되어 미구현이 대부분)

## 정리
- Java bridge는 **projection 실행기 수준**만 제공
- TS bridge의 핵심 기능(세계 연동, 이벤트 라우팅, 구독, catalog, 오류 모델)은 대부분 미구현
- Bridge 이벤트는 TS 기준 **UI 전용이 아님** (system/ui/api/agent 모두 지원)

## 다음 작업 후보 (bridge 기준)
1. Bridge API 도입: subscribe/dispatch/dispatchEvent/registry/recorder
2. ProjectionResult 모델(none/intent) 도입
3. IntentIssuer/World 연동 추상화 추가
4. Action Catalog projector 대응

## 최근 업데이트 (2026-02-08)
- routed projection 기능 추가 + 단위 테스트 보강
- Spring AI adapter 1차 정의
  - `ExternalEventAdapter` 인터페이스 추가
  - `SpringAiMessageAdapter` 추가 (type/eventId/payload/metadata 정규화)
  - `SpringAiMessageAdapterTest` 추가
