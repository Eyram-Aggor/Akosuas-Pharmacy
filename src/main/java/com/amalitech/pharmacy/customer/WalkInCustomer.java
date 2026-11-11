package com.amalitech.pharmacy.customer;

import com.amalitech.pharmacy.common.Money;

/** An anonymous walk-in: pays the full price. */
public final class WalkInCustomer extends Customer {

    public WalkInCustomer(String name) {
        super(name);
    }

    @Override
    public Money priceFor(Money price) {
        return price;
    }
}
