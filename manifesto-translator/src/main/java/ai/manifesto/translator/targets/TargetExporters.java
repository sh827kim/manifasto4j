package ai.manifesto.translator.targets;

import java.util.Objects;

/**
 * KR: target exporter 실행 헬퍼입니다.
 * EN: Helper utilities for invoking target exporters.
 */
public final class TargetExporters {
    private TargetExporters() {
    }

    public static <TOut, TCtx> TOut exportTo(TargetExporter<TOut, TCtx> exporter, ExportInput input, TCtx context) {
        Objects.requireNonNull(exporter, "exporter must not be null");
        Objects.requireNonNull(input, "input must not be null");
        return exporter.export(input, context);
    }
}
