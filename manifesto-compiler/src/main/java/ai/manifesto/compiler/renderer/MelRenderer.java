package ai.manifesto.compiler.renderer;

import ai.manifesto.compiler.parser.*;

import java.util.StringJoiner;

/**
 * MelRenderer - AST -> MEL string (minimal)
 */
public final class MelRenderer {

    public static String renderProgram(ProgramNode program) {
        return renderProgram(program, RenderOptions.defaults());
    }

    public static String renderProgram(ProgramNode program, RenderOptions options) {
        if (program == null) {
            return "";
        }
        return renderDomain(program.domain(), options);
    }

    public static String renderDomain(DomainNode domain) {
        return renderDomain(domain, RenderOptions.defaults());
    }

    public static String renderDomain(DomainNode domain, RenderOptions options) {
        String indent = options.indent();
        String newline = options.newline();
        StringBuilder sb = new StringBuilder();
        sb.append("domain ").append(domain.name()).append(" {").append(newline);

        for (TypeDeclNode typeDecl : domain.types()) {
            sb.append(indent).append("type ").append(typeDecl.name())
              .append(" = ").append(renderTypeExpr(typeDecl.typeExpr()))
              .append(newline);
        }

        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                sb.append(indent).append("state {").append(newline);
                for (StateFieldNode field : state.fields()) {
                    sb.append(indent).append(indent).append(field.name()).append(": ")
                      .append(renderTypeExpr(field.typeExpr()));
                    if (field.initializer() != null) {
                        sb.append(" = ").append(renderExpr(field.initializer()));
                    }
                    sb.append(newline);
                }
                sb.append(indent).append("}").append(newline);
            } else if (member instanceof ComputedNode computed) {
                sb.append(indent).append("computed ").append(computed.name())
                  .append(" = ").append(renderExpr(computed.expression()))
                  .append(newline);
            } else if (member instanceof ActionNode action) {
                sb.append(indent).append("action ").append(action.name()).append("(");
                StringJoiner params = new StringJoiner(", ");
                for (ParamNode param : action.params()) {
                    params.add(param.name() + ": " + renderTypeExpr(param.typeExpr()));
                }
                sb.append(params).append(")");
                if (action.available() != null) {
                    sb.append(" available when ").append(renderExpr(action.available()));
                }
                sb.append(" {").append(newline);
                for (GuardedStmtNode stmt : action.body()) {
                    renderStmt(sb, stmt, indent + indent, indent, newline);
                }
                sb.append(indent).append("}").append(newline);
            }
        }

        sb.append("}").append(newline);
        return sb.toString();
    }

    private static void renderStmt(StringBuilder sb, AstNode stmt, String indent, String indentUnit, String newline) {
        if (stmt instanceof WhenStmtNode whenStmt) {
            sb.append(indent).append("when ").append(renderExpr(whenStmt.condition())).append(" {").append(newline);
            for (InnerStmtNode inner : whenStmt.body()) {
                renderStmt(sb, inner, indent + indentUnit, indentUnit, newline);
            }
            sb.append(indent).append("}").append(newline);
            return;
        }
        if (stmt instanceof OnceStmtNode onceStmt) {
            sb.append(indent).append("once(").append(renderPath(onceStmt.marker())).append(")");
            if (onceStmt.condition() != null) {
                sb.append(" when ").append(renderExpr(onceStmt.condition()));
            }
            sb.append(" {").append(newline);
            for (InnerStmtNode inner : onceStmt.body()) {
                renderStmt(sb, inner, indent + indentUnit, indentUnit, newline);
            }
            sb.append(indent).append("}").append(newline);
            return;
        }
        if (stmt instanceof PatchStmtNode patchStmt) {
            sb.append(indent).append("patch ").append(renderPath(patchStmt.path())).append(" ");
            if ("unset".equals(patchStmt.op())) {
                sb.append("unset");
            } else if ("merge".equals(patchStmt.op())) {
                sb.append("merge ").append(renderExpr(patchStmt.value()));
            } else {
                sb.append("= ").append(renderExpr(patchStmt.value()));
            }
            sb.append(newline);
            return;
        }
        if (stmt instanceof EffectStmtNode effectStmt) {
            sb.append(indent).append("effect ").append(effectStmt.effectType()).append("({");
            StringJoiner args = new StringJoiner(", ");
            for (EffectArgNode arg : effectStmt.args()) {
                String value = arg.isPath()
                    ? renderPath((PathNode) arg.value())
                    : renderExpr((ExprNode) arg.value());
                args.add(arg.name() + ": " + value);
            }
            sb.append(args).append("})").append(newline);
            return;
        }
        if (stmt instanceof FailStmtNode failStmt) {
            sb.append(indent).append("fail ").append(renderValue(failStmt.code()));
            if (failStmt.message() != null) {
                sb.append(" with ").append(renderExpr(failStmt.message()));
            }
            sb.append(newline);
            return;
        }
        if (stmt instanceof StopStmtNode stopStmt) {
            sb.append(indent).append("stop ").append(renderValue(stopStmt.reason())).append(newline);
        }
    }

    public record RenderOptions(String indent, String newline) {
        public static RenderOptions defaults() {
            return new RenderOptions("  ", "\n");
        }
    }

    public static String renderTypeExpr(TypeExprNode typeExpr) {
        if (typeExpr instanceof SimpleTypeNode simple) {
            return simple.name();
        }
        if (typeExpr instanceof UnionTypeNode union) {
            StringJoiner joiner = new StringJoiner(" | ");
            for (TypeExprNode t : union.types()) {
                joiner.add(renderTypeExpr(t));
            }
            return joiner.toString();
        }
        if (typeExpr instanceof ArrayTypeNode array) {
            return "Array<" + renderTypeExpr(array.elementType()) + ">";
        }
        if (typeExpr instanceof RecordTypeNode record) {
            return "Record<" + renderTypeExpr(record.keyType()) + ", " + renderTypeExpr(record.valueType()) + ">";
        }
        if (typeExpr instanceof LiteralTypeNode literal) {
            return renderValue(literal.value());
        }
        if (typeExpr instanceof ObjectTypeNode objectType) {
            StringJoiner joiner = new StringJoiner(", ");
            for (TypeFieldNode field : objectType.fields()) {
                String name = field.name() + (field.optional() ? "?" : "");
                joiner.add(name + ": " + renderTypeExpr(field.typeExpr()));
            }
            return "{ " + joiner + " }";
        }
        return "any";
    }

    public static String renderExpr(ExprNode expr) {
        if (expr instanceof LiteralExprNode literal) {
            return renderValue(literal.value());
        }
        if (expr instanceof IdentifierExprNode ident) {
            return ident.name();
        }
        if (expr instanceof SystemIdentExprNode systemIdent) {
            return "$" + String.join(".", systemIdent.path());
        }
        if (expr instanceof IterationVarExprNode iter) {
            return "$" + iter.name();
        }
        if (expr instanceof PropertyAccessExprNode prop) {
            return renderExpr(prop.object()) + "." + prop.property();
        }
        if (expr instanceof IndexAccessExprNode index) {
            return renderExpr(index.object()) + "[" + renderExpr(index.index()) + "]";
        }
        if (expr instanceof FunctionCallExprNode call) {
            StringJoiner joiner = new StringJoiner(", ");
            for (ExprNode arg : call.args()) {
                joiner.add(renderExpr(arg));
            }
            return call.name() + "(" + joiner + ")";
        }
        if (expr instanceof UnaryExprNode unary) {
            return unary.operator() + renderExpr(unary.operand());
        }
        if (expr instanceof BinaryExprNode binary) {
            return renderExpr(binary.left()) + " " + binary.operator() + " " + renderExpr(binary.right());
        }
        if (expr instanceof TernaryExprNode ternary) {
            return renderExpr(ternary.condition()) + " ? " + renderExpr(ternary.consequent()) + " : " + renderExpr(ternary.alternate());
        }
        if (expr instanceof ObjectLiteralExprNode objectLiteral) {
            StringJoiner joiner = new StringJoiner(", ");
            for (ObjectPropertyNode prop : objectLiteral.properties()) {
                joiner.add(prop.key() + ": " + renderExpr(prop.value()));
            }
            return "{ " + joiner + " }";
        }
        if (expr instanceof ArrayLiteralExprNode arrayLiteral) {
            StringJoiner joiner = new StringJoiner(", ");
            for (ExprNode element : arrayLiteral.elements()) {
                joiner.add(renderExpr(element));
            }
            return "[" + joiner + "]";
        }
        return "null";
    }

    public static String renderPath(PathNode path) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (PathSegmentNode segment : path.segments()) {
            if (segment instanceof PropertySegmentNode prop) {
                if (!first) {
                    sb.append(".");
                }
                sb.append(prop.name());
            } else if (segment instanceof IndexSegmentNode index) {
                sb.append("[").append(renderExpr(index.index())).append("]");
            }
            first = false;
        }
        return sb.toString();
    }

    public static String renderValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "\"" + s.replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Boolean b) {
            return b ? "true" : "false";
        }
        return value.toString();
    }
}
