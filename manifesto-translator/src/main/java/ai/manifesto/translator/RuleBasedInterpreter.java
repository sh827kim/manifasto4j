package ai.manifesto.translator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KR: actionHint/메시지 규칙을 기반으로 Intent 초안을 만드는 기본 interpret 구현입니다.
 * EN: Default interpret implementation that builds intent drafts from actionHint/message rules.
 */
public final class RuleBasedInterpreter implements TranslatorInterpreter {
    private static final Pattern ACTION_PATTERN = Pattern.compile("action\\s*:\\s*([A-Za-z0-9_.-]+)");

    @Override
    public TranslationDraft interpret(TranslationRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        List<String> diagnostics = new ArrayList<>();

        TranslatorMessage lastUserMessage = findLastUserMessage(request.messages());
        if (lastUserMessage == null) {
            diagnostics.add("TRI001: user message is missing");
        }

        String actionName = resolveActionName(request.actionHint(), lastUserMessage);
        if (actionName == null || actionName.isBlank()) {
            actionName = "unknown";
            diagnostics.add("TRI002: action could not be inferred");
        }

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("text", lastUserMessage != null ? safeString(lastUserMessage.content()) : "");
        input.put("messageCount", request.messages() == null ? 0 : request.messages().size());

        Map<String, Object> meta = new LinkedHashMap<>();
        if (request.context() != null && !request.context().isEmpty()) {
            meta.putAll(request.context());
        }
        meta.putIfAbsent("source", "translator-rule-based");

        return new TranslationDraft(
            safeString(request.domainName()),
            actionName,
            input,
            meta,
            diagnostics
        );
    }

    private TranslatorMessage findLastUserMessage(List<TranslatorMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }
        for (int i = messages.size() - 1; i >= 0; i--) {
            TranslatorMessage message = messages.get(i);
            if (message != null && "user".equalsIgnoreCase(safeString(message.role()))) {
                return message;
            }
        }
        return null;
    }

    private String resolveActionName(String actionHint, TranslatorMessage lastUserMessage) {
        if (actionHint != null && !actionHint.isBlank()) {
            return actionHint.trim();
        }
        if (lastUserMessage == null || lastUserMessage.content() == null) {
            return null;
        }
        Matcher matcher = ACTION_PATTERN.matcher(lastUserMessage.content());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
