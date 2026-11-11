package com.amalitech.pharmacy.customer;

import com.amalitech.pharmacy.common.Money;

/**
 * Someone the pharmacy sells to. Each kind of customer owns its own pricing rule, so
 * the pharmacy never has to ask "what kind are you?".
 */
public abstract class Customer {

    private final String name;

    protected Customer(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        this.name = name.strip();
    }

    public String name() {
        return name;
    }

    /** The amount this customer pays for a sale that would otherwise cost {@code price}. */
    public abstract Money priceFor(Money price);
}
