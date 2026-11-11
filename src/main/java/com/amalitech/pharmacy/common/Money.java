package com.amalitech.pharmacy.common;

import java.math.BigDecimal;

/**
 * An amount of money in Ghana cedis.
 *
 * <p>Backed by {@link BigDecimal} so decimal amounts are exact (a {@code double} cannot
 * represent most prices). The amount is normalised so that equal amounts are always
 * {@code equals} and print the same way, e.g. {@code 25.00} and {@code 25.0} both
 * print as {@code GHS 25.0}.
 *
 * @param amount the non-negative amount in cedis
 */
public record Money(BigDecimal amount) {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
        BigDecimal normalised = amount.stripTrailingZeros();
        if (normalised.scale() < 1) {
            normalised = normalised.setScale(1);
        }
        amount = normalised;
    }

    /** Creates an amount from text such as {@code "12.50"}. */
    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor));
    }

    @Override
    public String toString() {
        return "GHS " + amount.toPlainString();
    }
}
