package ai.manifesto.codegen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: DomainSchema 유사 map에서 state 필드를 읽어 Java DTO 소스 코드를 생성하는 기본 생성기입니다.
 * EN: Default generator that reads state fields from a DomainSchema-like map and emits Java DTO source code.
 */
public final class JavaDtoCodeGenerator implements CodeGenerator {
    private static final Set<String> JAVA_KEYWORDS = Set.of(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "record", "sealed"
    );

    @Override
    public List<GeneratedArtifact> generate(CodegenRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(request.target(), "request.target must not be null");
        if (!"java-dto".equalsIgnoreCase(request.target().name())) {
            throw new IllegalArgumentException("Unsupported target: " + request.target().name());
        }
        if (request.basePackage() == null || request.basePackage().isBlank()) {
            throw new IllegalArgumentException("basePackage must not be blank");
        }

        Map<String, Object> schema = Objects.requireNonNull(request.schema(), "schema must not be null");
        Map<String, Object> state = requireMap(schema.get("state"), "schema.state must be an object");
        Map<String, Object> fields = requireMap(state.get("fields"), "schema.state.fields must be an object");

        String packageName = request.basePackage().trim();
        String pathPrefix = packageName.replace('.', '/');
        String content = buildStateDtoSource(packageName, fields);

        return List.of(new GeneratedArtifact(pathPrefix + "/StateDto.java", content));
    }

    private String buildStateDtoSource(String packageName, Map<String, Object> fields) {
        List<FieldDef> fieldDefs = fields.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> toFieldDef(entry.getKey(), entry.getValue()))
            .toList();

        boolean needsList = fieldDefs.stream().anyMatch(def -> def.javaType.contains("List<"));
        boolean needsMap = fieldDefs.stream().anyMatch(def -> def.javaType.contains("Map<"));
        boolean needsObjects = !fieldDefs.isEmpty();

        StringBuilder source = new StringBuilder();
        source.append("package ").append(packageName).append(";\n\n");
        if (needsList) {
            source.append("import java.util.List;\n");
        }
        if (needsMap) {
            source.append("import java.util.Map;\n");
        }
        if (needsObjects) {
            source.append("import java.util.Objects;\n");
        }
        if (needsList || needsMap || needsObjects) {
            source.append("\n");
        }

        source.append("/**\n");
        source.append(" * KR: DomainSchema state를 Java 객체로 다루기 위한 기본 DTO입니다.\n");
        source.append(" * EN: Basic DTO for handling DomainSchema state as a Java object.\n");
        source.append(" */\n");
        source.append("public final class StateDto {\n");

        for (FieldDef fieldDef : fieldDefs) {
            source.append("    private ").append(fieldDef.javaType).append(" ").append(fieldDef.fieldName).append(";\n");
        }
        if (!fieldDefs.isEmpty()) {
            source.append("\n");
        }

        source.append("    public StateDto() {\n");
        source.append("    }\n");

        for (FieldDef fieldDef : fieldDefs) {
            String methodSuffix = upperCamel(fieldDef.fieldName);
            source.append("\n");
            source.append("    public ").append(fieldDef.javaType).append(" get").append(methodSuffix).append("() {\n");
            source.append("        return ").append(fieldDef.fieldName).append(";\n");
            source.append("    }\n");
            source.append("\n");
            source.append("    public void set").append(methodSuffix).append("(")
                .append(fieldDef.javaType).append(" ").append(fieldDef.fieldName).append(") {\n");
            if (fieldDef.nonNull) {
                source.append("        this.").append(fieldDef.fieldName).append(" = Objects.requireNonNull(")
                    .append(fieldDef.fieldName).append(", \"").append(fieldDef.fieldName).append(" must not be null\");\n");
            } else {
                source.append("        this.").append(fieldDef.fieldName).append(" = ").append(fieldDef.fieldName).append(";\n");
            }
            source.append("    }\n");
        }

        source.append("}\n");
        return source.toString();
    }

    private FieldDef toFieldDef(String originalName, Object fieldSpecValue) {
        Map<String, Object> fieldSpec = requireMap(fieldSpecValue, "field spec must be an object: " + originalName);
        String javaType = resolveJavaType(fieldSpec.get("type"));
        boolean required = Boolean.TRUE.equals(fieldSpec.get("required"));
        String fieldName = sanitizeIdentifier(lowerCamel(originalName));
        return new FieldDef(originalName, fieldName, javaType, required && !isContainerType(javaType));
    }

    private String resolveJavaType(Object typeValue) {
        if (typeValue instanceof String typeName) {
            return switch (typeName) {
                case "string" -> "String";
                case "number" -> "Double";
                case "integer" -> "Integer";
                case "boolean" -> "Boolean";
                case "array" -> "List<Object>";
                case "object" -> "Map<String, Object>";
                default -> "Object";
            };
        }
        if (typeValue instanceof Map<?, ?> map) {
            if (map.containsKey("enum")) {
                return "String";
            }
        }
        return "Object";
    }

    private boolean isContainerType(String javaType) {
        return javaType.startsWith("List<") || javaType.startsWith("Map<") || "Object".equals(javaType);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> requireMap(Object value, String message) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(message);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            out.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return out;
    }

    private String lowerCamel(String value) {
        if (value == null || value.isBlank()) {
            return "field";
        }
        String[] parts = value.split("[^A-Za-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                tokens.add(part);
            }
        }
        if (tokens.isEmpty()) {
            return "field";
        }
        StringBuilder out = new StringBuilder(tokens.get(0).substring(0, 1).toLowerCase(Locale.ROOT));
        out.append(tokens.get(0).substring(1));
        for (int i = 1; i < tokens.size(); i++) {
            out.append(upperCamel(tokens.get(i)));
        }
        return out.toString();
    }

    private String upperCamel(String value) {
        if (value == null || value.isBlank()) {
            return "Field";
        }
        String normalized = value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
        return normalized;
    }

    private String sanitizeIdentifier(String candidate) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < candidate.length(); i++) {
            char c = candidate.charAt(i);
            if (i == 0) {
                out.append(Character.isJavaIdentifierStart(c) ? c : '_');
            } else {
                out.append(Character.isJavaIdentifierPart(c) ? c : '_');
            }
        }
        String sanitized = out.length() == 0 ? "field" : out.toString();
        if (JAVA_KEYWORDS.contains(sanitized)) {
            return sanitized + "_";
        }
        return sanitized;
    }

    private record FieldDef(String originalName, String fieldName, String javaType, boolean nonNull) {
    }
}
