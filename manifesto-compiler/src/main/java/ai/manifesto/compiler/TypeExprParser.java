package ai.manifesto.compiler;

import ai.manifesto.core.schema.FieldSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TypeExprParser - MEL-lite 타입 표현식 파서
 *
 * 지원 문법(간이):
 * - string|number|boolean|integer|object|array
 * - nullable: T? 또는 T|null
 * - enum("a","b",null)
 * - array<T>
 * - object{a:string,b?:number}
 */
final class TypeExprParser {
    FieldSpec parseFieldSpec(String name, String typeToken, boolean required, Object defaultValue) {
        TypeParseResult parsed = parseTypeExpr(typeToken);
        boolean finalRequired = required && !parsed.nullable;
        return new FieldSpec(
            name,
            parsed.type,
            finalRequired,
            defaultValue,
            parsed.fields,
            parsed.items,
            parsed.enumValues
        );
    }

    Map<String, FieldSpec> parseInputFields(String inputSpec) {
        Map<String, FieldSpec> inputs = new HashMap<>();
        if (inputSpec == null || inputSpec.isEmpty()) {
            return inputs;
        }
        for (String entry : splitTopLevel(inputSpec, ',')) {
            String trimmed = entry.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int colonIdx = trimmed.indexOf(':');
            if (colonIdx < 0) {
                continue;
            }
            String rawName = trimmed.substring(0, colonIdx).trim();
            String typeToken = trimmed.substring(colonIdx + 1).trim();
            boolean required = true;
            if (rawName.endsWith("?")) {
                required = false;
                rawName = rawName.substring(0, rawName.length() - 1);
            }
            FieldSpec spec = parseFieldSpec(rawName, typeToken, required, null);
            inputs.put(rawName, spec);
        }
        return inputs;
    }

    private TypeParseResult parseTypeExpr(String typeToken) {
        if (typeToken == null || typeToken.isEmpty()) {
            return TypeParseResult.simple("any", false);
        }

        String token = typeToken.trim();
        boolean nullable = false;
        if (token.endsWith("?")) {
            nullable = true;
            token = token.substring(0, token.length() - 1).trim();
        }

        if (token.startsWith("array<") && token.endsWith(">")) {
            String inner = token.substring(6, token.length() - 1).trim();
            TypeParseResult items = parseTypeExpr(inner);
            FieldSpec itemSpec = new FieldSpec("item", items.type, !items.nullable, null, items.fields, items.items, items.enumValues);
            return new TypeParseResult("array", nullable || items.nullable, null, itemSpec, null);
        }

        if (token.startsWith("object{") && token.endsWith("}")) {
            String inner = token.substring(7, token.length() - 1).trim();
            Map<String, FieldSpec> fields = new HashMap<>();
            if (!inner.isEmpty()) {
                for (String entry : splitTopLevel(inner, ',')) {
                    String trimmed = entry.trim();
                    if (trimmed.isEmpty()) continue;
                    int colonIdx = trimmed.indexOf(':');
                    if (colonIdx < 0) continue;
                    String rawName = trimmed.substring(0, colonIdx).trim();
                    String typeExpr = trimmed.substring(colonIdx + 1).trim();
                    boolean required = true;
                    if (rawName.endsWith("?")) {
                        required = false;
                        rawName = rawName.substring(0, rawName.length() - 1);
                    }
                    TypeParseResult fieldType = parseTypeExpr(typeExpr);
                    FieldSpec fieldSpec = new FieldSpec(
                        rawName,
                        fieldType.type,
                        required && !fieldType.nullable,
                        null,
                        fieldType.fields,
                        fieldType.items,
                        fieldType.enumValues
                    );
                    fields.put(rawName, fieldSpec);
                }
            }
            return new TypeParseResult("object", nullable, fields, null, null);
        }

        if (token.startsWith("enum(") && token.endsWith(")")) {
            String inner = token.substring(5, token.length() - 1);
            List<Object> values = new ArrayList<>();
            boolean hasNull = false;
            for (String part : splitTopLevel(inner, ',')) {
                Object value = parseLiteral(part.trim());
                if (value == null) {
                    hasNull = true;
                }
                values.add(value);
            }
            return new TypeParseResult("enum", nullable || hasNull, null, null, values);
        }

        if (token.contains("|")) {
            List<String> parts = splitTopLevel(token, '|');
            List<Object> literals = new ArrayList<>();
            List<String> simpleTypes = new ArrayList<>();
            boolean hasNull = false;
            boolean onlyLiterals = true;

            for (String part : parts) {
                String trimmed = part.trim();
                if (trimmed.isEmpty()) continue;
                if ("null".equals(trimmed)) {
                    hasNull = true;
                    literals.add(null);
                    continue;
                }
                if (isLiteralToken(trimmed)) {
                    literals.add(parseLiteral(trimmed));
                    continue;
                }
                onlyLiterals = false;
                simpleTypes.add(trimmed);
            }

            if (onlyLiterals && !literals.isEmpty()) {
                return new TypeParseResult("enum", nullable || hasNull, null, null, literals);
            }

            if (simpleTypes.size() == 1) {
                return new TypeParseResult(simpleTypes.get(0), nullable || hasNull, null, null, null);
            }

            return new TypeParseResult("any", nullable || hasNull, null, null, null);
        }

        return new TypeParseResult(token, nullable, null, null, null);
    }

    private Object parseLiteral(String token) {
        if (token == null) {
            return null;
        }
        if ("null".equalsIgnoreCase(token)) {
            return null;
        }
        if (token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }
        if ("true".equalsIgnoreCase(token)) {
            return true;
        }
        if ("false".equalsIgnoreCase(token)) {
            return false;
        }
        try {
            if (token.contains(".")) {
                return Double.parseDouble(token);
            }
            return Integer.parseInt(token);
        } catch (NumberFormatException e) {
            return token;
        }
    }

    private boolean isLiteralToken(String token) {
        if (token == null) return false;
        if ("true".equalsIgnoreCase(token) || "false".equalsIgnoreCase(token)) {
            return true;
        }
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
            return true;
        }
        if ("null".equalsIgnoreCase(token)) {
            return true;
        }
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<String> splitTopLevel(String input, char delimiter) {
        List<String> parts = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return parts;
        }
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuote = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            }
            if (!inQuote) {
                if (c == '<' || c == '{' || c == '(') {
                    depth++;
                } else if (c == '>' || c == '}' || c == ')') {
                    depth = Math.max(0, depth - 1);
                }
            }
            if (c == delimiter && depth == 0 && !inQuote) {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        parts.add(current.toString());
        return parts;
    }

    private static final class TypeParseResult {
        private final String type;
        private final boolean nullable;
        private final Map<String, FieldSpec> fields;
        private final FieldSpec items;
        private final List<Object> enumValues;

        private TypeParseResult(String type, boolean nullable, Map<String, FieldSpec> fields,
                                FieldSpec items, List<Object> enumValues) {
            this.type = type;
            this.nullable = nullable;
            this.fields = fields;
            this.items = items;
            this.enumValues = enumValues;
        }

        private static TypeParseResult simple(String type, boolean nullable) {
            return new TypeParseResult(type, nullable, null, null, null);
        }
    }
}
