# 15. 모듈 상세: manifesto-codegen

## 모듈 역할
`manifesto-codegen`은 `DomainSchema`를 바탕으로 코드 산출물을 생성합니다.

현재 주요 타깃:
- `java-dto`
- `java-typed-client`

## 패키지 트리 (root tree)

```text
ai.manifesto.codegen
└─ runtime
```

## 패키지별 역할

| 패키지 | 역할 |
| --- | --- |
| `ai.manifesto.codegen` | codegen plugin/runner/target 계약과 기본 생성기 |
| `ai.manifesto.codegen.runtime` | diagnostics, path safety, stable hash, virtual fs |

## 주요 핵심 클래스

| 클래스 | 설명 |
| --- | --- |
| `CodegenRunner` | plugin 실행 오케스트레이션(`generateDetailed`, `generateComposite`) |
| `CodegenPluginRegistry` | plugin 등록/조회 |
| `CodegenPlugin` | plugin SPI 계약 |
| `JavaDtoCodeGenerator` | DTO 생성기 |
| `JavaTypedClientCodeGenerator` | typed client + input DTO 생성기 |
| `CodegenRequest`/`CodegenTarget` | codegen 입력 모델 |
| `CodegenRunResult` | files/diagnostics/schemaHash 결과 |
| `VirtualFileSystem` | 산출물 충돌/patch 합성 관리 |

## 구현 포인트
- path safety 검증으로 위험 경로 차단
- generated header/stable hash로 산출물 추적성 강화
- multi-plugin sequential mode(`generateComposite`)로 TS runner 모델 정렬

## 신입 개발자 추천 읽기 순서
1. `CodegenRequest`, `CodegenTarget`, `GeneratedArtifact`
2. `CodegenPlugin`, `CodegenPluginRegistry`
3. `CodegenRunner`
4. `JavaDtoCodeGenerator`, `JavaTypedClientCodeGenerator`
5. `runtime` 패키지(`VirtualFileSystem`, `PathSafety`, `CodegenDiagnostic`)

<!-- NEXT_DOC_START -->
---

## 다음 문서
- [학습서 처음으로 (Index)](./index.md)
<!-- NEXT_DOC_END -->
