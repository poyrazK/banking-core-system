package com.cbs.common.security;

/**
 * Utility class for masking credit card numbers (PANs) to maintain PCI-DSS
 * compliance.
 */
public final class CardNumberMasker {

    private CardNumberMasker() {
        // Utility class
    }

    /**
     * Masks a card number, revealing only the last 4 digits.
     * Format: ************1234
     */
    public static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) {
            return cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "*".repeat(cardNumber.length() - 4) + lastFour;
    }
}
