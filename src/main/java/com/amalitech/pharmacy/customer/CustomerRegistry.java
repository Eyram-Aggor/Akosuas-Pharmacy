package com.amalitech.pharmacy.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The customers the pharmacy knows about. */
public final class CustomerRegistry {

    private final List<Customer> customers = new ArrayList<>();

    public void add(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (contains(customer)) {
            throw new IllegalArgumentException("Customer already registered: " + customer.name());
        }
        customers.add(customer);
    }

    public boolean contains(Customer customer) {
        return customers.contains(customer);
    }

    /** A read-only view. */
    public List<Customer> all() {
        return Collections.unmodifiableList(customers);
    }
}
