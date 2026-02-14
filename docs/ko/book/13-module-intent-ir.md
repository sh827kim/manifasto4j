# 13. 모듈 상세: manifesto-intent-ir

## 모듈 역할
`manifesto-intent-ir`는 자연어에서 얻은 의도를 **정규화 가능하고 검증 가능한 중간 표현**으로 다룹니다.

핵심 책임:
- normalize
- canonical serialize/hash
- key derivation(strict/semantic/sim)
- lexicon/resolver 검증
- lower 결과 제공

## 패키지 트리 (root tree)

```text
ai.manifesto.intentir
└─ schema
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.intentir` | normalize/hash/key/lexicon/resolver/lower 핵심 로직 |
| `ai.manifesto.intentir.schema` | intent-ir 구조 모델 + 스키마 validator |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `IntentIrDocument` | intent-ir 원본문서 모델 |
| `IntentIrNormalizer`/`DefaultIntentIrNormalizer` | 정규화 엔트리 |
| `IntentIrCanonicalizer` | canonical JSON 직렬화 |
| `IntentIrHashing` | 해시 생성 |
| `IntentIrKeyDeriver` | strict/semantic/sim key 파생 |
| `IntentIrLexicon`/`DefaultIntentIrLexicon` | 허용 action/요건 검증 |
| `IntentIrResolver`/`DefaultIntentIrResolver` | 미해결 action 보정 |
| `IntentIrLowerer`/`DefaultIntentIrLowerer` | 실행 경계용 lower 결과 생성 |

## 신입 개발자 추천 읽기 순서
1. `IntentIrDocument`
2. `DefaultIntentIrNormalizer`
3. `IntentIrCanonicalizer`, `IntentIrHashing`
4. `IntentIrKeyDeriver`
5. `DefaultIntentIrLexicon`, `DefaultIntentIrResolver`
6. `DefaultIntentIrLowerer`

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [14. 모듈 상세: manifesto-translator](./14-module-translator.md)
<!-- NEXT_DOC_END -->
