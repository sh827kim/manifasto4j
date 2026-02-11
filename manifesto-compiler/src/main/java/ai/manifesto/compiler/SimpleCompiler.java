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
 * KR: SimpleCompiler는 MEL 소스에서 Core 실행에 필요한 중간 표현을 생성하는 컴파일러 구현입니다.
 * EN: SimpleCompiler compiles MEL source into intermediate representation required for Core execution.
 */
public final class SimpleCompiler implements CompilerFacade {
    private final TypeExprParser typeExprParser = new TypeExprParser();

    @Override
    public CompilationResult compileDomain(String melText) {
        return compileDomain(melText, null);
    }

    @Override
    public CompilationResult compileDomain(String melText, CompileDomainOptions options) {
        if (melText == null || melText.trim().isEmpty()) {
            return CompilationResult.error("MEL input is empty");
        }

        List<CompileTrace> trace = new ArrayList<>();
        long parseStart = System.nanoTime();
        String fnTableVersion = options == null ? null : options.fnTableVersion();

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
                        trace.add(traceEntry("parse", parseStart, fnTableVersion));
                        return CompilationResult.error("schema requires id and version", List.of(), trace);
                    }
                    id = parts[1];
                    version = parts[2];
                }
                case "field" -> {
                    if (parts.length < 3) {
                        trace.add(traceEntry("parse", parseStart, fnTableVersion));
                        return CompilationResult.error("field requires name and type", List.of(), trace);
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
                        trace.add(traceEntry("parse", parseStart, fnTableVersion));
                        return CompilationResult.error("action requires name and flow", List.of(), trace);
                    }
                    String name = parts[1];
                    if (!"halt".equals(parts[2])) {
                        trace.add(traceEntry("parse", parseStart, fnTableVersion));
                        return CompilationResult.error("only 'halt' flow is supported in SimpleCompiler", List.of(), trace);
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
                        trace.add(traceEntry("parse", parseStart, fnTableVersion));
                        return CompilationResult.error("computed requires name and literal", List.of(), trace);
                    }
                    String name = parts[1];
                    Object value = parseLiteral(parts[2]);
                    computedFields.add(ComputedFieldDef.simple(name, new Lit(value)));
                }
                default -> {
                    trace.add(traceEntry("parse", parseStart, fnTableVersion));
                    return CompilationResult.error("Unknown directive: " + parts[0], List.of(), trace);
                }
            }
        }

        if (id == null || version == null) {
            trace.add(traceEntry("parse", parseStart, fnTableVersion));
            return CompilationResult.error("schema directive is required", List.of(), trace);
        }

        trace.add(traceEntry("parse", parseStart, fnTableVersion));
        long genStart = System.nanoTime();

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

        trace.add(traceEntry("generate", genStart, fnTableVersion));
        return CompilationResult.ok(finalBuilder.build(), List.of(), trace);
    }

    private long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private CompileTrace traceEntry(String phase, long startNanos, String fnTableVersion) {
        long duration = elapsedMs(startNanos);
        if (fnTableVersion == null) {
            return CompileTrace.of(phase, duration);
        }
        return CompileTrace.of(phase, duration, Map.of("fnTableVersion", fnTableVersion));
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
