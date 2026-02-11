package ai.manifesto.compiler.parser;

import ai.manifesto.compiler.diagnostics.Diagnostic;
import ai.manifesto.compiler.diagnostics.DiagnosticCode;
import ai.manifesto.compiler.diagnostics.SourceSpan;
import ai.manifesto.compiler.lexer.SourceLocation;
import ai.manifesto.compiler.lexer.SourcePosition;
import ai.manifesto.compiler.lexer.Token;
import ai.manifesto.compiler.lexer.TokenKind;

import java.util.ArrayList;
import java.util.List;

/**
 * KR: Parser는 MEL 토큰 스트림을 AST로 변환하는 구문 분석기입니다.
 * EN: Parser parses MEL token streams into AST structures.
 */
public final class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens == null ? List.of() : tokens;
    }

    public ParseResult parse() {
        try {
            ProgramNode program = parseProgram();
            return new ParseResult(program, List.copyOf(diagnostics));
        } catch (RuntimeException ex) {
            return new ParseResult(null, List.copyOf(diagnostics));
        }
    }

    // ========= Program =========

    private ProgramNode parseProgram() {
        SourceLocation start = peek().location();
        List<ImportNode> imports = new ArrayList<>();

        while (check(TokenKind.IMPORT)) {
            imports.add(parseImport());
        }

        DomainNode domain = parseDomain();
        return new ProgramNode(imports, domain, mergeLocations(start, domain.location()));
    }

    private ImportNode parseImport() {
        SourceLocation start = consume(TokenKind.IMPORT, "Expected 'import'").location();
        consume(TokenKind.LBRACE, "Expected '{' after 'import'");

        List<String> names = new ArrayList<>();
        do {
            names.add(consume(TokenKind.IDENTIFIER, "Expected identifier").lexeme());
        } while (match(TokenKind.COMMA));

        consume(TokenKind.RBRACE, "Expected '}' after import names");
        consume(TokenKind.FROM, "Expected 'from' after import names");
        Token fromToken = consume(TokenKind.STRING, "Expected string after 'from'");

        return new ImportNode(names, stringValue(fromToken), mergeLocations(start, fromToken.location()));
    }

    private DomainNode parseDomain() {
        SourceLocation start = consume(TokenKind.DOMAIN, "Expected 'domain'").location();
        String name = consume(TokenKind.IDENTIFIER, "Expected domain name").lexeme();
        consume(TokenKind.LBRACE, "Expected '{' after domain name");

        List<TypeDeclNode> types = new ArrayList<>();
        while (check(TokenKind.TYPE) && !isAtEnd()) {
            types.add(parseTypeDecl());
        }

        List<DomainMember> members = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            if (check(TokenKind.TYPE)) {
                types.add(parseTypeDecl());
            } else {
                DomainMember member = parseDomainMember();
                if (member != null) {
                    members.add(member);
                }
            }
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close domain").location();
        return new DomainNode(name, types, members, mergeLocations(start, end));
    }

    private TypeDeclNode parseTypeDecl() {
        SourceLocation start = consume(TokenKind.TYPE, "Expected 'type'").location();
        String name = consume(TokenKind.IDENTIFIER, "Expected type name").lexeme();
        consume(TokenKind.EQ, "Expected '=' after type name");
        TypeExprNode typeExpr = parseTypeExpr();
        return new TypeDeclNode(name, typeExpr, mergeLocations(start, typeExpr.location()));
    }

    private DomainMember parseDomainMember() {
        if (check(TokenKind.STATE)) return parseState();
        if (check(TokenKind.COMPUTED)) return parseComputed();
        if (check(TokenKind.ACTION)) return parseAction();
        error("Unexpected token '" + peek().lexeme() + "'. Expected 'state', 'computed', or 'action'.");
        advance();
        return null;
    }

    // ========= State =========

    private StateNode parseState() {
        SourceLocation start = consume(TokenKind.STATE, "Expected 'state'").location();
        consume(TokenKind.LBRACE, "Expected '{' after 'state'");

        List<StateFieldNode> fields = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            fields.add(parseStateField());
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close state block").location();
        return new StateNode(fields, mergeLocations(start, end));
    }

    private StateFieldNode parseStateField() {
        Token nameToken = consume(TokenKind.IDENTIFIER, "Expected field name");
        consume(TokenKind.COLON, "Expected ':' after field name");
        TypeExprNode typeExpr = parseTypeExpr();
        ExprNode initializer = null;
        if (match(TokenKind.EQ)) {
            initializer = parseExpression(Precedence.NONE);
        }
        SourceLocation end = initializer != null ? initializer.location() : typeExpr.location();
        return new StateFieldNode(nameToken.lexeme(), typeExpr, initializer, mergeLocations(nameToken.location(), end));
    }

    // ========= Computed =========

    private ComputedNode parseComputed() {
        SourceLocation start = consume(TokenKind.COMPUTED, "Expected 'computed'").location();
        String name = consume(TokenKind.IDENTIFIER, "Expected computed name").lexeme();
        consume(TokenKind.EQ, "Expected '=' after computed name");
        ExprNode expression = parseExpression(Precedence.NONE);
        return new ComputedNode(name, expression, mergeLocations(start, expression.location()));
    }

    // ========= Action =========

    private ActionNode parseAction() {
        SourceLocation start = consume(TokenKind.ACTION, "Expected 'action'").location();
        String name = consume(TokenKind.IDENTIFIER, "Expected action name").lexeme();
        consume(TokenKind.LPAREN, "Expected '(' after action name");

        List<ParamNode> params = new ArrayList<>();
        if (!check(TokenKind.RPAREN)) {
            do {
                params.add(parseParam());
            } while (match(TokenKind.COMMA));
        }

        consume(TokenKind.RPAREN, "Expected ')' after parameters");

        ExprNode available = null;
        if (match(TokenKind.AVAILABLE)) {
            consume(TokenKind.WHEN, "Expected 'when' after 'available'");
            available = parseExpression(Precedence.NONE);
        }

        consume(TokenKind.LBRACE, "Expected '{' to start action body");

        List<GuardedStmtNode> body = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            GuardedStmtNode stmt = parseGuardedStmt();
            if (stmt != null) {
                body.add(stmt);
            }
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close action").location();
        return new ActionNode(name, params, available, body, mergeLocations(start, end));
    }

    private ParamNode parseParam() {
        Token nameToken = consume(TokenKind.IDENTIFIER, "Expected parameter name");
        consume(TokenKind.COLON, "Expected ':' after parameter name");
        TypeExprNode typeExpr = parseTypeExpr();
        return new ParamNode(nameToken.lexeme(), typeExpr, mergeLocations(nameToken.location(), typeExpr.location()));
    }

    // ========= Statements =========

    private GuardedStmtNode parseGuardedStmt() {
        if (check(TokenKind.WHEN)) return parseWhenStmt();
        if (check(TokenKind.ONCE)) return parseOnceStmt();
        if (isOnceIntentContext()) return parseOnceIntentStmt();
        error("Unexpected token '" + peek().lexeme() + "'. Expected 'when', 'once', or 'onceIntent'.");
        advance();
        return null;
    }

    private WhenStmtNode parseWhenStmt() {
        SourceLocation start = consume(TokenKind.WHEN, "Expected 'when'").location();
        ExprNode condition = parseExpression(Precedence.NONE);
        consume(TokenKind.LBRACE, "Expected '{' after when condition");

        List<InnerStmtNode> body = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            InnerStmtNode stmt = parseInnerStmt();
            if (stmt != null) {
                body.add(stmt);
            }
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close when block").location();
        return new WhenStmtNode(condition, body, mergeLocations(start, end));
    }

    private OnceStmtNode parseOnceStmt() {
        SourceLocation start = consume(TokenKind.ONCE, "Expected 'once'").location();
        consume(TokenKind.LPAREN, "Expected '(' after 'once'");
        PathNode marker = parsePath();
        consume(TokenKind.RPAREN, "Expected ')' after marker");

        ExprNode condition = null;
        if (match(TokenKind.WHEN)) {
            condition = parseExpression(Precedence.NONE);
        }

        consume(TokenKind.LBRACE, "Expected '{' to start once block");
        List<InnerStmtNode> body = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            InnerStmtNode stmt = parseInnerStmt();
            if (stmt != null) {
                body.add(stmt);
            }
        }
        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close once block").location();
        return new OnceStmtNode(marker, condition, body, mergeLocations(start, end));
    }

    private OnceIntentStmtNode parseOnceIntentStmt() {
        Token startToken = consume(TokenKind.IDENTIFIER, "Expected 'onceIntent'");

        ExprNode condition = null;
        if (match(TokenKind.WHEN)) {
            condition = parseExpression(Precedence.NONE);
        }

        consume(TokenKind.LBRACE, "Expected '{' to start onceIntent block");
        List<InnerStmtNode> body = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            InnerStmtNode stmt = parseInnerStmt();
            if (stmt != null) {
                body.add(stmt);
            }
        }
        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close onceIntent block").location();
        return new OnceIntentStmtNode(condition, body, mergeLocations(startToken.location(), end));
    }

    private InnerStmtNode parseInnerStmt() {
        if (check(TokenKind.PATCH)) return parsePatchStmt();
        if (check(TokenKind.EFFECT)) return parseEffectStmt();
        if (check(TokenKind.WHEN)) return parseWhenStmt();
        if (check(TokenKind.ONCE)) return parseOnceStmt();
        if (isOnceIntentContext()) return parseOnceIntentStmt();
        if (check(TokenKind.FAIL)) return parseFailStmt();
        if (check(TokenKind.STOP)) return parseStopStmt();
        error("Unexpected token '" + peek().lexeme() + "'. Expected 'patch', 'effect', 'when', 'once', 'onceIntent', 'fail', or 'stop'.");
        advance();
        return null;
    }

    private PatchStmtNode parsePatchStmt() {
        SourceLocation start = consume(TokenKind.PATCH, "Expected 'patch'").location();
        PathNode path = parsePath();

        String op;
        ExprNode value = null;
        SourceLocation end;

        if (match(TokenKind.UNSET)) {
            op = "unset";
            end = previous().location();
        } else if (match(TokenKind.MERGE)) {
            op = "merge";
            value = parseExpression(Precedence.NONE);
            end = value.location();
        } else {
            consume(TokenKind.EQ, "Expected '=', 'unset', or 'merge' after path");
            op = "set";
            value = parseExpression(Precedence.NONE);
            end = value.location();
        }

        return new PatchStmtNode(path, op, value, mergeLocations(start, end));
    }

    private EffectStmtNode parseEffectStmt() {
        SourceLocation start = consume(TokenKind.EFFECT, "Expected 'effect'").location();
        String effectType = consume(TokenKind.IDENTIFIER, "Expected effect type").lexeme();
        while (match(TokenKind.DOT)) {
            effectType += "." + consume(TokenKind.IDENTIFIER, "Expected identifier after '.'").lexeme();
        }

        consume(TokenKind.LPAREN, "Expected '(' after effect type");
        consume(TokenKind.LBRACE, "Expected '{' for effect arguments");

        List<EffectArgNode> args = new ArrayList<>();
        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            args.add(parseEffectArg());
            match(TokenKind.COMMA);
        }

        consume(TokenKind.RBRACE, "Expected '}' after effect arguments");
        SourceLocation end = consume(TokenKind.RPAREN, "Expected ')' to close effect").location();
        return new EffectStmtNode(effectType, args, mergeLocations(start, end));
    }

    private EffectArgNode parseEffectArg() {
        Token nameToken = match(TokenKind.IDENTIFIER) ? previous() : null;
        if (nameToken == null && match(TokenKind.FAIL)) {
            nameToken = previous();
        }
        if (nameToken == null) {
            nameToken = consume(TokenKind.IDENTIFIER, "Expected argument name");
        }
        consume(TokenKind.COLON, "Expected ':' after argument name");
        boolean isPath = nameToken.lexeme().equals("into")
            || nameToken.lexeme().equals("pass")
            || nameToken.lexeme().equals("fail");
        AstNode value = isPath ? parsePath() : parseExpression(Precedence.NONE);
        return new EffectArgNode(nameToken.lexeme(), value, isPath, mergeLocations(nameToken.location(), value.location()));
    }

    private FailStmtNode parseFailStmt() {
        SourceLocation start = consume(TokenKind.FAIL, "Expected 'fail'").location();
        Token codeToken = consume(TokenKind.STRING, "Expected error code string after 'fail'");
        String code = stringValue(codeToken);
        ExprNode message = null;
        SourceLocation end = codeToken.location();
        if (match(TokenKind.WITH)) {
            message = parseExpression(Precedence.NONE);
            end = message.location();
        }
        return new FailStmtNode(code, message, mergeLocations(start, end));
    }

    private StopStmtNode parseStopStmt() {
        SourceLocation start = consume(TokenKind.STOP, "Expected 'stop'").location();
        Token reasonToken = consume(TokenKind.STRING, "Expected reason string after 'stop'");
        String reason = stringValue(reasonToken);
        return new StopStmtNode(reason, mergeLocations(start, reasonToken.location()));
    }

    // ========= Types =========

    private TypeExprNode parseTypeExpr() {
        TypeExprNode type = parseBaseType();
        if (check(TokenKind.PIPE)) {
            List<TypeExprNode> types = new ArrayList<>();
            types.add(type);
            while (match(TokenKind.PIPE)) {
                types.add(parseBaseType());
            }
            type = new UnionTypeNode(types, mergeLocations(types.get(0).location(), types.get(types.size() - 1).location()));
        }
        return type;
    }

    private TypeExprNode parseBaseType() {
        if (check(TokenKind.LBRACE)) {
            return parseObjectType();
        }

        if (check(TokenKind.STRING)) {
            Token token = advance();
            return new LiteralTypeNode(stringValue(token), token.location());
        }
        if (check(TokenKind.NUMBER)) {
            Token token = advance();
            return new LiteralTypeNode(numberValue(token), token.location());
        }
        if (check(TokenKind.TRUE) || check(TokenKind.FALSE)) {
            Token token = advance();
            return new LiteralTypeNode(token.kind() == TokenKind.TRUE, token.location());
        }
        if (check(TokenKind.NULL)) {
            Token token = advance();
            return new LiteralTypeNode(null, token.location());
        }

        Token nameToken = consume(TokenKind.IDENTIFIER, "Expected type name");
        if (match(TokenKind.LT)) {
            if ("Array".equals(nameToken.lexeme())) {
                TypeExprNode elementType = parseTypeExpr();
                SourceLocation end = consume(TokenKind.GT, "Expected '>' after array element type").location();
                return new ArrayTypeNode(elementType, mergeLocations(nameToken.location(), end));
            }
            if ("Record".equals(nameToken.lexeme())) {
                TypeExprNode keyType = parseTypeExpr();
                consume(TokenKind.COMMA, "Expected ',' between Record type parameters");
                TypeExprNode valueType = parseTypeExpr();
                SourceLocation end = consume(TokenKind.GT, "Expected '>' after Record value type").location();
                return new RecordTypeNode(keyType, valueType, mergeLocations(nameToken.location(), end));
            }
            error("Unknown generic type '" + nameToken.lexeme() + "'");
            while (!check(TokenKind.GT) && !isAtEnd()) {
                advance();
            }
            match(TokenKind.GT);
        }

        return new SimpleTypeNode(nameToken.lexeme(), nameToken.location());
    }

    private ObjectTypeNode parseObjectType() {
        SourceLocation start = consume(TokenKind.LBRACE, "Expected '{'").location();
        List<TypeFieldNode> fields = new ArrayList<>();

        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            Token nameToken = consume(TokenKind.IDENTIFIER, "Expected field name");
            boolean optional = match(TokenKind.QUESTION);
            consume(TokenKind.COLON, "Expected ':' after field name");
            TypeExprNode typeExpr = parseTypeExpr();
            fields.add(new TypeFieldNode(nameToken.lexeme(), typeExpr, optional, mergeLocations(nameToken.location(), typeExpr.location())));
            match(TokenKind.COMMA);
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close object type").location();
        return new ObjectTypeNode(fields, mergeLocations(start, end));
    }

    // ========= Expressions =========

    private ExprNode parseExpression(Precedence minPrecedence) {
        ExprNode left = parsePrimary();

        while (true) {
            Precedence precedence = Precedence.getBinaryPrecedence(peek().kind());
            if (precedence.level() <= minPrecedence.level()) {
                break;
            }

            if (peek().kind() == TokenKind.QUESTION) {
                left = parseTernary(left);
                continue;
            }

            String op = Precedence.tokenToBinaryOp(peek().kind());
            if (op == null) {
                break;
            }

            advance();
            Precedence nextPrecedence = Precedence.isRightAssociative(previous().kind())
                ? Precedence.values()[precedence.ordinal() - 1]
                : precedence;
            ExprNode right = parseExpression(nextPrecedence);

            left = new BinaryExprNode(op, left, right, mergeLocations(left.location(), right.location()));
        }

        return left;
    }

    private ExprNode parseTernary(ExprNode condition) {
        consume(TokenKind.QUESTION, "Expected '?'");
        ExprNode consequent = parseExpression(Precedence.NONE);
        consume(TokenKind.COLON, "Expected ':' in ternary expression");
        ExprNode alternate = parseExpression(Precedence.TERNARY);
        return new TernaryExprNode(condition, consequent, alternate, mergeLocations(condition.location(), alternate.location()));
    }

    private ExprNode parsePrimary() {
        if (check(TokenKind.ERROR)) {
            error("Unexpected token '" + peek().lexeme() + "'");
            advance();
            Token fallback = previous();
            return new LiteralExprNode(null, "null", fallback.location());
        }

        if (check(TokenKind.BANG) || (check(TokenKind.MINUS) && isUnaryContext())) {
            Token op = advance();
            ExprNode operand = parsePrimary();
            return new UnaryExprNode(op.kind() == TokenKind.BANG ? "!" : "-", operand, mergeLocations(op.location(), operand.location()));
        }

        if (match(TokenKind.LPAREN)) {
            ExprNode expr = parseExpression(Precedence.NONE);
            consume(TokenKind.RPAREN, "Expected ')' after expression");
            return expr;
        }

        if (check(TokenKind.LBRACE)) {
            return parseObjectLiteral();
        }

        if (check(TokenKind.LBRACKET)) {
            return parseArrayLiteral();
        }

        if (check(TokenKind.NUMBER)) {
            Token token = advance();
            return new LiteralExprNode(numberValue(token), "number", token.location());
        }

        if (check(TokenKind.STRING)) {
            Token token = advance();
            return new LiteralExprNode(stringValue(token), "string", token.location());
        }

        if (check(TokenKind.TRUE) || check(TokenKind.FALSE)) {
            Token token = advance();
            return new LiteralExprNode(token.kind() == TokenKind.TRUE, "boolean", token.location());
        }

        if (check(TokenKind.NULL)) {
            Token token = advance();
            return new LiteralExprNode(null, "null", token.location());
        }

        if (check(TokenKind.SYSTEM_IDENT)) {
            Token token = advance();
            String lexeme = token.lexeme();
            String[] parts = lexeme.substring(1).split("\\.");
            List<String> path = List.of(parts);
            return parsePostfix(new SystemIdentExprNode(path, token.location()));
        }

        if (check(TokenKind.ITEM)) {
            Token token = advance();
            return parsePostfix(new IterationVarExprNode("item", token.location()));
        }

        if (check(TokenKind.IDENTIFIER)) {
            Token token = advance();
            if (check(TokenKind.LPAREN)) {
                return parseFunctionCall(token);
            }
            return parsePostfix(new IdentifierExprNode(token.lexeme(), token.location()));
        }

        error("Unexpected token '" + peek().lexeme() + "'");
        Token fallback = peek();
        return new LiteralExprNode(null, "null", fallback.location());
    }

    private ExprNode parseFunctionCall(Token nameToken) {
        consume(TokenKind.LPAREN, "Expected '(' for function call");
        List<ExprNode> args = new ArrayList<>();
        if (!check(TokenKind.RPAREN)) {
            do {
                args.add(parseExpression(Precedence.NONE));
            } while (match(TokenKind.COMMA));
        }
        SourceLocation end = consume(TokenKind.RPAREN, "Expected ')' after arguments").location();
        return parsePostfix(new FunctionCallExprNode(nameToken.lexeme(), args, mergeLocations(nameToken.location(), end)));
    }

    private ExprNode parsePostfix(ExprNode expr) {
        while (true) {
            if (match(TokenKind.DOT)) {
                Token prop = consume(TokenKind.IDENTIFIER, "Expected property name after '.'");
                expr = new PropertyAccessExprNode(expr, prop.lexeme(), mergeLocations(expr.location(), prop.location()));
            } else if (match(TokenKind.LBRACKET)) {
                ExprNode index = parseExpression(Precedence.NONE);
                SourceLocation end = consume(TokenKind.RBRACKET, "Expected ']' after index").location();
                expr = new IndexAccessExprNode(expr, index, mergeLocations(expr.location(), end));
            } else {
                break;
            }
        }
        return expr;
    }

    private ExprNode parseObjectLiteral() {
        SourceLocation start = consume(TokenKind.LBRACE, "Expected '{'").location();
        List<ObjectPropertyNode> properties = new ArrayList<>();

        while (!check(TokenKind.RBRACE) && !isAtEnd()) {
            Token keyToken = consume(TokenKind.IDENTIFIER, "Expected property name");
            consume(TokenKind.COLON, "Expected ':' after property name");
            ExprNode value = parseExpression(Precedence.NONE);
            properties.add(new ObjectPropertyNode(keyToken.lexeme(), value, mergeLocations(keyToken.location(), value.location())));
            if (!check(TokenKind.RBRACE)) {
                consume(TokenKind.COMMA, "Expected ',' between properties");
            }
        }

        SourceLocation end = consume(TokenKind.RBRACE, "Expected '}' to close object").location();
        return new ObjectLiteralExprNode(properties, mergeLocations(start, end));
    }

    private ExprNode parseArrayLiteral() {
        SourceLocation start = consume(TokenKind.LBRACKET, "Expected '['").location();
        List<ExprNode> elements = new ArrayList<>();
        while (!check(TokenKind.RBRACKET) && !isAtEnd()) {
            elements.add(parseExpression(Precedence.NONE));
            if (!check(TokenKind.RBRACKET)) {
                consume(TokenKind.COMMA, "Expected ',' between elements");
            }
        }
        SourceLocation end = consume(TokenKind.RBRACKET, "Expected ']' to close array").location();
        return new ArrayLiteralExprNode(elements, mergeLocations(start, end));
    }

    // ========= Path =========

    private PathNode parsePath() {
        List<PathSegmentNode> segments = new ArrayList<>();
        SourceLocation start = peek().location();

        Token first = consume(TokenKind.IDENTIFIER, "Expected identifier");
        segments.add(new PropertySegmentNode(first.lexeme(), first.location()));

        while (true) {
            if (match(TokenKind.DOT)) {
                Token prop = consume(TokenKind.IDENTIFIER, "Expected property name");
                segments.add(new PropertySegmentNode(prop.lexeme(), prop.location()));
            } else if (match(TokenKind.LBRACKET)) {
                ExprNode index = parseExpression(Precedence.NONE);
                SourceLocation end = consume(TokenKind.RBRACKET, "Expected ']'").location();
                segments.add(new IndexSegmentNode(index, mergeLocations(index.location(), end)));
            } else {
                break;
            }
        }

        PathSegmentNode last = segments.get(segments.size() - 1);
        return new PathNode(segments, mergeLocations(start, last.location()));
    }

    // ========= Helpers =========

    private boolean isUnaryContext() {
        if (current == 0) return true;
        Token prev = previous();
        return switch (prev.kind()) {
            case LPAREN, LBRACKET, LBRACE, COMMA, COLON, EQ,
                PLUS, MINUS, STAR, SLASH, PERCENT,
                EQ_EQ, BANG_EQ, LT, LT_EQ, GT, GT_EQ,
                AMP_AMP, PIPE_PIPE, BANG, QUESTION, QUESTION_QUESTION -> true;
            default -> false;
        };
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token peekNext() {
        if (current + 1 >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(current + 1);
    }

    private boolean isAtEnd() {
        return peek().kind() == TokenKind.EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenKind kind) {
        if (isAtEnd()) return false;
        return peek().kind() == kind;
    }

    private boolean isOnceIntentContext() {
        if (!check(TokenKind.IDENTIFIER)) return false;
        Token token = peek();
        if (!"onceIntent".equals(token.lexeme())) return false;
        Token next = peekNext();
        return next.kind() == TokenKind.LBRACE || next.kind() == TokenKind.WHEN;
    }

    private boolean match(TokenKind... kinds) {
        for (TokenKind kind : kinds) {
            if (check(kind)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenKind kind, String message) {
        if (check(kind)) return advance();
        throw errorAtCurrent(message);
    }

    private void error(String message) {
        Token token = peek();
        diagnostics.add(Diagnostic.error(
            DiagnosticCode.MEL_PARSER,
            message,
            spanFrom(token)
        ));
    }

    private RuntimeException errorAtCurrent(String message) {
        Token token = peek();
        diagnostics.add(Diagnostic.error(
            DiagnosticCode.MEL_PARSER,
            message,
            spanFrom(token)
        ));
        return new RuntimeException(message);
    }

    private SourceSpan spanFrom(Token token) {
        SourceLocation loc = token.location();
        SourcePosition start = loc.start();
        int length = Math.max(1, token.lexeme() == null ? 1 : token.lexeme().length());
        return SourceSpan.of(start.line(), start.column(), length);
    }

    private String stringValue(Token token) {
        if (token.value() instanceof String) {
            return (String) token.value();
        }
        return token.lexeme();
    }

    private Number numberValue(Token token) {
        String lexeme = token.lexeme();
        if (token.value() instanceof Number) {
            Number value = (Number) token.value();
            if (value instanceof Double d) {
                boolean integerLexeme = lexeme != null
                    && !lexeme.contains(".")
                    && !lexeme.contains("e")
                    && !lexeme.contains("E");
                if (integerLexeme && d % 1 == 0) {
                    long longValue = d.longValue();
                    if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                        return (int) longValue;
                    }
                    return longValue;
                }
            }
            return value;
        }
        try {
            return lexeme.contains(".")
                ? Double.parseDouble(lexeme)
                : Integer.parseInt(lexeme);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static SourceLocation mergeLocations(SourceLocation start, SourceLocation end) {
        return SourceLocation.of(start.start(), end.end());
    }
}
