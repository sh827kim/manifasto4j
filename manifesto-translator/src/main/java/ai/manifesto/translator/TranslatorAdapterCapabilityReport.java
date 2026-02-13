package ai.manifesto.translator;

import java.util.List;

/**
 * KR: 메시지 어댑터 계약 점검 결과를 담는 리포트입니다.
 * EN: Report containing capability validation results for a message adapter contract.
 */
public record TranslatorAdapterCapabilityReport(
    boolean translatorRoundTripPreserved,
    boolean externalRoundTripPreserved,
    List<String> diagnostics
) {
    public boolean isCompatible() {
        return translatorRoundTripPreserved && externalRoundTripPreserved && diagnostics.isEmpty();
    }
}
