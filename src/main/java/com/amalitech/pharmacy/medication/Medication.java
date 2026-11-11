package com.amalitech.pharmacy.medication;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.exception.DispenseException;
import com.amalitech.pharmacy.exception.InsufficientStockException;
import com.amalitech.pharmacy.exception.PrescriberRequiredException;

import java.util.Optional;

/**
 * Something the pharmacy stocks. Each kind of medicine is a subclass that decides for
 * itself what it needs before it can be dispensed ({@link #authorise}).
 *
 * <p><b>Invariant:</b> the stock count is private and can only change through
 * {@link #dispense} and {@link #restock}, which are package-private (only
 * {@link Inventory} calls them). Neither can leave the count below zero.
 *
 * <p>To add a new kind of medicine, extend this class and implement the two abstract
 * methods. Nothing else needs editing.
 */
public abstract class Medication {

    private final String name;
    private final Money unitPrice;
    private int stock;

    protected Medication(String name, Money unitPrice, int initialStock) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Medication name is required");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative: " + initialStock);
        }
        this.name = name.strip();
        this.unitPrice = unitPrice;
        this.stock = initialStock;
    }

    public String name() {
        return name;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public int stock() {
        return stock;
    }

    /** Label shown in the stock report, e.g. {@code "OTC"}. */
    public abstract String categoryLabel();

    /**
     * Checks that this medicine may be dispensed with what the caller supplied.
     *
     * @param prescriber who prescribed it, if anyone
     * @return the prescriber that must be recorded against the sale (empty if none applies)
     * @throws PrescriberRequiredException if this kind of medicine needs a prescriber and none was given
     */
    protected abstract Optional<Prescriber> authorise(Optional<Prescriber> prescriber)
            throws PrescriberRequiredException;

    /** Removes stock for a sale. All checks happen before the count is touched. */
    final Optional<Prescriber> dispense(int quantity, Optional<Prescriber> prescriber)
            throws DispenseException {
        Optional<Prescriber> toRecord = authorise(prescriber);
        requirePositive(quantity);
        if (quantity > stock) {
            throw new InsufficientStockException(name, quantity, stock);
        }
        stock -= quantity;
        return toRecord;
    }

    /** Adds delivered stock. */
    final void restock(int quantity) {
        requirePositive(quantity);
        stock = Math.addExact(stock, quantity);
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero: " + quantity);
        }
    }
}
