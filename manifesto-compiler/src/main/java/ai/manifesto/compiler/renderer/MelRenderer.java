package ai.manifesto.compiler.renderer;

import ai.manifesto.compiler.parser.*;

import java.util.StringJoiner;

/**
 * MelRenderer - AST -> MEL string (minimal)
 */
public final class MelRenderer {

    public static String renderProgram(ProgramNode program) {
        if (program == null) {
            return "";
        }
        return renderDomain(program.domain());
    }

    public static String renderDomain(DomainNode domain) {
        StringBuilder sb = new StringBuilder();
        sb.append("domain ").append(domain.name()).append(" {\n");

        for (TypeDeclNode typeDecl : domain.types()) {
            sb.append("  type ").append(typeDecl.name())
              .append(" = ").append(renderTypeExpr(typeDecl.typeExpr()))
              .append("\n");
        }

        for (DomainMember member : domain.members()) {
            if (member instanceof StateNode state) {
                sb.append("  state {\n");
                for (StateFieldNode field : state.fields()) {
                    sb.append("    ").append(field.name()).append(": ")
                      .append(renderTypeExpr(field.typeExpr()));
                    if (field.initializer() != null) {
                        sb.append(" = ").append(renderExpr(field.initializer()));
                    }
                    sb.append("\n");
                }
                sb.append("  }\n");
            } else if (member instanceof ComputedNode computed) {
                sb.append("  computed ").append(computed.name())
                  .append(" = ").append(renderExpr(computed.expression()))
                  .append("\n");
            } else if (member instanceof ActionNode action) {
                sb.append("  action ").append(action.name()).append("(");
                StringJoiner params = new StringJoiner(", ");
                for (ParamNode param : action.params()) {
                    params.add(param.name() + ": " + renderTypeExpr(param.typeExpr()));
                }
                sb.append(params).append(")");
                if (action.available() != null) {
                    sb.append(" available when ").append(renderExpr(action.available()));
                }
                sb.append(" {\n");
                for (GuardedStmtNode stmt : action.body()) {
                    renderStmt(sb, stmt, "    ");
                }
                sb.append("  }\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private static void renderStmt(StringBuilder sb, AstNode stmt, String indent) {
        if (stmt instanceof WhenStmtNode whenStmt) {
            sb.append(indent).append("when ").append(renderExpr(whenStmt.condition())).append(" {\n");
            for (InnerStmtNode inner : whenStmt.body()) {
                renderStmt(sb, inner, indent + "  ");
            }
            sb.append(indent).append("}\n");
            return;
        }
        if (stmt instanceof OnceStmtNode onceStmt) {
            sb.append(indent).append("once(").append(renderPath(onceStmt.marker())).append(")");
            if (onceStmt.condition() != null) {
                sb.append(" when ").append(renderExpr(onceStmt.condition()));
            }
            sb.append(" {\n");
            for (InnerStmtNode inner : onceStmt.body()) {
                renderStmt(sb, inner, indent + "  ");
            }
            sb.append(indent).append("}\n");
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
            sb.append("\n");
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
            sb.append(args).append("})\n");
            return;
        }
        if (stmt instanceof FailStmtNode failStmt) {
            sb.append(indent).append("fail ").append(renderValue(failStmt.code()));
            if (failStmt.message() != null) {
                sb.append(" with ").append(renderExpr(failStmt.message()));
            }
            sb.append("\n");
            return;
        }
        if (stmt instanceof StopStmtNode stopStmt) {
            sb.append(indent).append("stop ").append(renderValue(stopStmt.reason())).append("\n");
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
