package ai.manifesto.compiler.analyzer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KR: Scope는 컴파일러 분석 계층에서 scope 역할을 수행하는 구현 타입입니다.
 * EN: Scope is an implementation type performing scope roles in the compiler analyzer layer.
 */
public final class Scope {
    private final Scope parent;
    private final String kind;
    private final Map<String, Symbol> symbols = new LinkedHashMap<>();

    public Scope(String kind, Scope parent) {
        this.kind = kind;
        this.parent = parent;
    }

    public String kind() {
        return kind;
    }

    public Scope parent() {
        return parent;
    }

    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.name())) {
            return false;
        }
        symbols.put(symbol.name(), symbol);
        return true;
    }

    public Symbol lookup(String name) {
        Symbol symbol = symbols.get(name);
        if (symbol != null) {
            return symbol;
        }
        return parent != null ? parent.lookup(name) : null;
    }

    public boolean isDefinedHere(String name) {
        return symbols.containsKey(name);
    }
}
