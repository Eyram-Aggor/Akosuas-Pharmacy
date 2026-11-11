package com.amalitech.pharmacy.medication;

import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.exception.DispenseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The medicines the pharmacy stocks, and the only code allowed to move their stock.
 *
 * <p>It lives in the same package as {@link Medication} so it can call the
 * package-private stock mutators. Code outside this package cannot change a count at all.
 */
public final class Inventory {

    private final List<Medication> medications = new ArrayList<>();

    public void add(Medication medication) {
        if (medication == null) {
            throw new IllegalArgumentException("Medication is required");
        }
        boolean nameTaken = medications.stream()
                .anyMatch(existing -> existing.name().equalsIgnoreCase(medication.name()));
        if (nameTaken) {
            throw new IllegalArgumentException("A medication named '" + medication.name()
                    + "' is already stocked");
        }
        medications.add(medication);
    }

    public boolean contains(Medication medication) {
        return medications.contains(medication);
    }

    /** A read-only view, in the order medicines were added. */
    public List<Medication> all() {
        return Collections.unmodifiableList(medications);
    }

    public Optional<Prescriber> dispense(Medication medication, int quantity,
                                         Optional<Prescriber> prescriber) throws DispenseException {
        requireStocked(medication);
        return medication.dispense(quantity, prescriber);
    }

    public void restock(Medication medication, int quantity) {
        requireStocked(medication);
        medication.restock(quantity);
    }

    private void requireStocked(Medication medication) {
        if (medication == null || !contains(medication)) {
            throw new IllegalArgumentException("Not a medication stocked by this pharmacy: "
                    + (medication == null ? "null" : medication.name()));
        }
    }
}
