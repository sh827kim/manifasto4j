# Java Porting Action Plan (2026-02-11)

## Scope
- TS reference: `/workspace/manifasto-ts-core`
- Java target: `/workspace/manifesto-java-core`

## TS Package Baseline
- Active packages in TS: `app`, `codegen`, `compiler`, `core`, `host`, `intent-ir`, `translator`, `world`
- Translator is a package family:
  - `@manifesto-ai/translator`
  - `@manifesto-ai/translator-adapter-*`
  - `@manifesto-ai/translator-target-*`

## Java Current Modules
- `manifesto-core`, `manifesto-host`, `manifesto-app`, `manifesto-world`, `manifesto-compiler`

## Java Planned Modules
- `manifesto-intent-ir`
- `manifesto-translator`
- `manifesto-codegen`

## Cleanup Status
1. Removed out-of-scope modules from the Gradle build graph.
2. Removed out-of-scope spec/fdr documents from `docs/spec` and `docs/fdr`.
3. Rewritten README and learning docs to only track active/planned parity scope.

## Must Update (P0)
1. `onceIntent` runtime path compatibility (`$mel`):
   - Java `Apply` path validation must accept platform namespace, not only `$host`.
2. Schema hash policy:
   - Align with TS semantic hash policy (`$`-prefixed fields excluded for semantic mode).
3. Golden sync default path:
   - Use `/workspace/manifasto-ts-core` as default TS repo root.

## Must Update (P1)
1. App runtime/store parity:
   - Canonicalization and platform namespace filtering in storage/delta pipeline.
2. Host runtime parity:
   - Align toward mailbox/runner/job model boundaries.
3. World parity:
   - Head query / resume / branch persistence contracts.

## New Work (P1~P2)
1. Add `manifesto-intent-ir` module skeleton.
2. Add `manifesto-translator` module skeleton.
3. Add `manifesto-codegen` module skeleton.

## Documentation Policy
1. Docs are organized by:
   - `Active parity`
   - `Planned packages`
2. Local-only working notes stay under `local-only-docs/` and remain Git-ignored.
