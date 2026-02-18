# Manifesto Java Compiler SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | MEL → DomainSchema/patch compilation |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/compiler/docs/VERSION-INDEX.md` |
| Latest | `0.5.0` |

## 1. Scope

Compiler specifies lowering MEL into core IR and diagnostics.

## 2. Responsibilities

- Parse MEL and validate semantic rules
- Generate DomainSchema and Core IR
- Provide canonical form guarantees
- Provide diagnostic errors for invalid MEL

## 3. Integration Points

- Host uses compiler for Translator/MEL patch evaluation
- Compiler outputs Core IR compatible with core.apply()

## 4. Loader & Renderer Coverage (Cycle 7)

- `MelSourceLoader`를 통해 파일/클래스패스 MEL 로딩을 지원한다.
- `CompilerFacade` 연동 유틸리티(`compileFromFile`, `compileFromClasspathOrThrow`)를 제공한다.
- renderer는 malformed patch op, unknown op, newline/indent 옵션 경계 케이스를 회귀 테스트로 검증한다.

## 5. Runtime Evaluation Parity (TS Baseline)

- `RuntimePatchEvaluator`는 TS `evaluateExpr`와 연산 집합을 동치로 유지해야 한다.
- 필수 지원 연산: `substring`, `field`, `keys`, `values`, `entries`.
- `at` 연산은 `at(array, number)`와 `at(record, string)` 모두 지원해야 한다.
- runtime patch condition skip reason은 `false | null | non-boolean` 계약을 따른다.

## 6. Auxiliary Surface (Cycle 8 / P2-B)

- `CompilerCliSupport`를 추가해 CLI 인자 계약을 표준화한다.
  - 입력 소스: `--source` 또는 `--classpath` (상호배타)
  - 포맷팅 옵션: `--format-only`, `--indent`, `--newline`
- formatter는 parser + `MelRenderer`를 사용해 deterministic MEL 재출력을 제공한다.
- `check-golden-sync.sh`는 기본적으로 source 부재 시 N/A(성공) 처리하되,
  `CHECK_GOLDEN_SYNC_REQUIRE_SOURCE=1` strict 모드에서는 실패한다.
- `recover-golden-sync.sh`는 `sync-golden.sh` + strict `check-golden-sync.sh`를 연속 실행해
  TS vector 재도입 시 동기화 복구 절차를 자동화한다.

## 7. CLI Entrypoint (Cycle 9 / TASK-C1)

- compiler CLI entrypoint `ai.manifesto.compiler.CompilerCli`를 제공한다.
- 지원 서브커맨드:
  - `compile`: MEL을 컴파일해 canonical schema JSON 출력(`--out` 또는 stdout)
  - `format`: renderer 기반 deterministic MEL 포맷 출력
  - `check`: 컴파일 성공 여부 검증(`OK`/non-zero exit)
- 공통 입력 옵션:
  - `--source=...` 또는 `--classpath=...` (상호배타)
  - `--out=...`, `--indent=...`, `--newline=lf|crlf`, `--format-only`

## 8. Strict Golden Sync Lane (CI)

- 기본 `checkGoldenSync`는 소스 미존재 시 N/A 성공을 허용한다.
- CI strict lane은 아래처럼 환경변수를 강제한다.
  - `CHECK_GOLDEN_SYNC_REQUIRE_SOURCE=1 ./gradlew checkGoldenSync`
- TS core 경로를 명시할 때:
  - `TS_CORE_REPO=/path/to/manifasto-ts-core CHECK_GOLDEN_SYNC_REQUIRE_SOURCE=1 ./gradlew checkGoldenSync`

## 9. Parse/Tokens Surface Extension (Cycle 10 / TASK-F1)

- `CompilerFacade`는 compile 외에 parse/tokens 보조 API를 노출한다.
  - `tokenize(melText)` → lexer token stream + diagnostics
  - `parseSource(melText)` → parser AST + diagnostics
- `CompilerCli`는 `parse`, `tokens` 서브커맨드를 지원한다.
  - `parse`: parse summary JSON 출력(`ok`, `diagnostics`, `program` summary)
  - `tokens`: token stream JSON 출력(`ok`, `diagnostics`, `tokens`)
- 공통 입력 옵션은 `compile/format/check`와 동일하게 유지한다.
