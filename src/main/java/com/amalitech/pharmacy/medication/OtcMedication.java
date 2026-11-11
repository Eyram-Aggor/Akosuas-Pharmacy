package com.amalitech.pharmacy.medication;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;

import java.util.Optional;

/** Over-the-counter medicine: anyone can buy it, no prescriber is needed or recorded. */
public final class OtcMedication extends Medication {

    public OtcMedication(String name, Money unitPrice, int initialStock) {
        super(name, unitPrice, initialStock);
    }

    @Override
    public String categoryLabel() {
        return "OTC";
    }

    @Override
    protected Optional<Prescriber> authorise(Optional<Prescriber> prescriber) {
        return Optional.empty();
    }
}
