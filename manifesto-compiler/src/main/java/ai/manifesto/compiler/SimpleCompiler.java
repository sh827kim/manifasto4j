package ai.manifesto.compiler;

import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SimpleCompiler - 제한된 MEL-lite 컴파일러
 *
 * 지원 문법(라인 기반):
 * - schema <id> <version>
 * - field <name> <type> [required] [default=<value>]
 * - action <name> halt [input=...]
 * - computed <name> <literal>
 */
public final class SimpleCompiler implements CompilerFacade {
    private final TypeExprParser typeExprParser = new TypeExprParser();

    @Override
    public CompilationResult compileDomain(String melText) {
        if (melText == null || melText.trim().isEmpty()) {
            return CompilationResult.error("MEL input is empty");
        }

        String id = null;
        String version = null;
        List<ActionSpec> actions = new ArrayList<>();
        List<FieldSpec> dataFields = new ArrayList<>();
        List<ComputedFieldDef> computedFields = new ArrayList<>();

        String[] lines = melText.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            if (parts.length == 0) {
                continue;
            }

            switch (parts[0]) {
                case "schema" -> {
                    if (parts.length < 3) {
                        return CompilationResult.error("schema requires id and version");
                    }
                    id = parts[1];
                    version = parts[2];
                }
                case "field" -> {
                    if (parts.length < 3) {
                        return CompilationResult.error("field requires name and type");
                    }
                    String name = parts[1];
                    String type = parts[2];
                    boolean required = false;
                    Object defaultValue = null;
                    for (int i = 3; i < parts.length; i++) {
                        if ("required".equals(parts[i])) {
                            required = true;
                            continue;
                        }
                        if (parts[i].startsWith("default=")) {
                            defaultValue = parseLiteral(parts[i].substring(8));
                        }
                    }
                    FieldSpec fieldSpec = typeExprParser.parseFieldSpec(name, type, required, defaultValue);
                    dataFields.add(fieldSpec);
                }
                case "action" -> {
                    if (parts.length < 3) {
                        return CompilationResult.error("action requires name and flow");
                    }
                    String name = parts[1];
                    if (!"halt".equals(parts[2])) {
                        return CompilationResult.error("only 'halt' flow is supported in SimpleCompiler");
                    }
                    ActionSpec.Builder actionBuilder = new ActionSpec.Builder(name)
                        .flow(FlowNode.Halt.of(null));
                    for (int i = 3; i < parts.length; i++) {
                        if (parts[i].startsWith("input=")) {
                            String inputSpec = parts[i].substring(6);
                            Map<String, FieldSpec> inputs = typeExprParser.parseInputFields(inputSpec);
                            for (Map.Entry<String, FieldSpec> entry : inputs.entrySet()) {
                                actionBuilder.addInputField(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    actions.add(actionBuilder.build());
                }
                case "computed" -> {
                    if (parts.length < 3) {
                        return CompilationResult.error("computed requires name and literal");
                    }
                    String name = parts[1];
                    Object value = parseLiteral(parts[2]);
                    computedFields.add(ComputedFieldDef.simple(name, new Lit(value)));
                }
                default -> {
                    return CompilationResult.error("Unknown directive: " + parts[0]);
                }
            }
        }

        if (id == null || version == null) {
            return CompilationResult.error("schema directive is required");
        }

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        for (ComputedFieldDef field : computedFields) {
            builder.addComputedField(field);
        }

        DomainSchema temp = builder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(temp);

        DomainSchema.Builder finalBuilder = new DomainSchema.Builder(id, version).hash(hash);
        for (ActionSpec action : actions) {
            finalBuilder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            finalBuilder.addDataField(field);
        }
        for (ComputedFieldDef field : computedFields) {
            finalBuilder.addComputedField(field);
        }

        return CompilationResult.ok(finalBuilder.build());
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
}
