package com.amalitech.pharmacy.service;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.WalkInCustomer;
import com.amalitech.pharmacy.exception.PrescriberRequiredException;
import com.amalitech.pharmacy.medication.Medication;
import com.amalitech.pharmacy.transaction.Dispensing;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proof that the coming change is cheap: a controlled substance is added here as ONE new
 * class, living outside the production packages. Not a single line of {@code Pharmacy},
 * {@code Inventory}, {@code OtcMedication}, {@code PrescriptionMedication} or the
 * reporter is touched, yet it is stock-safe, needs a prescriber, records the prescriber,
 * and appears in the report.
 */
class ControlledSubstanceExtensionTest {

    /** What the real class would look like. */
    static final class ControlledMedication extends Medication {

        ControlledMedication(String name, Money unitPrice, int initialStock) {
            super(name, unitPrice, initialStock);
        }

        @Override
        public String categoryLabel() {
            return "CONTROLLED";
        }

        @Override
        protected Optional<Prescriber> authorise(Optional<Prescriber> prescriber)
                throws PrescriberRequiredException {
            if (prescriber.isEmpty()) {
                throw new PrescriberRequiredException(name());
            }
            return prescriber; // recorded on every dispense
        }
    }

    private final Prescriber doctor = new Prescriber("Dr. Boateng");

    @Test
    void controlledMedicineWorksThroughTheExistingPharmacyUnchanged() throws Exception {
        var pharmacy = new Pharmacy("Akosua's Pharmacy");
        var customer = new WalkInCustomer("Kofi Mensah");
        var morphine = new ControlledMedication("Morphine 10mg", Money.of("45.00"), 5);
        pharmacy.addMedication(morphine);
        pharmacy.addCustomer(customer);

        assertThrows(PrescriberRequiredException.class, () -> pharmacy.dispense(customer, morphine, 1));

        Dispensing sale = pharmacy.dispense(customer, morphine, 2, doctor);
        assertEquals(Optional.of(doctor), sale.prescriber());
        assertEquals(3, morphine.stock());
        assertEquals("CONTROLLED", pharmacy.endOfDayReport().stock().get(0).category());
    }
}
