# manifasto4j

[Korean](README.ko.md)

Pure Java port of Manifesto's TypeScript implementation.
Goal: provide a Java-first runtime and tooling while keeping output compatibility with the TS reference.

## What is Manifesto?
Manifesto is a deterministic domain-runtime stack for stateful apps:
- **Schema** defines state, computed fields, and actions.
- **Intent** is a user/system request to execute an action.
- **Core** evaluates actions to effects/patches and produces a new snapshot.
- **Host** runs the compute/effect loop.
- **App** exposes a high-level runtime API.
- **World** enforces governance (authority, lineage, approval).
- **Compiler** provides MEL tooling.

Reference docs:
https://docs.manifesto-ai.dev/

## Current Status (2026-02-18)
- TS baseline packages: `app/codegen/compiler/core/host/intent-ir/translator/world`
- TS latest reviewed commit: `3b40070`
- TS diff `754d860..3b40070` is docs/ADR/SPEC updates (no package source behavior change)
- Java active modules: `core/host/runtime/sdk/compiler/world`
- Java planned-but-implemented baseline modules: `intent-ir/translator/codegen`

## Current Priority
1. App parity surface expansion (runtime/policy/memory/recovery contracts)
2. Host compliance hardening (error taxonomy + compliance-style tests)
3. Intent-IR / Translator / Codegen semantic depth improvements

## Modules
- `manifesto-core` - core runtime (schema/expr/flow/compute)
- `manifesto-host` - compute/effect loop
- `manifesto-sdk` - canonical public API surface
- `manifesto-runtime` - runtime app implementation layer
- `manifesto-compiler` - MEL compiler + lowering
- `manifesto-world` - world/authority/lineage runtime
- `manifesto-intent-ir` - intent IR normalization/canonical/hash and key pipeline bootstrap
- `manifesto-translator` - framework-agnostic translator contracts and baseline pipeline
- `manifesto-codegen` - code generation contracts and Java targets baseline

## Docs in this repo
- `docs/INDEX.md` - spec/fdr index
- `docs/ko/book/index.md` - Korean learning guide for Java developers
- `docs/spec/spec-*.md`, `docs/fdr/fdr-*.md` - package-level references
- `docs/migration/migrate-manifesto-app-to-sdk-runtime.md` - migration guide from retired `manifesto-app`

## Planning/Local Notes
- Planning docs, gap analysis, and local-environment notes are maintained in `local-only-docs/` (not tracked by Git).

## Build / Test
```bash
./gradlew test
```

## Useful Verification Commands
```bash
./gradlew :manifesto-core:test :manifesto-host:test :manifesto-runtime:test :manifesto-sdk:test :manifesto-compiler:test :manifesto-world:test
./gradlew :manifesto-intent-ir:test :manifesto-translator:test :manifesto-codegen:test
./gradlew checkGoldenSync
```
