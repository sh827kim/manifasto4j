package ai.manifesto.compiler;

import java.util.List;

/**
 * CompilePatchOptions - patch compile options
 */
public record CompilePatchOptions(
    String actionName,
    List<String> allowSysPrefixes,
    String fnTableVersion
) {}
