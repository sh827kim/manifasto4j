# Manifesto Java App SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | app facade for server/CLI |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/app/docs/VERSION-INDEX.md` |
| Latest | `2.3.1` |

## 1. Scope

App is a facade over Core/Host/World/Store. Java App should provide:

- createApp(domain, opts)
- app.ready() explicit initialization
- act(intent) with ActionHandle lifecycle
- subscribe(state selector)
- service registration (effect handlers)

## 2. Server/CLI-Friendly Minimal App

Minimal conformance for server/CLI:

- Single runtime (domain runtime only)
- Basic ActionHandle: status/result/trace
- Services map for effect execution
- Optional external store integration (disabled by default)

## 3. Required Behaviors

- MUST not modify core semantics
- MUST keep compute-effect loop deterministic
- MUST use explicit initialization (ready)
- MUST expose action execution as observable handle

## 4. Optional Features (defer)

- Branch management
- Session management
- Hook/plugin system
- System actions catalog
