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

