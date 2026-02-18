package com.cbs.card.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Custom Logback PatternLayout that scrubs potential PANs (Permanent Account
 * Numbers)
 * from log messages to ensure PCI-DSS compliance.
 */
public class PanMaskingPatternLayout extends PatternLayout {

    @Override
    public String doLayout(ILoggingEvent event) {
        return mask(super.doLayout(event));
    }

    /**
     * Masks any 13-19 digit sequences found in the message, revealing only the last
     * 4 digits.
     */
    public String mask(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }
        // Regex to find 13-19 digit numbers (common PAN length)
        // and replace all but the last 4 digits with asterisks.
        return message.replaceAll("(?<!\\d)(\\d{12,15})(\\d{4})(?!\\d)", "************$2");
    }
}
