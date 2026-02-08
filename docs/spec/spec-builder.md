# Manifesto Java Builder SPEC (Porting Summary)

| Field | Value |
| --- | --- |
| Status | Draft (Java port) |
| Scope | type-safe domain definition DSL |
| Source of Truth | `https://github.com/manifesto-ai/core/blob/main/packages/builder/docs/VERSION-INDEX.md` |
| Latest | `1.0.0` |

## 1. Scope

Builder provides a DSL for creating DomainSchema without stringly-typed paths.

## 2. Responsibilities

- Define schema via fluent/typed API
- Validate field and action definitions
- Produce DomainSchema consistent with core spec

## 3. Notes for Java

- Java builder should emphasize immutability and compile-time safety
- Zod-based patterns from TS need Java equivalents

