package ai.manifesto.codegen;

import ai.manifesto.codegen.runtime.CodegenPluginOptions;
import ai.manifesto.codegen.runtime.NamingConvention;
import ai.manifesto.codegen.runtime.NullabilityMode;
import ai.manifesto.codegen.runtime.StyleProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * KR: DomainSchema 유사 map에서 action 정보를 읽어 Java typed client 인터페이스/입력 DTO를 생성합니다.
 * EN: Generates Java typed-client interface and per-action input DTOs from a DomainSchema-like action map.
 */
public final class JavaTypedClientCodeGenerator implements CodegenPlugin {
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
        return generate(request, CodegenPluginOptions.defaults());
    }

    @Override
    public List<GeneratedArtifact> generate(CodegenRequest request, CodegenPluginOptions options) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(options, "options must not be null");
        Objects.requireNonNull(request.target(), "request.target must not be null");
        if (!supports(request.target())) {
            throw new IllegalArgumentException("Unsupported target: " + request.target().name());
        }
        if (request.basePackage() == null || request.basePackage().isBlank()) {
            throw new IllegalArgumentException("basePackage must not be blank");
        }

        Map<String, Object> schema = requireMap(request.schema(), "schema must not be null");
        Map<String, Object> actions = requireMap(schema.get("actions"), "schema.actions must be an object");
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("schema.actions must not be empty");
        }

        String packageName = request.basePackage().trim();
        String domainName = inferDomainName(schema.get("id"));
        String pathPrefix = packageName.replace('.', '/');

        List<ActionDef> actionDefs = actions.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> toActionDef(entry.getKey(), entry.getValue(), options))
            .toList();

        List<GeneratedArtifact> artifacts = new ArrayList<>();
        artifacts.add(new GeneratedArtifact(
            pathPrefix + "/" + domainName + "Client.java",
            buildClientSource(packageName, domainName, actionDefs)
        ));
        for (ActionDef action : actionDefs) {
            artifacts.add(new GeneratedArtifact(
                pathPrefix + "/" + action.inputTypeName + ".java",
                buildActionInputSource(packageName, action, options)
            ));
        }
        return List.copyOf(artifacts);
    }

    @Override
    public String pluginId() {
        return "java-typed-client";
    }

    @Override
    public boolean supports(CodegenTarget target) {
        return target != null && "java-typed-client".equalsIgnoreCase(target.name());
    }

    private String buildClientSource(String packageName, String domainName, List<ActionDef> actions) {
        StringBuilder source = new StringBuilder();
        source.append("package ").append(packageName).append(";\n\n");
        source.append("/**\n");
        source.append(" * KR: ").append(domainName).append(" 도메인 action 호출을 위한 typed client 인터페이스입니다.\n");
        source.append(" * EN: Typed client interface for invoking actions in ").append(domainName).append(" domain.\n");
        source.append(" */\n");
        source.append("public interface ").append(domainName).append("Client {\n");
        for (ActionDef action : actions) {
            source.append("\n");
            source.append("    void ").append(action.methodName).append("(").append(action.inputTypeName).append(" input);\n");
        }
        source.append("}\n");
        return source.toString();
    }

    private String buildActionInputSource(String packageName, ActionDef action, CodegenPluginOptions options) {
        boolean needsList = action.fields.stream().anyMatch(field -> field.javaType.contains("List<"));
        boolean needsMap = action.fields.stream().anyMatch(field -> field.javaType.contains("Map<"));
        boolean needsObjects = options.nullability() == NullabilityMode.STRICT
            && action.fields.stream().anyMatch(FieldDef::nonNull);
        String indent = options.style() == StyleProfile.COMPACT ? "  " : "    ";
        String lineBreak = options.style() == StyleProfile.COMPACT ? "\n" : "\n\n";

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

        if (options.style() != StyleProfile.COMPACT) {
            source.append("/**\n");
            source.append(" * KR: action `").append(action.originalActionName).append("` 호출 입력 DTO입니다.\n");
            source.append(" * EN: Input DTO for action `").append(action.originalActionName).append("` invocation.\n");
            source.append(" */\n");
        }
        source.append("public final class ").append(action.inputTypeName).append(" {\n");
        for (FieldDef field : action.fields) {
            source.append(indent).append("private ").append(field.javaType).append(" ").append(field.fieldName).append(";\n");
        }
        if (!action.fields.isEmpty()) {
            source.append(lineBreak);
        }
        source.append(indent).append("public ").append(action.inputTypeName).append("() {\n");
        source.append(indent).append("}\n");

        for (FieldDef field : action.fields) {
            String suffix = upperCamel(field.fieldName);
            source.append(lineBreak);
            source.append(indent).append("public ").append(field.javaType).append(" get").append(suffix).append("() {\n");
            source.append(indent).append(indent).append("return ").append(field.fieldName).append(";\n");
            source.append(indent).append("}\n");
            source.append(lineBreak);
            source.append(indent).append("public void set").append(suffix).append("(").append(field.javaType).append(" ").append(field.fieldName).append(") {\n");
            if (field.nonNull) {
                source.append(indent).append(indent).append("this.").append(field.fieldName).append(" = Objects.requireNonNull(")
                    .append(field.fieldName).append(", \"").append(field.fieldName).append(" must not be null\");\n");
            } else {
                source.append(indent).append(indent).append("this.").append(field.fieldName).append(" = ").append(field.fieldName).append(";\n");
            }
            source.append(indent).append("}\n");
        }
        source.append("}\n");
        return source.toString();
    }

    private ActionDef toActionDef(String actionName, Object actionSpecValue, CodegenPluginOptions options) {
        Map<String, Object> actionSpec = requireMap(actionSpecValue, "action spec must be an object: " + actionName);
        List<FieldDef> fields = parseActionFields(actionSpec, options);

        String methodName = sanitizeIdentifier(applyNaming(actionName, options.naming()));
        String inputTypeName = upperCamel(methodName) + "Input";
        return new ActionDef(actionName, methodName, inputTypeName, fields);
    }

    private List<FieldDef> parseActionFields(Map<String, Object> actionSpec, CodegenPluginOptions options) {
        Map<String, Object> input = optionalMap(actionSpec.get("input"));
        if (input == null) {
            return List.of();
        }
        Map<String, Object> fields = optionalMap(input.get("fields"));
        if (fields == null || fields.isEmpty()) {
            return List.of();
        }
        return fields.entrySet().stream()
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .map(entry -> toFieldDef(entry.getKey(), entry.getValue(), options))
            .toList();
    }

    private FieldDef toFieldDef(String originalName, Object fieldSpecValue, CodegenPluginOptions options) {
        Map<String, Object> fieldSpec = requireMap(fieldSpecValue, "field spec must be an object: " + originalName);
        String javaType = resolveJavaType(fieldSpec.get("type"));
        boolean required = Boolean.TRUE.equals(fieldSpec.get("required"));
        String fieldName = sanitizeIdentifier(applyNaming(originalName, options.naming()));
        boolean nonNull = options.nullability() == NullabilityMode.STRICT && required && !isContainerType(javaType);
        return new FieldDef(fieldName, javaType, nonNull);
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
        if (typeValue instanceof Map<?, ?> map && map.containsKey("enum")) {
            return "String";
        }
        return "Object";
    }

    private boolean isContainerType(String javaType) {
        return javaType.startsWith("List<") || javaType.startsWith("Map<") || "Object".equals(javaType);
    }

    private String inferDomainName(Object idValue) {
        String id = idValue == null ? "Domain" : String.valueOf(idValue);
        String[] parts = id.split("[:/.-]+");
        String candidate = parts.length == 0 ? "Domain" : parts[parts.length - 1];
        if (candidate.isBlank()) {
            return "Domain";
        }
        return upperCamel(sanitizeIdentifier(candidate));
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
        String first = tokens.get(0);
        StringBuilder out = new StringBuilder(first.substring(0, 1).toLowerCase(Locale.ROOT));
        if (first.length() > 1) {
            out.append(first.substring(1));
        }
        for (int i = 1; i < tokens.size(); i++) {
            out.append(upperCamel(tokens.get(i)));
        }
        return out.toString();
    }

    private String upperCamel(String value) {
        if (value == null || value.isBlank()) {
            return "Field";
        }
        String sanitized = sanitizeIdentifier(value);
        return sanitized.substring(0, 1).toUpperCase(Locale.ROOT) + sanitized.substring(1);
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

    private String applyNaming(String originalName, NamingConvention namingConvention) {
        return switch (namingConvention) {
            case CAMEL_CASE -> lowerCamel(originalName);
            case PASCAL_CASE -> upperCamel(lowerCamel(originalName));
            case SNAKE_CASE -> toSnakeCase(originalName);
        };
    }

    private String toSnakeCase(String value) {
        String camel = lowerCamel(value);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                out.append('_').append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

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

    private Map<String, Object> optionalMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            out.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return out;
    }

    private record ActionDef(
        String originalActionName,
        String methodName,
        String inputTypeName,
        List<FieldDef> fields
    ) {
    }

    private record FieldDef(String fieldName, String javaType, boolean nonNull) {
    }
}
