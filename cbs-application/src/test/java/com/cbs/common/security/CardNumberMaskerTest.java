package com.cbs.common.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardNumberMaskerTest {

    @ParameterizedTest
    @CsvSource({
            "4111111111111111, ************1111",
            "12345678, ****5678",
            "1234, 1234",
            "'  4111 1111 1111 1111  ', ************1111"
    })
    void mask_returnsExpectedMaskedValue(String input, String expected) {
        assertEquals(expected, CardNumberMasker.mask(input));
    }

    @Test
    void mask_returnsPlaceholderForShortOrNullInput() {
        assertEquals("****", CardNumberMasker.mask(null));
        assertEquals("****", CardNumberMasker.mask(""));
        assertEquals("****", CardNumberMasker.mask("   "));
        assertEquals("****", CardNumberMasker.mask("123"));
    }
}
