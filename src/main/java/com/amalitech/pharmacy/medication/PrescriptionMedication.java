package com.amalitech.pharmacy.medication;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.exception.PrescriberRequiredException;

import java.util.Optional;

/** Prescription-only medicine: a prescriber must be supplied and is recorded on the sale. */
public final class PrescriptionMedication extends Medication {

    public PrescriptionMedication(String name, Money unitPrice, int initialStock) {
        super(name, unitPrice, initialStock);
    }

    @Override
    public String categoryLabel() {
        return "PRESCRIPTION";
    }

    @Override
    protected Optional<Prescriber> authorise(Optional<Prescriber> prescriber)
            throws PrescriberRequiredException {
        if (prescriber.isEmpty()) {
            throw new PrescriberRequiredException(name());
        }
        return prescriber;
    }
}
