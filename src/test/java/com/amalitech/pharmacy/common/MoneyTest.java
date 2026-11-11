package com.amalitech.pharmacy.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void printsLikeTheOriginalDoubleOutput() {
        assertEquals("GHS 25.0", Money.of("12.50").times(2).toString());
        assertEquals("GHS 28.5", Money.of("30.00").times(1).times(new BigDecimal("0.95")).toString());
        assertEquals("GHS 3000.0", Money.of("30.00").times(100).toString());
    }

    @Test
    void equalAmountsAreEqualRegardlessOfScale() {
        assertEquals(Money.of("25.00"), Money.of("25.0"));
        assertEquals(Money.of("25"), Money.of("25.000"));
    }

    @Test
    void decimalArithmeticIsExact() {
        assertEquals(Money.of("0.3"), Money.of("0.1").times(3));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("-1"));
        assertThrows(IllegalArgumentException.class, () -> Money.of("1").times(-1));
    }
}
