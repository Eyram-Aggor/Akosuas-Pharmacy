package com.amalitech.pharmacy.transaction;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.Customer;

import java.util.Optional;

/**
 * Medicine handed to a customer. Records the prescriber whenever one applied, which is
 * what makes prescriptions (and later controlled substances) auditable.
 */
public record Dispensing(int id, Customer customer, String medicationName, int quantity,
                         Money total, Optional<Prescriber> prescriber) implements Transaction {

    /** The line kept in a customer's purchase history. */
    public String historyEntry() {
        return "Got " + quantity + " x " + medicationName;
    }
}
