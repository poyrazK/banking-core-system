package com.cbs.common.security;

public final class CardNumberMasker {
    private CardNumberMasker() {
    }

    /**
     * Returns masked PAN showing only the last 4 digits. e.g., "************1234"
     */
    public static String mask(String pan) {
        if (pan == null || pan.isBlank()) {
            return "****";
        }

        String cleanPan = pan.trim().replace(" ", "");
        if (cleanPan.length() < 4) {
            return "****";
        }

        return "*".repeat(cleanPan.length() - 4) + cleanPan.substring(cleanPan.length() - 4);
    }
}
