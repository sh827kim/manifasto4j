package ai.manifesto.world.types;

import ai.manifesto.core.utils.CanonicalUtils;
import ai.manifesto.core.utils.HashUtils;
import ai.manifesto.world.schema.IntentBody;
import ai.manifesto.world.schema.IntentScope;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class IntentKeys {
    private IntentKeys() {
    }

    public static String computeIntentKey(String schemaHash, IntentBody body) {
        Objects.requireNonNull(schemaHash, "schemaHash is required");
        Objects.requireNonNull(body, "body is required");

        String inputCanonical = CanonicalUtils.toCanonical(body.getInput());
        String scopeCanonical = CanonicalUtils.toCanonical(toScopeMap(body.getScopeProposal()));
        String source = schemaHash + ":" + body.getType() + ":" + inputCanonical + ":" + scopeCanonical;
        return HashUtils.sha256(source);
    }

    private static Map<String, Object> toScopeMap(IntentScope scope) {
        if (scope == null) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("paths", scope.getPaths());
        return map;
    }
}
