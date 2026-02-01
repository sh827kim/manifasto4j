package ai.manifesto.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * LoweringContext - MEL lowering context constraints
 */
public final class LoweringContext {
    private final Set<String> allowSysPrefixes;
    private final boolean allowItem;
    private final String mode;

    private LoweringContext(Set<String> allowSysPrefixes, boolean allowItem, String mode) {
        this.allowSysPrefixes = allowSysPrefixes;
        this.allowItem = allowItem;
        this.mode = mode;
    }

    public static LoweringContext defaultContext() {
        return new LoweringContext(new HashSet<>(List.of("meta", "input")), false, "default");
    }

    public static LoweringContext effectArgsContext() {
        return new LoweringContext(new HashSet<>(List.of("meta", "input")), true, "effectArgs");
    }

    public Set<String> getAllowSysPrefixes() {
        return allowSysPrefixes;
    }

    public boolean isAllowItem() {
        return allowItem;
    }

    public String getMode() {
        return mode;
    }
}
