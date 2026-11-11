package com.amalitech.pharmacy.medication;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.exception.InsufficientStockException;
import com.amalitech.pharmacy.exception.PrescriberRequiredException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The "stock can never be invalid" rule, tested at the level of a single medicine. */
class MedicationStockTest {

    private final Prescriber doctor = new Prescriber("Dr. Boateng");

    private OtcMedication otc(int stock) {
        return new OtcMedication("Paracetamol 500mg", Money.of("12.50"), stock);
    }

    private PrescriptionMedication prescription(int stock) {
        return new PrescriptionMedication("Amoxicillin 250mg", Money.of("30.00"), stock);
    }

    @Test
    void dispensingReducesStock() throws Exception {
        var med = otc(10);
        med.dispense(4, Optional.empty());
        assertEquals(6, med.stock());
    }

    @Test
    void dispensingExactlyWhatIsLeftReachesZero() throws Exception {
        var med = otc(5);
        med.dispense(5, Optional.empty());
        assertEquals(0, med.stock());
    }

    @Test
    void dispensingMoreThanInStockIsRefusedAndStockIsUntouched() {
        var med = prescription(39);
        assertThrows(InsufficientStockException.class, () -> med.dispense(100, Optional.of(doctor)));
        assertEquals(39, med.stock());
    }

    @Test
    void zeroAndNegativeQuantitiesAreRejected() {
        var med = otc(10);
        assertThrows(IllegalArgumentException.class, () -> med.dispense(0, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> med.dispense(-5, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () -> med.restock(0));
        assertThrows(IllegalArgumentException.class, () -> med.restock(-5));
        assertEquals(10, med.stock());
    }

    @Test
    void cannotBeCreatedWithNegativeStock() {
        assertThrows(IllegalArgumentException.class, () -> otc(-1));
    }

    @Test
    void prescriptionMedicineNeedsAPrescriberAndStockIsUntouchedWithout() {
        var med = prescription(10);
        assertThrows(PrescriberRequiredException.class, () -> med.dispense(1, Optional.empty()));
        assertEquals(10, med.stock());
    }

    @Test
    void prescriptionMedicineReturnsThePrescriberToRecord() throws Exception {
        var med = prescription(10);
        assertEquals(Optional.of(doctor), med.dispense(1, Optional.of(doctor)));
    }

    @Test
    void otcMedicineDoesNotRecordAPrescriber() throws Exception {
        var med = otc(10);
        assertTrue(med.dispense(1, Optional.of(doctor)).isEmpty());
    }

    @Test
    void blankPrescriberIsNotAPrescriber() {
        assertThrows(IllegalArgumentException.class, () -> new Prescriber("   "));
        assertThrows(IllegalArgumentException.class, () -> new Prescriber(null));
    }
}
