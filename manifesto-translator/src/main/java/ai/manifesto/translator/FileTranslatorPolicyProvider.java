package ai.manifesto.translator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * KR: 파일 기반 정책 소스에서 translator 도메인 정책을 로드/재로딩하는 provider입니다.
 * EN: Provider that loads and reloads translator domain policies from a file-backed policy source.
 */
public final class FileTranslatorPolicyProvider implements TranslatorPolicyProvider {
    private final Path policyFilePath;
    private Map<String, TranslatorDomainPolicy> policiesByDomain = Map.of();

    public FileTranslatorPolicyProvider(Path policyFilePath) {
        this.policyFilePath = Objects.requireNonNull(policyFilePath, "policyFilePath must not be null");
        reload();
    }

    @Override
    public Optional<TranslatorDomainPolicy> findByDomain(String domainName) {
        if (domainName == null || domainName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(policiesByDomain.get(normalize(domainName)));
    }

    @Override
    public Map<String, TranslatorDomainPolicy> snapshot() {
        return Map.copyOf(policiesByDomain);
    }

    @Override
    public void reload() {
        Map<String, TranslatorDomainPolicy> loaded = parsePolicyFile(policyFilePath);
        policiesByDomain = Map.copyOf(loaded);
    }

    private Map<String, TranslatorDomainPolicy> parsePolicyFile(Path filePath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read translator policy file: " + filePath, e);
        }

        Map<String, String> rawByDomain = new LinkedHashMap<>();
        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("#")) {
                continue;
            }
            int equalIndex = trimmed.indexOf('=');
            if (equalIndex <= 0 || equalIndex >= trimmed.length() - 1) {
                throw new IllegalArgumentException("Invalid policy line format: " + trimmed);
            }
            String key = trimmed.substring(0, equalIndex).trim();
            String value = trimmed.substring(equalIndex + 1).trim();
            rawByDomain.put(key, value);
        }

        Map<String, TranslatorDomainPolicy> parsed = new LinkedHashMap<>();
        Set<String> domains = extractDomains(rawByDomain.keySet());
        for (String domain : domains) {
            String actionKey = domain + ".allowedActions";
            String contextKey = domain + ".requiredContextKeys";
            Set<String> actions = parseCommaSet(rawByDomain.get(actionKey));
            Set<String> contextKeys = parseCommaSet(rawByDomain.get(contextKey));
            parsed.put(normalize(domain), new TranslatorDomainPolicy(domain, actions, contextKeys));
        }
        return parsed;
    }

    private Set<String> extractDomains(Set<String> keys) {
        Set<String> domains = new LinkedHashSet<>();
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            int dot = key.indexOf('.');
            if (dot > 0) {
                domains.add(key.substring(0, dot));
            }
        }
        return domains;
    }

    private Set<String> parseCommaSet(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        Set<String> values = new LinkedHashSet<>();
        Arrays.stream(raw.split(","))
            .map(String::trim)
            .filter(value -> !value.isBlank())
            .forEach(values::add);
        return Set.copyOf(values);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
