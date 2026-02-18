package com.cbs.card.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Logback layout that masks sequences of 13-19 digits that look like
 * PANs.
 */
public class PanMaskingPatternLayout extends PatternLayout {

    private static final Pattern PAN_PATTERN = Pattern.compile("(?<!\\d)\\d{13,19}(?!\\d)");

    @Override
    public String doLayout(ILoggingEvent event) {
        return mask(super.doLayout(event));
    }

    public String mask(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }

        Matcher matcher = PAN_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        int last = 0;

        while (matcher.find()) {
            sb.append(message, last, matcher.start());
            String pan = matcher.group();
            String cleanPan = pan.replace(" ", "").replace("-", "");

            // Basic Luhn check could be added here for higher accuracy,
            // but for logging simple digit matching is usually enough to prevent leaks.
            if (cleanPan.length() >= 13 && cleanPan.length() <= 19) {
                sb.append("*".repeat(pan.length() - 4)).append(pan.substring(pan.length() - 4));
            } else {
                sb.append(pan);
            }
            last = matcher.end();
        }
        sb.append(message.substring(last));
        return sb.toString();
    }
}
