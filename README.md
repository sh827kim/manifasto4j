# manifasto4j

[Korean](README.ko.md)

Pure Java port of Manifesto’s TypeScript implementation.
Goal: provide a Java-first runtime and tooling while keeping output compatibility with the TS reference.

## What is Manifesto?
Manifesto is a deterministic domain-runtime stack for stateful apps:
- **Schema** defines state, computed fields, and actions.
- **Intent** is a user/system request to execute an action.
- **Core** evaluates actions → effects/patches → new snapshot.
- **Host** runs the compute/effect loop.
- **App/Bridge** expose higher-level APIs and projection/event binding.
- **Compiler/Builder** provide MEL/DSL tooling.

Reference docs:
https://docs.manifesto-ai.dev/

## Current Status (2026-02-08)
- Multi-module Java port is active for `core/host/app/bridge/compiler/builder/effect-utils/world`.
- `world` module is implemented and covered with edge-case tests.
- `bridge` now supports routed projections and `ProjectionResult` (`intent` or `none(reason)`).
- `host` includes retry/timeout options and `$host` error recording paths.
- `compiler` includes strict runtime-patch APIs and golden/vector sync checks.

## Modules
- `manifesto-core` — core runtime (schema/expr/flow/compute)
- `manifesto-host` — compute/effect loop
- `manifesto-app` — high-level API surface
- `manifesto-bridge` — projection/event binding
- `manifesto-compiler` — MEL compiler + lowering
- `manifesto-builder` — DSL for schema/expr/flow
- `manifesto-effect-utils` — effect handler utilities
- `manifesto-world` — world/authority/lineage/governance runtime
- `manifesto-examples` — sample usage

## Docs in this repo
- `docs/INDEX.md` — spec/fdr index
- `docs/ko/book/index.md` — Korean learning guide for Java developers
- `docs/spec/spec-*.md`, `docs/fdr/fdr-*.md` — package-level references

## Local-only Notes
- `local-only-docs/` is intentionally ignored by Git and is for local workspace notes only.

## Build / Test
```bash
./gradlew test
```

## Useful Verification Commands
```bash
./gradlew :manifesto-bridge:test :manifesto-app:test
./gradlew :manifesto-world:test
./gradlew :manifesto-compiler:test
./gradlew checkGoldenSync
```
