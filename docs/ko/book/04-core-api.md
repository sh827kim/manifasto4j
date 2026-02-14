# 04. 모듈별 역할 요약 (아키텍처 지도)

이 장은 "각 모듈이 무엇을 담당하는지"를 빠르게 파악하기 위한 요약입니다.

## 모듈 역할 한눈에 보기

| 모듈 | 한 줄 역할 | 주 입력 | 주 출력 |
| --- | --- | --- | --- |
| `manifesto-core` | 순수 계산 엔진 | schema, snapshot, intent | compute/apply 결과, trace |
| `manifesto-host` | compute-effect 실행 루프 | compute 결과(requirements) | effect 반영 snapshot |
| `manifesto-app` | 상위 런타임 API/조립 계층 | action 요청, runtime 설정 | ActionHandle, session/branch 상태 |
| `manifesto-world` | 승인/거절/라인리지 거버넌스 | proposal, actor, policy | decision, lineage, event |
| `manifesto-compiler` | MEL 파싱/검증/로워링 | MEL 텍스트 | DomainSchema/patch IR |
| `manifesto-intent-ir` | 자연어 의도 중간표현 정규화 | translator draft | canonical IR + keys + diagnostics |
| `manifesto-translator` | 자연어 -> 구조화 의도 파이프라인 | NL 입력, 정책, adapter | TranslationResult, exporter 결과 |
| `manifesto-codegen` | schema 기반 코드 생성 | CodegenRequest | GeneratedArtifact 목록 |

## 모듈 간 협업 관점
- 실행 경로(Production Runtime): `app -> world -> host -> core`
- 개발/도구 경로(Authoring Tooling): `compiler`, `intent-ir`, `translator`, `codegen`
- `core`는 "비즈니스 의미"의 기준점입니다.

## 신입 개발자에게 추천하는 코드 읽기 순서
1. `manifesto-core`: 상태 전이 원리 이해
2. `manifesto-host`: effect 루프 이해
3. `manifesto-app`: 실제 사용 API 이해
4. `manifesto-world`: 승인 정책/기록 이해
5. `manifesto-compiler`: MEL에서 schema로 내려오는 과정
6. `manifesto-intent-ir` + `manifesto-translator`: LLM 연동 파이프라인
7. `manifesto-codegen`: 개발 생산성 자동화

## 코드 리뷰 시 반드시 확인할 질문
- 이 변경이 `core` 결정성을 깨지 않는가?
- effect 실행 책임이 `host` 경계를 넘지 않는가?
- world 승인/기록 없이 실행되는 우회 경로가 없는가?
- diagnostic/trace로 운영 추적이 가능한가?

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [05. 코드베이스 읽는 순서와 실무 온보딩 가이드](./05-packages.md)
<!-- NEXT_DOC_END -->
