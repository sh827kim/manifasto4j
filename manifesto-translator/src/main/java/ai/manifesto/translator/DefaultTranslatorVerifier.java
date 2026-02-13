package ai.manifesto.translator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * KR: 번역 초안의 최소 유효성(도메인/액션/입력)을 검증하는 기본 verify 구현입니다.
 * EN: Default verify implementation that checks minimum validity for domain/action/input.
 */
public final class DefaultTranslatorVerifier implements TranslatorVerifier {
    private static final Pattern ACTION_NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_.-]{1,63}$");

    @Override
    public TranslationDraft verify(TranslationRequest request, TranslationDraft draft) {
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(draft, "draft must not be null");

        List<String> diagnostics = new ArrayList<>();
        if (draft.diagnostics() != null) {
            diagnostics.addAll(draft.diagnostics());
        }

        String domainName = draft.domainName() == null ? "" : draft.domainName().trim();
        if (domainName.isBlank()) {
            diagnostics.add("TRV001: domainName must not be blank");
        }

        String actionName = draft.actionName() == null ? "" : draft.actionName().trim();
        if (actionName.isBlank() || "unknown".equals(actionName)) {
            diagnostics.add("TRV002: actionName is unresolved");
        } else if (!ACTION_NAME_PATTERN.matcher(actionName).matches()) {
            diagnostics.add("TRV005: actionName format is invalid");
        }

        if (request.messages() == null || request.messages().isEmpty()) {
            diagnostics.add("TRV003: request.messages must not be empty");
        } else {
            boolean hasUserMessage = request.messages().stream()
                .filter(Objects::nonNull)
                .anyMatch(message -> "user".equalsIgnoreCase(safe(message.role())));
            if (!hasUserMessage) {
                diagnostics.add("TRV004: at least one user message is required");
            }
        }

        Map<String, Object> input = new LinkedHashMap<>();
        if (draft.input() != null) {
            input.putAll(draft.input());
        }
        input.putIfAbsent("text", "");
        input.putIfAbsent("messageCount", request.messages() == null ? 0 : request.messages().size());
        String inputText = input.get("text") == null ? "" : String.valueOf(input.get("text"));
        if (safe(inputText).isBlank()) {
            diagnostics.add("TRV006: input.text must not be blank");
        }

        Map<String, Object> meta = new LinkedHashMap<>();
        if (draft.meta() != null) {
            meta.putAll(draft.meta());
        }
        boolean verified = diagnostics.stream().noneMatch(code -> code.startsWith("TRV"));
        meta.put("verified", verified);
        meta.put("verificationScore", computeScore(diagnostics));

        return new TranslationDraft(domainName, actionName, input, meta, List.copyOf(diagnostics));
    }

    private double computeScore(List<String> diagnostics) {
        long verifierIssues = diagnostics.stream().filter(code -> code.startsWith("TRV")).count();
        double score = 1.0d - (0.2d * verifierIssues);
        return Math.max(0.0d, score);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
