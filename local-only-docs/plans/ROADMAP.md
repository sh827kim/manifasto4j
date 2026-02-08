# ROADMAP (중장기 포팅 로드맵)

작성일: 2026-02-08
대상: `/workspace/manifesto-java-core`
역할: **중장기 로드맵** (우선순위/기간/완료 기준을 명시)
근거: `local-only-docs/reports/porting-evaluation-report.ko.md`

---

## 0. 목표 정의

**최종 목표**
- 순수 Java 기반 Manifesto 스택 완성
- Core 결정성 보장
- World/Bridge/Host를 포함한 실사용 가능한 거버넌스 기반 실행 경로 확보
- MEL 컴파일까지 JVM 내부에서 완결

**운영 목표**
- LLM/에이전트 통합 시 “Intent 구조화 + 승인 + 실행 + 추적”이 일관되게 동작

---

## 1. 우선순위별 액션 플랜

### Priority 1: 결정성/거버넌스 핵심 안전성 확보

**P1-1. Core의 System time 제거**
- 목적: 결정성 보장
- 범위:
  - `manifesto-core/src/main/java/ai/manifesto/core/HostContext.java`
  - `manifesto-core/src/main/java/ai/manifesto/core/Snapshot.java`
  - `manifesto-core/src/main/java/ai/manifesto/core/TraceNode.java`
  - `manifesto-core/src/main/java/ai/manifesto/core/Requirement.java`
  - `manifesto-core/src/main/java/ai/manifesto/core/utils/DagUtils.java`
  - `manifesto-core/src/main/java/ai/manifesto/core/core/Compute.java`
- 작업 방식:
  - Core 내부에서 `System.currentTimeMillis()` 직접 호출 제거
  - 외부에서 주입된 `HostContext.getNow()`만 사용
- 완료 기준:
  - 동일 입력 시 동일 결과 재현 가능
  - Snapshot meta/trace timestamp가 HostContext 기반으로만 생성됨
- 상태:
  - 완료 (2026-02-08)

**P1-2. World 정식 구현 (TS 정합 기준)**
- 목적: 승인/라인리지 기반 거버넌스 확보
- 현재 상태:
  - Phase 0~1 1차 완료 (모듈/타입/상태기계/queue)
- 다음 단계:
  - Phase 2: hash/factory
  - Phase 3~4: registry + lineage + persistence
  - Phase 5~6: authority + orchestrator
- 완료 기준:
  - TS `packages/world` 주요 시나리오 동치
  - Proposal/Decision/Lineage/Authority/Execution 경로 통합 테스트 통과
- 최근 진행:
  - P0 하드닝, escalation fallback, lineage query 확장 반영
  - world golden(`approve/reject`) 테스트 추가 완료

---

### Priority 2: 실행/연동 레이어 확장

**P2-1. Bridge 확장 (Spring AI 결합용)**
- 목적: Intent 구조화와 Snapshot 전달을 안전하게 수행
- 핵심 기능:
  - SourceEvent → Intent 구조화
  - SnapshotView → 도메인 안전 노출
  - 외부 메시지/이벤트 버스와의 통합 설계
- 완료 기준:
  - LLM 도구 호출이 Intent로 변환되어 World에 제출됨
  - Snapshot이 Bridge를 통해 구독 가능

**P2-2. Host 확장 (프로덕션 실행 정책)**
- 목적: Effect 실행의 안정성/정책/관찰성 확보
- 핵심 기능:
  - 효과 실행 실패 처리(재시도 정책, 실패 기록)
  - 타임아웃 정책
  - 병렬 실행 옵션 및 순서 제어
- 완료 기준:
  - 효과 실패가 Snapshot에 일관된 방식으로 기록됨

---

### Priority 3: 컴파일 및 DSL 안정화

**P3-1. MEL 컴파일러 정합성 검증 강화**
- 목적: TS 컴파일러와 스키마 동치성 확보
- 방법:
  - 골든 벡터 테스트 추가
  - 진단 메시지/위치 정보 비교
- 완료 기준:
  - 동일 MEL 입력 → 동일 DomainSchema 출력
- 최근 진행:
  - compiler golden에 `onceIntent` edge + namespace hash impact 케이스 반영

**P3-2. Builder 전략 재검토**
- 목적: Java DSL의 필요성과 범위를 확정
- 선택지:
  - Java DSL 최소 유지
  - MEL 중심 개발로 전환

---

## 2. 실행 순서 제안 (Milestones)

1. **World 정식 구현 후속 확장 (Phase 8+)** (P1-2)
2. **Host 정책/실패 처리 강화** (P2-2)
3. **Bridge MVP 확장** (P2-1)
4. **Compiler lowering/evaluation 정식 계층 전환** (P3-1)
5. **Builder 전략 확정** (P3-2)

---

## 3. 우선순위/기간/담당 역할 (초안)

**표 설명**
- 기간은 “작업 규모 감” 기준의 러프 추정입니다.
- 담당 역할은 팀 구성에 맞게 치환 가능합니다.

| 항목 | 우선순위 | 예상 기간 | 담당 역할(예시) | 완료 기준 |
| --- | --- | --- | --- | --- |
| P1-1 Core System time 제거 | P1 | 완료 | Core 엔지니어 | 동일 입력 → 동일 결과 재현 |
| P1-2 World 정식 구현 | P1 | 4~8주 | 플랫폼/거버넌스 | TS world 시나리오 정합 통과 |
| P2-1 Bridge 확장 | P2 | 2~4주 | 통합/플랫폼 | Intent 구조화 + Snapshot 구독 |
| P2-2 Host 확장 | P2 | 2~4주 | 런타임/플랫폼 | 실패/재시도 정책 동작 |
| P3-1 Compiler 정합성 | P3 | 3~6주 | 컴파일러 | Golden test 통과 |
| P3-2 Builder 전략 확정 | P3 | 1~2주 | 아키텍처 | 방향 결정 문서화 |

---

## 4. 리스크 및 완화 전략

- **Core 결정성 실패** → 시스템 시간 제거 우선
- **World 부재로 인한 승인 누락** → MVP라도 우선 구현
- **LLM 통합 불안정** → Bridge에서 Intent 구조화 검증 강화
- **컴파일 정합성 불일치** → 골든 테스트 기반 동치성 검증

---

## 5. 관련 문서

- 설계 초안: `local-only-docs/design/porting-design-drafts.ko.md`
- World MVP 설계(참고): `local-only-docs/design/world-mvp-design.ko.md`
- World 정식 구현 계획: `local-only-docs/plans/WORLD_FULL_IMPLEMENTATION_PLAN_2026-02-08.md`
