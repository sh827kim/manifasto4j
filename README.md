# manifasto4j

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
```
https://docs.manifesto-ai.dev/
```

## Docs in this repo
- `docs/INDEX.md` (spec/fdr index)

## Modules
- `manifesto-core` — core runtime (schema/expr/flow/compute)
- `manifesto-host` — compute/effect loop
- `manifesto-app` — high-level API surface
- `manifesto-bridge` — projection/event binding
- `manifesto-compiler` — MEL compiler + lowering
- `manifesto-builder` — DSL for schema/expr/flow
- `manifesto-effect-utils` — effect handler utilities
- `manifesto-examples` — sample usage

## Build / Test
```bash
./gradlew test
```
