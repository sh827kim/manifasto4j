package ai.manifesto.core.trace;

import ai.manifesto.core.TraceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * TraceRecorder - TraceNode 생성 헬퍼
 *
 * TraceContext를 받아 결정론적 ID와 타임스탬프를 일관되게 부여한다.
 */
public final class TraceRecorder {

    private final TraceContext trace;

    private TraceRecorder(TraceContext trace) {
        this.trace = Objects.requireNonNull(trace, "trace is required");
    }

    public static TraceRecorder create(TraceContext trace) {
        return new TraceRecorder(trace);
    }

    /**
     * TraceNode 생성
     */
    public TraceNode record(
        TraceNode.Kind kind,
        String sourcePath,
        Map<String, Object> inputs,
        Object output,
        List<TraceNode> children
    ) {
        return TraceNode.builder()
            .id(trace.nextId())
            .kind(kind)
            .sourcePath(sourcePath)
            .inputs(inputs != null ? inputs : new HashMap<>())
            .output(output)
            .children(children != null ? children : new ArrayList<>())
            .timestamp(trace.getTimestamp())
            .build();
    }

    /**
     * 입력/자식 없이 TraceNode 생성
     */
    public TraceNode record(
        TraceNode.Kind kind,
        String sourcePath,
        Object output
    ) {
        return record(kind, sourcePath, new HashMap<>(), output, new ArrayList<>());
    }
}
