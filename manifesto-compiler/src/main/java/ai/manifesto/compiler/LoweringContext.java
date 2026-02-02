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
    private final String fnTableVersion;

    private LoweringContext(Set<String> allowSysPrefixes, boolean allowItem, String mode, String fnTableVersion) {
        this.allowSysPrefixes = allowSysPrefixes;
        this.allowItem = allowItem;
        this.mode = mode;
        this.fnTableVersion = fnTableVersion;
    }

    public static LoweringContext defaultSchemaContext() {
        return new LoweringContext(new HashSet<>(List.of("meta", "input")), false, "schema", "1.0");
    }

    public static LoweringContext defaultActionContext() {
        return new LoweringContext(new HashSet<>(List.of("meta", "input")), false, "action", "1.0");
    }

    public static LoweringContext effectArgsContext() {
        return new LoweringContext(new HashSet<>(List.of("meta", "input")), true, "action", "1.0");
    }

    public static LoweringContext fromPatchOptions(CompilePatchOptions options) {
        if (options == null) {
            return defaultActionContext();
        }
        Set<String> prefixes = options.allowSysPrefixes() == null || options.allowSysPrefixes().isEmpty()
            ? new HashSet<>(List.of("meta", "input"))
            : new HashSet<>(options.allowSysPrefixes());
        String version = options.fnTableVersion() == null ? "1.0" : options.fnTableVersion();
        return new LoweringContext(prefixes, false, "action", version);
    }

    public LoweringContext withModeAndAllowItem(String mode, boolean allowItem) {
        return new LoweringContext(new HashSet<>(allowSysPrefixes), allowItem, mode, fnTableVersion);
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

    public String getFnTableVersion() {
        return fnTableVersion;
    }
}
