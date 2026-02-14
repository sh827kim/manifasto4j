package ai.manifesto.translator.core;

/**
 * KR: 원문 텍스트 상의 구간(offset)입니다.
 * EN: Offset range in source text.
 */
public record Span(int start, int end) {
    public Span {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid span range");
        }
    }

    public boolean overlaps(Span other) {
        return other != null && this.start < other.end && other.start < this.end;
    }
}
