package ai.manifesto.compiler;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.DiagnosticSeverity;
import ai.manifesto.compiler.lexer.SourceLocation;
import ai.manifesto.compiler.parser.ActionNode;
import ai.manifesto.compiler.parser.ArrayLiteralExprNode;
import ai.manifesto.compiler.parser.ArrayTypeNode;
import ai.manifesto.compiler.parser.AstNode;
import ai.manifesto.compiler.parser.BinaryExprNode;
import ai.manifesto.compiler.parser.ComputedNode;
import ai.manifesto.compiler.parser.DomainMember;
import ai.manifesto.compiler.parser.DomainNode;
import ai.manifesto.compiler.parser.EffectArgNode;
import ai.manifesto.compiler.parser.EffectStmtNode;
import ai.manifesto.compiler.parser.FailStmtNode;
import ai.manifesto.compiler.parser.FunctionCallExprNode;
import ai.manifesto.compiler.parser.IdentifierExprNode;
import ai.manifesto.compiler.parser.IndexAccessExprNode;
import ai.manifesto.compiler.parser.IndexSegmentNode;
import ai.manifesto.compiler.parser.InnerStmtNode;
import ai.manifesto.compiler.parser.IterationVarExprNode;
import ai.manifesto.compiler.parser.LiteralExprNode;
import ai.manifesto.compiler.parser.LiteralTypeNode;
import ai.manifesto.compiler.parser.ObjectLiteralExprNode;
import ai.manifesto.compiler.parser.ObjectPropertyNode;
import ai.manifesto.compiler.parser.ObjectTypeNode;
import ai.manifesto.compiler.parser.OnceIntentStmtNode;
import ai.manifesto.compiler.parser.OnceStmtNode;
import ai.manifesto.compiler.parser.ParamNode;
import ai.manifesto.compiler.parser.PatchStmtNode;
import ai.manifesto.compiler.parser.PathNode;
import ai.manifesto.compiler.parser.PathSegmentNode;
import ai.manifesto.compiler.parser.ProgramNode;
import ai.manifesto.compiler.parser.PropertyAccessExprNode;
import ai.manifesto.compiler.parser.PropertySegmentNode;
import ai.manifesto.compiler.parser.RecordTypeNode;
import ai.manifesto.compiler.parser.SimpleTypeNode;
import ai.manifesto.compiler.parser.StateFieldNode;
import ai.manifesto.compiler.parser.StateNode;
import ai.manifesto.compiler.parser.StopStmtNode;
import ai.manifesto.compiler.parser.SystemIdentExprNode;
import ai.manifesto.compiler.parser.TernaryExprNode;
import ai.manifesto.compiler.parser.TypeDeclNode;
import ai.manifesto.compiler.parser.TypeExprNode;
import ai.manifesto.compiler.parser.TypeFieldNode;
import ai.manifesto.compiler.parser.UnaryExprNode;
import ai.manifesto.compiler.parser.UnionTypeNode;
import ai.manifesto.compiler.parser.WhenStmtNode;
import ai.manifesto.core.core.ValidationUtils;
import ai.manifesto.core.expr.ExprNode;
import ai.manifesto.core.expr.arithmetic.*;
import ai.manifesto.core.expr.collection.Append;
import ai.manifesto.core.expr.collection.At;
import ai.manifesto.core.expr.collection.Every;
import ai.manifesto.core.expr.collection.Filter;
import ai.manifesto.core.expr.collection.Find;
import ai.manifesto.core.expr.collection.First;
import ai.manifesto.core.expr.collection.Includes;
import ai.manifesto.core.expr.collection.Last;
import ai.manifesto.core.expr.collection.Len;
import ai.manifesto.core.expr.collection.Slice;
import ai.manifesto.core.expr.collection.Some;
import ai.manifesto.core.expr.comparison.*;
import ai.manifesto.core.expr.conditional.If;
import ai.manifesto.core.expr.literal.Get;
import ai.manifesto.core.expr.literal.Lit;
import ai.manifesto.core.expr.logical.And;
import ai.manifesto.core.expr.logical.Not;
import ai.manifesto.core.expr.logical.Or;
import ai.manifesto.core.expr.object.Entries;
import ai.manifesto.core.expr.object.Keys;
import ai.manifesto.core.expr.object.Merge;
import ai.manifesto.core.expr.object.ObjectExpr;
import ai.manifesto.core.expr.object.Values;
import ai.manifesto.core.expr.string.Concat;
import ai.manifesto.core.expr.string.StrLen;
import ai.manifesto.core.expr.string.Substring;
import ai.manifesto.core.expr.string.ToString;
import ai.manifesto.core.expr.string.Trim;
import ai.manifesto.core.expr.string.ToLowerCase;
import ai.manifesto.core.expr.string.ToUpperCase;
import ai.manifesto.core.expr.type.Coalesce;
import ai.manifesto.core.expr.type.IsNull;
import ai.manifesto.core.expr.type.Typeof;
import ai.manifesto.core.flow.FlowNode;
import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainMeta;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;
import ai.manifesto.core.schema.TypeSpec;
import ai.manifesto.core.utils.HashUtils;

import java.util.*;

/**
 * KR: AstIrGenerator는 컴파일러 모듈에서 ast ir generator 역할을 수행하는 구현 타입입니다.
 * EN: AstIrGenerator is an implementation type performing ast ir generator roles in the compiler module.
 */
public final class AstIrGenerator {

    public GenerateResult generate(ProgramNode program) {
        GeneratorContext ctx = new GeneratorContext();
        if (program == null) {
            return new GenerateResult(null, List.of());
        }

        collectFieldNames(program.domain(), ctx);

        Map<String, TypeSpec> types = generateTypes(program.domain(), ctx);
        Map<String, FieldSpec> dataFields = generateState(program.domain(), ctx);
        Map<String, ComputedFieldDef> computedFields = generateComputed(program.domain(), ctx);
        Map<String, ActionSpec> actions = generateActions(program.domain(), ctx);

        if (ctx.hasErrors()) {
            return new GenerateResult(null, List.copyOf(ctx.diagnostics));
        }

        String id = "mel:" + program.domain().name().toLowerCase(Locale.ROOT);
        String version = "1.0.0";

        DomainSchema.Builder builder = new DomainSchema.Builder(id, version);
        builder.meta(new DomainMeta(program.domain().name(), null, null));
        builder.types(types);
        actions.values().forEach(builder::addAction);
        dataFields.values().forEach(builder::addDataField);
        computedFields.values().forEach(builder::addComputedField);

        DomainSchema temp = builder.hash("").build();
        String hash = ValidationUtils.computeSchemaHash(temp);

        DomainSchema.Builder finalBuilder = new DomainSchema.Builder(id, version).hash(hash);
        finalBuilder.meta(new DomainMeta(program.domain().name(), null, null));
        finalBuilder.types(types);
        actions.values().forEach(finalBuilder::addAction);
        dataFields.values().forEach(finalBuilder::addDataField);
        computedFields.values().forEach(finalBuilder::addComputedField);

        return new GenerateResult(finalBuilder.build(), List.copyOf(ctx.diagnostics));
    }

    private void collectFieldNames(DomainNode domain, GeneratorContext ctx) {
        for (TypeDeclNode typeDecl : domain.types()) {
            ctx.typeDefs.put(typeDecl.name(), typeDecl);
        }
        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                for (StateFieldNode field : state.fields()) {
                    ctx.stateFields.add(field.name());
                }
            } else if (member instanceof ComputedNode computed) {
                ctx.computedFields.add(computed.name());
            }
        }
    }

    private Map<String, TypeSpec> generateTypes(DomainNode domain, GeneratorContext ctx) {
        Map<String, TypeSpec> types = new LinkedHashMap<>();
        for (TypeDeclNode typeDecl : domain.types()) {
            Map<String, Object> definition = typeExprToDefinition(typeDecl.typeExpr(), ctx);
            types.put(typeDecl.name(), new TypeSpec(typeDecl.name(), definition));
        }
        return types;
    }

    private Map<String, Object> typeExprToDefinition(TypeExprNode typeExpr, GeneratorContext ctx) {
        if (typeExpr instanceof SimpleTypeNode simple) {
            if ("string".equals(simple.name()) || "number".equals(simple.name())
                || "boolean".equals(simple.name()) || "null".equals(simple.name())) {
                return mapOf("kind", "primitive", "type", simple.name());
            }
            return mapOf("kind", "ref", "name", simple.name());
        }
        if (typeExpr instanceof ArrayTypeNode array) {
            return mapOf("kind", "array", "element", typeExprToDefinition(array.elementType(), ctx));
        }
        if (typeExpr instanceof RecordTypeNode record) {
            return mapOf(
                "kind", "record",
                "key", typeExprToDefinition(record.keyType(), ctx),
                "value", typeExprToDefinition(record.valueType(), ctx)
            );
        }
        if (typeExpr instanceof ObjectTypeNode objectType) {
            Map<String, Object> fields = new LinkedHashMap<>();
            for (TypeFieldNode field : objectType.fields()) {
                Map<String, Object> fieldDef = new LinkedHashMap<>();
                fieldDef.put("type", typeExprToDefinition(field.typeExpr(), ctx));
                fieldDef.put("optional", field.optional());
                fields.put(field.name(), fieldDef);
            }
            return mapOf("kind", "object", "fields", fields);
        }
        if (typeExpr instanceof UnionTypeNode union) {
            List<Map<String, Object>> types = new ArrayList<>();
            for (TypeExprNode t : union.types()) {
                types.add(typeExprToDefinition(t, ctx));
            }
            return mapOf("kind", "union", "types", types);
        }
        if (typeExpr instanceof LiteralTypeNode literal) {
            return mapOf("kind", "literal", "value", literal.value());
        }
        return mapOf("kind", "primitive", "type", "null");
    }

    private Map<String, FieldSpec> generateState(DomainNode domain, GeneratorContext ctx) {
        Map<String, FieldSpec> fields = new LinkedHashMap<>();
        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                for (StateFieldNode field : state.fields()) {
                    FieldSpecData spec = typeExprToFieldSpec(field.typeExpr(), ctx);
                    Object defaultValue = field.initializer() != null
                        ? evaluateInitializer(field.initializer(), ctx)
                        : null;
                    FieldSpec fieldSpec = new FieldSpec(
                        field.name(),
                        spec.type(),
                        spec.required(),
                        defaultValue,
                        spec.fields(),
                        spec.items(),
                        spec.enumValues()
                    );
                    fields.put(field.name(), fieldSpec);
                }
            }
        }
        return fields;
    }

    private Map<String, ComputedFieldDef> generateComputed(DomainNode domain, GeneratorContext ctx) {
        Map<String, ComputedFieldDef> fields = new LinkedHashMap<>();
        for (DomainMember member : domain.members()) {
            if (member instanceof ComputedNode computed) {
                String computedPath = toComputedPath(computed.name());
                ExprNode expr = generateExpr(computed.expression(), ctx);
                Set<String> deps = extractDeps(expr);
                ComputedFieldDef def = new ComputedFieldDef(computedPath, expr, deps);
                fields.put(computedPath, def);
            }
        }
        return fields;
    }

    private String toComputedPath(String name) {
        if (name == null || name.isEmpty()) {
            return "computed";
        }
        return name.startsWith("computed.") ? name : "computed." + name;
    }

    private Map<String, ActionSpec> generateActions(DomainNode domain, GeneratorContext ctx) {
        Map<String, ActionSpec> actions = new LinkedHashMap<>();
        for (DomainMember member : domain.members()) {
            if (member instanceof ActionNode action) {
                ctx.currentAction = action.name();
                Set<String> params = new HashSet<>();
                for (ParamNode param : action.params()) {
                    params.add(param.name());
                }
                ctx.actionParams.put(action.name(), params);

                FlowNode flow = generateFlow(action.body(), ctx);
                ActionSpec.Builder builder = new ActionSpec.Builder(action.name())
                    .flow(flow);

                for (ParamNode param : action.params()) {
                    FieldSpecData spec = typeExprToFieldSpec(param.typeExpr(), ctx);
                    FieldSpec inputField = new FieldSpec(
                        param.name(),
                        spec.type(),
                        spec.required(),
                        null,
                        spec.fields(),
                        spec.items(),
                        spec.enumValues()
                    );
                    builder.addInputField(param.name(), inputField);
                }

                if (action.available() != null) {
                    builder.available(generateExpr(action.available(), ctx));
                }

                actions.put(action.name(), builder.build());
                ctx.currentAction = null;
            }
        }
        return actions;
    }

    private FlowNode generateFlow(List<? extends AstNode> stmts, GeneratorContext ctx) {
        if (stmts == null || stmts.isEmpty()) {
            return FlowNode.Seq.of(List.of());
        }
        if (stmts.size() == 1) {
            return generateStmt(stmts.get(0), ctx);
        }
        List<FlowNode> steps = new ArrayList<>();
        for (AstNode stmt : stmts) {
            steps.add(generateStmt(stmt, ctx));
        }
        return FlowNode.Seq.of(steps);
    }

    private FlowNode generateStmt(AstNode stmt, GeneratorContext ctx) {
        if (stmt instanceof WhenStmtNode whenStmt) {
            return generateWhen(whenStmt, ctx);
        }
        if (stmt instanceof OnceStmtNode onceStmt) {
            return generateOnce(onceStmt, ctx);
        }
        if (stmt instanceof OnceIntentStmtNode onceIntentStmt) {
            return generateOnceIntent(onceIntentStmt, ctx);
        }
        if (stmt instanceof PatchStmtNode patchStmt) {
            return generatePatch(patchStmt, ctx);
        }
        if (stmt instanceof EffectStmtNode effectStmt) {
            return generateEffect(effectStmt, ctx);
        }
        if (stmt instanceof FailStmtNode failStmt) {
            return generateFail(failStmt, ctx);
        }
        if (stmt instanceof StopStmtNode stopStmt) {
            return FlowNode.Halt.of(stopStmt.reason());
        }
        return FlowNode.Seq.of(List.of());
    }

    private FlowNode generateWhen(WhenStmtNode stmt, GeneratorContext ctx) {
        ExprNode cond = generateExpr(stmt.condition(), ctx);
        FlowNode thenFlow = generateFlow(stmt.body(), ctx);
        return new FlowNode.If(cond, thenFlow, null);
    }

    private FlowNode generateOnce(OnceStmtNode stmt, GeneratorContext ctx) {
        String markerPath = generatePath(stmt.marker(), ctx);
        ExprNode intentIdExpr = Get.of("meta.intentId");

        ExprNode cond = new Neq(Get.of(markerPath), intentIdExpr);
        if (stmt.condition() != null) {
            ExprNode extra = generateExpr(stmt.condition(), ctx);
            cond = new And(List.of(cond, extra));
        }

        FlowNode markerPatch = FlowNode.Patch.set(markerPath, intentIdExpr);
        List<FlowNode> steps = new ArrayList<>();
        steps.add(markerPatch);
        for (InnerStmtNode inner : stmt.body()) {
            steps.add(generateStmt(inner, ctx));
        }

        return new FlowNode.If(cond, FlowNode.Seq.of(steps), null);
    }

    private FlowNode generateOnceIntent(OnceIntentStmtNode stmt, GeneratorContext ctx) {
        String actionName = ctx.currentAction != null ? ctx.currentAction : "unknown";
        int nextIndex = ctx.onceIntentCounters.getOrDefault(actionName, 0);
        ctx.onceIntentCounters.put(actionName, nextIndex + 1);

        String guardId = HashUtils.sha256Sync(actionName + ":" + nextIndex + ":intent");
        String guardPath = "$mel.guards.intent." + guardId;
        ExprNode intentIdExpr = Get.of("meta.intentId");

        ExprNode cond = new Neq(Get.of(guardPath), intentIdExpr);
        if (stmt.condition() != null) {
            ExprNode extra = generateExpr(stmt.condition(), ctx);
            cond = new And(List.of(cond, extra));
        }

        FlowNode markerPatch = FlowNode.Patch.merge(
            "$mel.guards.intent",
            ObjectExpr.of(Map.of(guardId, intentIdExpr))
        );

        List<FlowNode> steps = new ArrayList<>();
        steps.add(markerPatch);
        for (InnerStmtNode inner : stmt.body()) {
            steps.add(generateStmt(inner, ctx));
        }
        return new FlowNode.If(cond, FlowNode.Seq.of(steps), null);
    }

    private FlowNode generatePatch(PatchStmtNode stmt, GeneratorContext ctx) {
        String path = generatePath(stmt.path(), ctx);
        return switch (stmt.op()) {
            case "unset" -> FlowNode.Patch.unset(path);
            case "merge" -> FlowNode.Patch.merge(path, generateExpr(stmt.value(), ctx));
            default -> FlowNode.Patch.set(path, generateExpr(stmt.value(), ctx));
        };
    }

    private FlowNode generateEffect(EffectStmtNode stmt, GeneratorContext ctx) {
        Map<String, ExprNode> params = new LinkedHashMap<>();
        for (EffectArgNode arg : stmt.args()) {
            if (arg.isPath()) {
                String path = generatePath((PathNode) arg.value(), ctx);
                params.put(arg.name(), Lit.of(path));
            } else {
                params.put(arg.name(), generateExpr((ai.manifesto.compiler.parser.ExprNode) arg.value(), ctx));
            }
        }
        return new FlowNode.Effect(stmt.effectType(), params);
    }

    private FlowNode generateFail(FailStmtNode stmt, GeneratorContext ctx) {
        ExprNode message = stmt.message() != null ? generateExpr(stmt.message(), ctx) : null;
        return FlowNode.Fail.of(stmt.code(), message);
    }

    private String generatePath(PathNode path, GeneratorContext ctx) {
        List<String> segments = new ArrayList<>();
        for (PathSegmentNode segment : path.segments()) {
            if (segment instanceof PropertySegmentNode prop) {
                segments.add(prop.name());
            } else if (segment instanceof IndexSegmentNode index) {
                ExprNode indexExpr = generateExpr(index.index(), ctx);
                if (indexExpr instanceof Lit lit) {
                    segments.add(String.valueOf(lit.value()));
                } else {
                    segments.add("*");
                }
            }
        }

        if (segments.isEmpty()) {
            return "";
        }

        String first = segments.get(0);
        if (ctx.stateFields.contains(first)) {
            return String.join(".", segments);
        }
        if (ctx.computedFields.contains(first)) {
            return "computed." + String.join(".", segments);
        }
        if (ctx.currentAction != null && ctx.actionParams.getOrDefault(ctx.currentAction, Set.of()).contains(first)) {
            return "input." + String.join(".", segments);
        }

        return String.join(".", segments);
    }

    private ExprNode generateExpr(ai.manifesto.compiler.parser.ExprNode expr, GeneratorContext ctx) {
        if (expr instanceof LiteralExprNode literal) {
            return Lit.of(literal.value());
        }
        if (expr instanceof IdentifierExprNode ident) {
            return generateIdentifier(ident.name(), ctx, ident.location());
        }
        if (expr instanceof SystemIdentExprNode sys) {
            return generateSystemIdent(sys.path(), ctx, sys.location());
        }
        if (expr instanceof IterationVarExprNode iter) {
            return Get.of("$" + iter.name());
        }
        if (expr instanceof PropertyAccessExprNode prop) {
            ExprNode objectExpr = generateExpr(prop.object(), ctx);
            if (objectExpr instanceof Get get) {
                return Get.of(get.path() + "." + prop.property());
            }
            return new At(objectExpr, Lit.of(prop.property()));
        }
        if (expr instanceof IndexAccessExprNode index) {
            return new At(generateExpr(index.object(), ctx), generateExpr(index.index(), ctx));
        }
        if (expr instanceof FunctionCallExprNode call) {
            List<ExprNode> args = new ArrayList<>();
            for (ai.manifesto.compiler.parser.ExprNode arg : call.args()) {
                args.add(generateExpr(arg, ctx));
            }
            return normalizeFunctionCall(call.name(), args, call.location(), ctx);
        }
        if (expr instanceof UnaryExprNode unary) {
            ExprNode operand = generateExpr(unary.operand(), ctx);
            if ("!".equals(unary.operator())) {
                return new Not(operand);
            }
            return new Neg(operand);
        }
        if (expr instanceof BinaryExprNode binary) {
            ExprNode left = generateExpr(binary.left(), ctx);
            ExprNode right = generateExpr(binary.right(), ctx);
            return normalizeBinary(binary.operator(), left, right, binary.location(), ctx);
        }
        if (expr instanceof TernaryExprNode ternary) {
            return new If(
                generateExpr(ternary.condition(), ctx),
                generateExpr(ternary.consequent(), ctx),
                generateExpr(ternary.alternate(), ctx)
            );
        }
        if (expr instanceof ObjectLiteralExprNode objectLiteral) {
            Map<String, ExprNode> fields = new LinkedHashMap<>();
            for (ObjectPropertyNode prop : objectLiteral.properties()) {
                fields.put(prop.key(), generateExpr(prop.value(), ctx));
            }
            return new ObjectExpr(fields);
        }
        if (expr instanceof ArrayLiteralExprNode arrayLiteral) {
            if (arrayLiteral.elements().isEmpty()) {
                return Lit.of(List.of());
            }
            boolean allLiteral = true;
            List<Object> values = new ArrayList<>();
            for (ai.manifesto.compiler.parser.ExprNode element : arrayLiteral.elements()) {
                if (element instanceof LiteralExprNode literal) {
                    values.add(literal.value());
                } else {
                    allLiteral = false;
                    break;
                }
            }
            if (allLiteral) {
                return Lit.of(values);
            }
            List<ExprNode> items = new ArrayList<>();
            for (ai.manifesto.compiler.parser.ExprNode element : arrayLiteral.elements()) {
                items.add(generateExpr(element, ctx));
            }
            return new Append(Lit.of(List.of()), items);
        }

        return Lit.of(null);
    }

    private ExprNode generateIdentifier(String name, GeneratorContext ctx, SourceLocation location) {
        if (ctx.stateFields.contains(name)) {
            return Get.of(name);
        }
        if (ctx.computedFields.contains(name)) {
            return Get.of("computed." + name);
        }
        if (ctx.currentAction != null && ctx.actionParams.getOrDefault(ctx.currentAction, Set.of()).contains(name)) {
            return Get.of("input." + name);
        }
        ctx.diagnostics.add(Diagnostic.error(
            DiagnosticCode.E_UNDEFINED,
            "Unknown identifier '" + name + "'",
            spanOf(location)
        ));
        return Get.of(name);
    }

    private ExprNode generateSystemIdent(List<String> path, GeneratorContext ctx, SourceLocation location) {
        if (path == null || path.isEmpty()) {
            return Lit.of(null);
        }
        String namespace = path.get(0);
        String rest = path.size() > 1 ? String.join(".", path.subList(1, path.size())) : "";
        return switch (namespace) {
            case "system" -> Get.of("$system." + rest);
            case "meta" -> Get.of("meta." + rest);
            case "input" -> Get.of("input." + rest);
            default -> {
                ctx.diagnostics.add(Diagnostic.error(
                    DiagnosticCode.E003,
                    "Invalid system identifier namespace '$" + namespace + "'",
                    spanOf(location)
                ));
                yield Lit.of(null);
            }
        };
    }

    private ExprNode normalizeBinary(String op, ExprNode left, ExprNode right, SourceLocation location, GeneratorContext ctx) {
        return switch (op) {
            case "+" -> new Add(left, right);
            case "-" -> new Sub(left, right);
            case "*" -> new Mul(left, right);
            case "/" -> new Div(left, right);
            case "%" -> new Mod(left, right);
            case "==" -> new Eq(left, right);
            case "!=" -> new Neq(left, right);
            case "<" -> new Lt(left, right);
            case "<=" -> new Lte(left, right);
            case ">" -> new Gt(left, right);
            case ">=" -> new Gte(left, right);
            case "&&" -> new And(List.of(left, right));
            case "||" -> new Or(List.of(left, right));
            case "??" -> new Coalesce(List.of(left, right));
            default -> unsupportedExpr("Unknown operator '" + op + "'", location, ctx);
        };
    }

    private ExprNode normalizeFunctionCall(String name, List<ExprNode> args, SourceLocation location, GeneratorContext ctx) {
        return switch (name) {
            case "add" -> new Add(args.get(0), args.get(1));
            case "sub" -> new Sub(args.get(0), args.get(1));
            case "mul" -> new Mul(args.get(0), args.get(1));
            case "div" -> new Div(args.get(0), args.get(1));
            case "mod" -> new Mod(args.get(0), args.get(1));
            case "neg" -> new Neg(args.get(0));
            case "abs" -> new Abs(args.get(0));
            case "min" -> args.size() == 1
                ? new MinArray(args.get(0))
                : new Min(args);
            case "max" -> args.size() == 1
                ? new MaxArray(args.get(0))
                : new Max(args);
            case "sum" -> new SumArray(args.get(0));
            case "floor" -> new Floor(args.get(0));
            case "ceil" -> new Ceil(args.get(0));
            case "round" -> new Round(args.get(0));
            case "sqrt" -> new Sqrt(args.get(0));
            case "pow" -> new Pow(args.get(0), args.get(1));
            case "eq" -> new Eq(args.get(0), args.get(1));
            case "neq" -> new Neq(args.get(0), args.get(1));
            case "gt" -> new Gt(args.get(0), args.get(1));
            case "gte" -> new Gte(args.get(0), args.get(1));
            case "lt" -> new Lt(args.get(0), args.get(1));
            case "lte" -> new Lte(args.get(0), args.get(1));
            case "and" -> new And(args);
            case "or" -> new Or(args);
            case "not" -> new Not(args.get(0));
            case "isNull" -> new IsNull(args.get(0));
            case "typeof" -> new Typeof(args.get(0));
            case "coalesce" -> new Coalesce(args);
            case "concat" -> new Concat(args);
            case "trim" -> new Trim(args.get(0));
            case "toLowerCase", "lower" -> new ToLowerCase(args.get(0));
            case "toUpperCase", "upper" -> new ToUpperCase(args.get(0));
            case "substr", "substring" -> args.size() >= 3
                ? new Substring(args.get(0), args.get(1), args.get(2))
                : new Substring(args.get(0), args.get(1), new Len(args.get(0)));
            case "len", "length" -> new Len(args.get(0));
            case "strLen", "strlen" -> new StrLen(args.get(0));
            case "at" -> new At(args.get(0), args.get(1));
            case "first" -> new First(args.get(0));
            case "last" -> new Last(args.get(0));
            case "slice" -> args.size() >= 3
                ? new Slice(args.get(0), args.get(1), args.get(2))
                : new Slice(args.get(0), args.get(1), new Len(args.get(0)));
            case "includes" -> new Includes(args.get(0), args.get(1));
            case "filter" -> new Filter(args.get(0), args.get(1));
            case "map" -> new ai.manifesto.core.expr.collection.Map(args.get(0), args.get(1));
            case "find" -> new Find(args.get(0), args.get(1));
            case "every" -> new Every(args.get(0), args.get(1));
            case "some" -> new Some(args.get(0), args.get(1));
            case "append" -> new Append(args.get(0), args.subList(1, args.size()));
            case "keys" -> new Keys(args.get(0));
            case "values" -> new Values(args.get(0));
            case "entries" -> new Entries(args.get(0));
            case "merge" -> new Merge(args);
            case "if", "cond" -> new If(args.get(0), args.get(1), args.get(2));
            case "toString" -> new ToString(args.get(0));
            case "isNotNull", "notNull" -> new Not(new IsNull(args.get(0)));
            default -> unsupportedExpr("Unknown function '" + name + "'", location, ctx);
        };
    }

    private ExprNode unsupportedExpr(String message, SourceLocation location, GeneratorContext ctx) {
        ctx.diagnostics.add(Diagnostic.error(
            DiagnosticCode.E_TYPE_MISMATCH,
            message,
            spanOf(location)
        ));
        return Lit.of(null);
    }

    private Object evaluateInitializer(ai.manifesto.compiler.parser.ExprNode expr, GeneratorContext ctx) {
        if (expr instanceof LiteralExprNode literal) {
            return literal.value();
        }
        if (expr instanceof ArrayLiteralExprNode arrayLiteral) {
            List<Object> values = new ArrayList<>();
            for (ai.manifesto.compiler.parser.ExprNode element : arrayLiteral.elements()) {
                values.add(evaluateInitializer(element, ctx));
            }
            return values;
        }
        if (expr instanceof ObjectLiteralExprNode objectLiteral) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (ObjectPropertyNode prop : objectLiteral.properties()) {
                map.put(prop.key(), evaluateInitializer(prop.value(), ctx));
            }
            return map;
        }
        return null;
    }

    private FieldSpecData typeExprToFieldSpec(TypeExprNode typeExpr, GeneratorContext ctx) {
        if (typeExpr instanceof SimpleTypeNode simple) {
            return switch (simple.name()) {
                case "string" -> new FieldSpecData("string", true, null, null, null);
                case "number", "integer" -> new FieldSpecData("number", true, null, null, null);
                case "boolean" -> new FieldSpecData("boolean", true, null, null, null);
                case "null" -> new FieldSpecData("null", true, null, null, null);
                default -> {
                    TypeDeclNode typeDecl = ctx.typeDefs.get(simple.name());
                    if (typeDecl != null) {
                        yield typeExprToFieldSpec(typeDecl.typeExpr(), ctx);
                    }
                    yield new FieldSpecData("object", true, null, null, null);
                }
            };
        }
        if (typeExpr instanceof UnionTypeNode union) {
            List<Object> literals = new ArrayList<>();
            boolean isLiteralUnion = true;
            boolean hasNull = false;
            for (TypeExprNode t : union.types()) {
                if (t instanceof LiteralTypeNode literal) {
                    if (literal.value() == null) {
                        hasNull = true;
                    }
                    literals.add(literal.value());
                    continue;
                }
                if (t instanceof SimpleTypeNode simple && "null".equals(simple.name())) {
                    hasNull = true;
                    literals.add(null);
                    continue;
                }
                isLiteralUnion = false;
            }
            if (isLiteralUnion && !literals.isEmpty()) {
                String enumBaseType = inferEnumBaseType(literals);
                return new FieldSpecData(enumBaseType, !hasNull, null, null, literals);
            }
            if (hasNull) {
                for (TypeExprNode t : union.types()) {
                    if (!(t instanceof SimpleTypeNode simple && "null".equals(simple.name()))) {
                        FieldSpecData inner = typeExprToFieldSpec(t, ctx);
                        return new FieldSpecData(inner.type(), false, inner.fields(), inner.items(), inner.enumValues());
                    }
                }
            }
            for (TypeExprNode t : union.types()) {
                if (!(t instanceof SimpleTypeNode simple && "null".equals(simple.name()))) {
                    return typeExprToFieldSpec(t, ctx);
                }
            }
            return new FieldSpecData("null", true, null, null, null);
        }
        if (typeExpr instanceof ArrayTypeNode array) {
            FieldSpecData itemSpec = typeExprToFieldSpec(array.elementType(), ctx);
            FieldSpec items = new FieldSpec(
                "item",
                itemSpec.type(),
                itemSpec.required(),
                null,
                itemSpec.fields(),
                itemSpec.items(),
                itemSpec.enumValues()
            );
            return new FieldSpecData("array", true, null, items, null);
        }
        if (typeExpr instanceof RecordTypeNode) {
            return new FieldSpecData("object", true, null, null, null);
        }
        if (typeExpr instanceof LiteralTypeNode literal) {
            Object value = literal.value();
            if (value instanceof String) {
                return new FieldSpecData("string", true, null, null, null);
            }
            if (value instanceof Number) {
                return new FieldSpecData("number", true, null, null, null);
            }
            if (value instanceof Boolean) {
                return new FieldSpecData("boolean", true, null, null, null);
            }
            return new FieldSpecData("null", true, null, null, null);
        }
        if (typeExpr instanceof ObjectTypeNode objectType) {
            Map<String, FieldSpec> fields = new LinkedHashMap<>();
            for (TypeFieldNode field : objectType.fields()) {
                FieldSpecData fieldSpec = typeExprToFieldSpec(field.typeExpr(), ctx);
                FieldSpec spec = new FieldSpec(
                    field.name(),
                    fieldSpec.type(),
                    !field.optional() && fieldSpec.required(),
                    null,
                    fieldSpec.fields(),
                    fieldSpec.items(),
                    fieldSpec.enumValues()
                );
                fields.put(field.name(), spec);
            }
            return new FieldSpecData("object", true, fields, null, null);
        }
        return new FieldSpecData("object", true, null, null, null);
    }

    private Map<String, Object> mapOf(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < entries.length; i += 2) {
            map.put(String.valueOf(entries[i]), entries[i + 1]);
        }
        return map;
    }

    private String inferEnumBaseType(List<Object> literals) {
        boolean hasString = false;
        boolean hasNumber = false;
        boolean hasBoolean = false;
        for (Object value : literals) {
            if (value == null) {
                continue;
            }
            if (value instanceof String) {
                hasString = true;
                continue;
            }
            if (value instanceof Number) {
                hasNumber = true;
                continue;
            }
            if (value instanceof Boolean) {
                hasBoolean = true;
                continue;
            }
            return "enum";
        }
        int categories = (hasString ? 1 : 0) + (hasNumber ? 1 : 0) + (hasBoolean ? 1 : 0);
        if (categories == 1) {
            if (hasString) return "string";
            if (hasNumber) return "number";
            return "boolean";
        }
        return "enum";
    }

    private Set<String> extractDeps(ExprNode expr) {
        Set<String> deps = new LinkedHashSet<>();
        visitExpr(expr, deps);
        return deps;
    }

    private void visitExpr(ExprNode expr, Set<String> deps) {
        if (expr == null) return;
        if (expr instanceof Get get) {
            deps.add(get.path());
            return;
        }
        if (expr instanceof Lit) return;
        if (expr instanceof Add add) { visitExpr(add.left(), deps); visitExpr(add.right(), deps); return; }
        if (expr instanceof Sub sub) { visitExpr(sub.left(), deps); visitExpr(sub.right(), deps); return; }
        if (expr instanceof Mul mul) { visitExpr(mul.left(), deps); visitExpr(mul.right(), deps); return; }
        if (expr instanceof Div div) { visitExpr(div.left(), deps); visitExpr(div.right(), deps); return; }
        if (expr instanceof Mod mod) { visitExpr(mod.left(), deps); visitExpr(mod.right(), deps); return; }
        if (expr instanceof Min min) { min.args().forEach(a -> visitExpr(a, deps)); return; }
        if (expr instanceof Max max) { max.args().forEach(a -> visitExpr(a, deps)); return; }
        if (expr instanceof Abs abs) { visitExpr(abs.arg(), deps); return; }
        if (expr instanceof Neg neg) { visitExpr(neg.arg(), deps); return; }
        if (expr instanceof Round round) { visitExpr(round.arg(), deps); return; }
        if (expr instanceof Floor floor) { visitExpr(floor.arg(), deps); return; }
        if (expr instanceof Ceil ceil) { visitExpr(ceil.arg(), deps); return; }
        if (expr instanceof Eq eq) { visitExpr(eq.left(), deps); visitExpr(eq.right(), deps); return; }
        if (expr instanceof Neq neq) { visitExpr(neq.left(), deps); visitExpr(neq.right(), deps); return; }
        if (expr instanceof Gt gt) { visitExpr(gt.left(), deps); visitExpr(gt.right(), deps); return; }
        if (expr instanceof Gte gte) { visitExpr(gte.left(), deps); visitExpr(gte.right(), deps); return; }
        if (expr instanceof Lt lt) { visitExpr(lt.left(), deps); visitExpr(lt.right(), deps); return; }
        if (expr instanceof Lte lte) { visitExpr(lte.left(), deps); visitExpr(lte.right(), deps); return; }
        if (expr instanceof And and) { and.args().forEach(a -> visitExpr(a, deps)); return; }
        if (expr instanceof Or or) { or.args().forEach(a -> visitExpr(a, deps)); return; }
        if (expr instanceof Not not) { visitExpr(not.arg(), deps); return; }
        if (expr instanceof If ifExpr) { visitExpr(ifExpr.cond(), deps); visitExpr(ifExpr.thenExpr(), deps); visitExpr(ifExpr.elseExpr(), deps); return; }
        if (expr instanceof Concat concat) { concat.args().forEach(a -> visitExpr(a, deps)); return; }
        if (expr instanceof Trim trim) { visitExpr(trim.str(), deps); return; }
        if (expr instanceof Substring sub) { visitExpr(sub.str(), deps); visitExpr(sub.start(), deps); visitExpr(sub.end(), deps); return; }
        if (expr instanceof Len len) { visitExpr(len.arg(), deps); return; }
        if (expr instanceof At at) { visitExpr(at.array(), deps); visitExpr(at.index(), deps); return; }
        if (expr instanceof First first) { visitExpr(first.array(), deps); return; }
        if (expr instanceof Last last) { visitExpr(last.array(), deps); return; }
        if (expr instanceof Slice slice) { visitExpr(slice.array(), deps); visitExpr(slice.start(), deps); visitExpr(slice.end(), deps); return; }
        if (expr instanceof Includes includes) { visitExpr(includes.array(), deps); visitExpr(includes.item(), deps); return; }
        if (expr instanceof Filter filter) { visitExpr(filter.array(), deps); visitExpr(filter.predicate(), deps); return; }
        if (expr instanceof ai.manifesto.core.expr.collection.Map map) { visitExpr(map.array(), deps); visitExpr(map.mapper(), deps); return; }
        if (expr instanceof Find find) { visitExpr(find.array(), deps); visitExpr(find.predicate(), deps); return; }
        if (expr instanceof Every every) { visitExpr(every.array(), deps); visitExpr(every.predicate(), deps); return; }
        if (expr instanceof Some some) { visitExpr(some.array(), deps); visitExpr(some.predicate(), deps); return; }
        if (expr instanceof Append append) { visitExpr(append.array(), deps); append.items().forEach(i -> visitExpr(i, deps)); return; }
        if (expr instanceof Keys keys) { visitExpr(keys.obj(), deps); return; }
        if (expr instanceof Values values) { visitExpr(values.obj(), deps); return; }
        if (expr instanceof Entries entries) { visitExpr(entries.obj(), deps); return; }
        if (expr instanceof Merge merge) { merge.objects().forEach(o -> visitExpr(o, deps)); return; }
        if (expr instanceof Typeof typeOf) { visitExpr(typeOf.arg(), deps); return; }
        if (expr instanceof IsNull isNull) { visitExpr(isNull.arg(), deps); return; }
        if (expr instanceof Coalesce coalesce) { coalesce.args().forEach(a -> visitExpr(a, deps)); }
        if (expr instanceof ObjectExpr objectExpr) {
            objectExpr.fields().values().forEach(v -> visitExpr(v, deps));
        }
    }

    private static ai.manifesto.compiler.diagnostics.SourceSpan spanOf(SourceLocation location) {
        return ai.manifesto.compiler.diagnostics.SourceSpan.of(
            location.start().line(),
            location.start().column(),
            1
        );
    }

    private record FieldSpecData(
        String type,
        boolean required,
        Map<String, FieldSpec> fields,
        FieldSpec items,
        List<Object> enumValues
    ) {
    }

    private static final class GeneratorContext {
        private final Set<String> stateFields = new HashSet<>();
        private final Set<String> computedFields = new HashSet<>();
        private final Map<String, Set<String>> actionParams = new HashMap<>();
        private final Map<String, Integer> onceIntentCounters = new HashMap<>();
        private final Map<String, TypeDeclNode> typeDefs = new HashMap<>();
        private final List<Diagnostic> diagnostics = new ArrayList<>();
        private String currentAction;

        private boolean hasErrors() {
            return diagnostics.stream().anyMatch(d -> d.severity() == DiagnosticSeverity.ERROR);
        }
    }
}
