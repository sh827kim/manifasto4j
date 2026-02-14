# 14. 모듈 상세: manifesto-translator

## 모듈 역할
`manifesto-translator`는 자연어 입력을 구조화된 실행 대상(그래프/진단/export 결과)으로 변환하는 파이프라인입니다.

핵심 책임:
- interpret -> verify -> refine 파이프라인
- adapter SPI를 통한 프레임워크 비종속 경계
- 정책 기반 검증
- target exporter(json/manifesto/openapi)

## 패키지 트리 (root tree)

```text
ai.manifesto.translator
├─ adapters
│  ├─ profile
│  └─ spi
│     └─ provider
├─ core
├─ helpers
├─ invariants
├─ pipeline
├─ plugins
├─ strategies
└─ targets
   ├─ json
   ├─ manifesto
   └─ openapi
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.translator` | translator 엔트리/정책/provider/기본 구현 |
| `adapters.spi` | LLM port/request/response 계약 |
| `adapters.profile` | provider별 요청/응답 매핑 규약 |
| `core` | intent graph, execution plan, diagnostics 모델 |
| `pipeline` | 파이프라인 옵션/진단 집계 |
| `plugins` | stage hook 기반 확장 플러그인 |
| `strategies` | 분해/번역/병합 전략 조합 |
| `helpers` | 그래프/청크/실행계획 검증 보조 |
| `invariants` | 인과성/완전성/상태성 불변식 검사 |
| `targets.*` | export 결과 생성(json/manifesto/openapi) |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `DefaultTranslator` | 통합 translator 구현 |
| `TranslatorPipeline` | stage 실행기 |
| `RuleBasedInterpreter` | 기본 해석기 |
| `DefaultTranslatorVerifier` | 정책/규칙 검증기 |
| `DefaultTranslatorRefiner` | 후처리 정제기 |
| `IntentIrResolutionPlugin` | intent-ir resolver/lexicon 브리지 |
| `ProviderBindings` | 지원 provider 바인딩 카탈로그 |
| `TargetExporters` | exporter 레지스트리 |

## 신입 개발자 추천 읽기 순서
1. `Translator`, `TranslationRequest`, `TranslationResult`
2. `DefaultTranslator`, `TranslatorPipeline`
3. `RuleBasedInterpreter`, `DefaultTranslatorVerifier`
4. `TranslatorPipelinePlugin`, `IntentIrResolutionPlugin`
5. `adapters.spi`와 `targets` 계층

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [15. 모듈 상세: manifesto-codegen](./15-module-codegen.md)
<!-- NEXT_DOC_END -->
