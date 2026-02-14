package ai.manifesto.translator.targets;

import ai.manifesto.translator.core.Chunk;
import ai.manifesto.translator.core.IntentGraph;

import java.util.List;

/**
 * KR: target exporter 입력 모델입니다.
 * EN: Input model for target exporters.
 */
public record ExportInput(
    IntentGraph graph,
    List<String> diagnostics,
    SourceInfo source
) {
    public ExportInput {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    /**
     * KR: exporter traceback에 사용할 소스 메타 정보입니다.
     * EN: Source metadata used for exporter traceback.
     */
    public record SourceInfo(
        String text,
        List<Chunk> chunks
    ) {
        public SourceInfo {
            chunks = chunks == null ? List.of() : List.copyOf(chunks);
        }
    }
}
