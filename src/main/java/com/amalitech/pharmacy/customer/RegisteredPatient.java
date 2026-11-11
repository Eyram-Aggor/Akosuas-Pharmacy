package com.amalitech.pharmacy.customer;

import com.amalitech.pharmacy.common.Money;

import java.math.BigDecimal;

/** A registered patient: contact details are mandatory and they get 5% off. */
public final class RegisteredPatient extends Customer {

    private static final BigDecimal PRICE_FACTOR = new BigDecimal("0.95");

    private final String phone;

    public RegisteredPatient(String name, String phone) {
        super(name);
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("A registered patient needs a phone number");
        }
        this.phone = phone.strip();
    }

    public String phone() {
        return phone;
    }

    @Override
    public Money priceFor(Money price) {
        return price.times(PRICE_FACTOR);
    }
}
