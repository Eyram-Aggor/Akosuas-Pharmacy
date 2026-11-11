package com.amalitech.pharmacy.transaction;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.Customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Append-only record of everything that happened. Callers only ever get a read-only view. */
public final class TransactionLog {

    private final List<Transaction> entries = new ArrayList<>();
    private int nextId = 1;

    public Dispensing recordDispensing(Customer customer, String medicationName, int quantity,
                                       Money total, Optional<Prescriber> prescriber) {
        Dispensing entry = new Dispensing(nextId++, customer, medicationName, quantity, total, prescriber);
        entries.add(entry);
        return entry;
    }

    public Restocking recordRestocking(String medicationName, int quantity) {
        Restocking entry = new Restocking(nextId++, medicationName, quantity);
        entries.add(entry);
        return entry;
    }

    /** A read-only view; attempts to change it throw {@link UnsupportedOperationException}. */
    public List<Transaction> all() {
        return Collections.unmodifiableList(entries);
    }

    /** Everything dispensed to one customer, oldest first. Derived from the log, never stored twice. */
    public List<Dispensing> dispensedTo(Customer customer) {
        return entries.stream()
                .filter(Dispensing.class::isInstance)
                .map(Dispensing.class::cast)
                .filter(dispensing -> dispensing.customer().equals(customer))
                .toList();
    }
}
