package ai.manifesto.world.schema;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class PolicyCondition {
    private final PolicyConditionKind kind;
    private final Set<String> types;
    private final String pattern;
    private final String evaluator;

    private PolicyCondition(PolicyConditionKind kind, Set<String> types, String pattern, String evaluator) {
        this.kind = Objects.requireNonNull(kind, "kind is required");
        this.types = Collections.unmodifiableSet(new LinkedHashSet<>(types != null ? types : Set.of()));
        this.pattern = pattern;
        this.evaluator = evaluator;
    }

    public static PolicyCondition intentType(Set<String> types) {
        return new PolicyCondition(PolicyConditionKind.INTENT_TYPE, types, null, null);
    }

    public static PolicyCondition scopePattern(String pattern) {
        return new PolicyCondition(PolicyConditionKind.SCOPE_PATTERN, Set.of(), Objects.requireNonNull(pattern, "pattern is required"), null);
    }

    public static PolicyCondition custom(String evaluator) {
        return new PolicyCondition(PolicyConditionKind.CUSTOM, Set.of(), null, Objects.requireNonNull(evaluator, "evaluator is required"));
    }

    public PolicyConditionKind getKind() {
        return kind;
    }

    public Set<String> getTypes() {
        return types;
    }

    public String getPattern() {
        return pattern;
    }

    public String getEvaluator() {
        return evaluator;
    }
}
