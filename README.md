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

## Current Status (2026-02-11)
- TS baseline packages are `app/codegen/compiler/core/host/intent-ir/translator/world`.
- Java implemented modules are `core/host/app/compiler/world`.
- Java next modules are `intent-ir/translator/codegen`.
- Documentation and build graph were cleaned to remove out-of-scope packages.

## Modules
- `manifesto-core` - core runtime (schema/expr/flow/compute)
- `manifesto-host` - compute/effect loop
- `manifesto-app` - high-level API surface
- `manifesto-compiler` - MEL compiler + lowering
- `manifesto-world` - world/authority/lineage runtime

## Planned Modules
- `manifesto-intent-ir` (planned)
- `manifesto-translator` (planned)
- `manifesto-codegen` (planned)

## Docs in this repo
- `docs/INDEX.md` - spec/fdr index
- `docs/PORTING_ACTION_PLAN_2026-02-11.md` - current gap analysis and action items
- `docs/ko/book/index.md` - Korean learning guide for Java developers
- `docs/spec/spec-*.md`, `docs/fdr/fdr-*.md` - package-level references

## Local-only Notes
- `local-only-docs/` is intentionally ignored by Git and is for local workspace notes only.

## Build / Test
```bash
./gradlew test
```

## Useful Verification Commands
```bash
./gradlew :manifesto-core:test :manifesto-host:test :manifesto-app:test :manifesto-compiler:test :manifesto-world:test
./gradlew checkGoldenSync
```
