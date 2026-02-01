package ai.manifesto.builder;

import ai.manifesto.core.schema.ActionSpec;
import ai.manifesto.core.schema.ComputedFieldDef;
import ai.manifesto.core.schema.DomainSchema;
import ai.manifesto.core.schema.FieldSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DomainBuilder - Java용 DomainSchema 빌더 (최소 구현)
 */
public final class DomainBuilder {
    private final String id;
    private final String version;
    private final List<ActionSpec> actions = new ArrayList<>();
    private final List<FieldSpec> dataFields = new ArrayList<>();
    private final List<ComputedFieldDef> computedFields = new ArrayList<>();

    public DomainBuilder(String id, String version) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.version = Objects.requireNonNull(version, "version is required");
    }

    public DomainBuilder addAction(ActionSpec action) {
        actions.add(Objects.requireNonNull(action));
        return this;
    }

    public DomainBuilder addDataField(FieldSpec field) {
        dataFields.add(Objects.requireNonNull(field));
        return this;
    }

    public DomainBuilder addComputedField(ComputedFieldDef field) {
        computedFields.add(Objects.requireNonNull(field));
        return this;
    }

    public DomainSchema build(String hash) {
        DomainSchema.Builder builder = new DomainSchema.Builder(id, version).hash(hash);
        for (ActionSpec action : actions) {
            builder.addAction(action);
        }
        for (FieldSpec field : dataFields) {
            builder.addDataField(field);
        }
        for (ComputedFieldDef field : computedFields) {
            builder.addComputedField(field);
        }
        return builder.build();
    }
}
