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

## Current Status (2026-02-14)
- TS baseline packages: `app/codegen/compiler/core/host/intent-ir/translator/world`
- TS latest reviewed commit: `3b40070`
- TS diff `754d860..3b40070` is docs/ADR/SPEC updates (no package source behavior change)
- Java active modules: `core/host/app/compiler/world`
- Java planned-but-implemented baseline modules: `intent-ir/translator/codegen`

## Current Priority
1. App ActionHandle lifecycle parity (phase/update history)
2. Intent-IR key derivation + lexicon/resolver minimum core
3. Translator pipeline/plugin architecture hardening

## Modules
- `manifesto-core` - core runtime (schema/expr/flow/compute)
- `manifesto-host` - compute/effect loop
- `manifesto-app` - high-level API surface
- `manifesto-compiler` - MEL compiler + lowering
- `manifesto-world` - world/authority/lineage runtime
- `manifesto-intent-ir` - intent IR normalization/canonical/hash and key pipeline bootstrap
- `manifesto-translator` - framework-agnostic translator contracts and baseline pipeline
- `manifesto-codegen` - code generation contracts and Java targets baseline

## Docs in this repo
- `/workspace/manifesto-java-core/docs/INDEX.md` - spec/fdr index
- `/workspace/manifesto-java-core/docs/MASTER_COMPLETION_PLAN_2026-02-14.md` - master execution roadmap
- `/workspace/manifesto-java-core/docs/TS_PARITY_PROGRESS_REPORT_2026-02-14.md` - TS shape-based progress report
- `/workspace/manifesto-java-core/docs/ko/book/index.md` - Korean learning guide for Java developers
- `/workspace/manifesto-java-core/docs/spec/spec-*.md`, `/workspace/manifesto-java-core/docs/fdr/fdr-*.md` - package-level references

## Local-only Notes
- `/workspace/manifesto-java-core/local-only-docs/` is intentionally ignored by Git and is for local workspace notes only.

## Build / Test
```bash
./gradlew test
```

## Useful Verification Commands
```bash
./gradlew :manifesto-core:test :manifesto-host:test :manifesto-app:test :manifesto-compiler:test :manifesto-world:test
./gradlew :manifesto-intent-ir:test :manifesto-translator:test :manifesto-codegen:test
./gradlew checkGoldenSync
```
