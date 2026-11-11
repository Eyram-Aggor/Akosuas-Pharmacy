package com.amalitech.pharmacy.service;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.Customer;
import com.amalitech.pharmacy.customer.CustomerRegistry;
import com.amalitech.pharmacy.exception.DispenseException;
import com.amalitech.pharmacy.medication.Inventory;
import com.amalitech.pharmacy.medication.Medication;
import com.amalitech.pharmacy.transaction.Dispensing;
import com.amalitech.pharmacy.transaction.Restocking;
import com.amalitech.pharmacy.transaction.Transaction;
import com.amalitech.pharmacy.transaction.TransactionLog;

import java.util.List;
import java.util.Optional;

/**
 * Coordinates a sale: it asks the medicine whether it may be dispensed, the customer what
 * they pay, and records the result. It holds <em>no</em> rules about specific kinds of
 * medicine or customer, which is why new kinds can be added without editing it.
 *
 * <p>Everything is validated before anything changes, so a refused sale leaves stock and
 * the audit log exactly as they were.
 */
public final class Pharmacy {

    private final String name;
    private final Inventory inventory = new Inventory();
    private final CustomerRegistry customers = new CustomerRegistry();
    private final TransactionLog log = new TransactionLog();

    public Pharmacy(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pharmacy name is required");
        }
        this.name = name.strip();
    }

    public void addMedication(Medication medication) {
        inventory.add(medication);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    /** Dispenses a medicine that needs no prescriber. */
    public Dispensing dispense(Customer customer, Medication medication, int quantity)
            throws DispenseException {
        return dispense(customer, medication, quantity, Optional.empty());
    }

    /** Dispenses a medicine against a prescriber. */
    public Dispensing dispense(Customer customer, Medication medication, int quantity,
                               Prescriber prescriber) throws DispenseException {
        return dispense(customer, medication, quantity, Optional.of(prescriber));
    }

    private Dispensing dispense(Customer customer, Medication medication, int quantity,
                                Optional<Prescriber> prescriber) throws DispenseException {
        if (customer == null || !customers.contains(customer)) {
            throw new IllegalArgumentException("Not a customer of this pharmacy");
        }
        // The medicine's own rules run first; they throw before any stock moves.
        Optional<Prescriber> recorded = inventory.dispense(medication, quantity, prescriber);
        Money total = customer.priceFor(medication.unitPrice().times(quantity));
        return log.recordDispensing(customer, medication.name(), quantity, total, recorded);
    }

    public Restocking restock(Medication medication, int quantity) {
        inventory.restock(medication, quantity);
        return log.recordRestocking(medication.name(), quantity);
    }

    /** Read-only audit trail. */
    public List<Transaction> transactions() {
        return log.all();
    }

    /** Everything dispensed to one customer. */
    public List<Dispensing> historyOf(Customer customer) {
        return log.dispensedTo(customer);
    }

    public DayReport endOfDayReport() {
        List<DayReport.StockLine> lines = inventory.all().stream()
                .map(m -> new DayReport.StockLine(m.name(), m.categoryLabel(), m.stock()))
                .toList();
        return new DayReport(name, lines, log.all().size());
    }
}
