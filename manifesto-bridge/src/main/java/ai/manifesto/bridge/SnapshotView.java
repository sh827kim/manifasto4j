package ai.manifesto.bridge;

import java.util.Map;

/**
 * SnapshotView - projection용 읽기 전용 뷰
 */
public record SnapshotView(Map<String, Object> data, Map<String, Object> computed) {}
