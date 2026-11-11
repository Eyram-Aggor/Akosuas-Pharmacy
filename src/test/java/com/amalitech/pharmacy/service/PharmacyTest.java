package com.amalitech.pharmacy.service;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.RegisteredPatient;
import com.amalitech.pharmacy.customer.WalkInCustomer;
import com.amalitech.pharmacy.exception.InsufficientStockException;
import com.amalitech.pharmacy.exception.PrescriberRequiredException;
import com.amalitech.pharmacy.medication.Medication;
import com.amalitech.pharmacy.medication.OtcMedication;
import com.amalitech.pharmacy.medication.PrescriptionMedication;
import com.amalitech.pharmacy.transaction.Dispensing;
import com.amalitech.pharmacy.transaction.Restocking;
import com.amalitech.pharmacy.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PharmacyTest {

    private final Prescriber doctor = new Prescriber("Dr. Boateng");
    private final WalkInCustomer kofi = new WalkInCustomer("Kofi Mensah");
    private final RegisteredPatient ama = new RegisteredPatient("Ama Owusu", "0244 000 000");

    private Pharmacy pharmacy;
    private Medication paracetamol;
    private Medication amoxicillin;

    @BeforeEach
    void setUp() {
        pharmacy = new Pharmacy("Akosua's Pharmacy");
        paracetamol = new OtcMedication("Paracetamol 500mg", Money.of("12.50"), 100);
        amoxicillin = new PrescriptionMedication("Amoxicillin 250mg", Money.of("30.00"), 40);
        pharmacy.addMedication(paracetamol);
        pharmacy.addMedication(amoxicillin);
        pharmacy.addCustomer(kofi);
        pharmacy.addCustomer(ama);
    }

    @Test
    void walkInPaysFullPrice() throws Exception {
        Dispensing sale = pharmacy.dispense(kofi, paracetamol, 2);
        assertEquals(Money.of("25.0"), sale.total());
        assertEquals(98, paracetamol.stock());
    }

    @Test
    void registeredPatientGetsFivePercentOffOtc() throws Exception {
        assertEquals(Money.of("23.75"), pharmacy.dispense(ama, paracetamol, 2).total());
    }

    @Test
    void registeredPatientGetsFivePercentOffPrescription() throws Exception {
        assertEquals(Money.of("28.5"), pharmacy.dispense(ama, amoxicillin, 1, doctor).total());
    }

    @Test
    void prescriptionSaleRecordsThePrescriberInTheAuditTrail() throws Exception {
        pharmacy.dispense(ama, amoxicillin, 1, doctor);
        Dispensing recorded = (Dispensing) pharmacy.transactions().get(0);
        assertEquals(Optional.of(doctor), recorded.prescriber());
    }

    @Test
    void prescriptionWithoutPrescriberIsRefusedWithNoSideEffects() {
        assertThrows(PrescriberRequiredException.class, () -> pharmacy.dispense(kofi, amoxicillin, 1));
        assertEquals(40, amoxicillin.stock());
        assertTrue(pharmacy.transactions().isEmpty());
    }

    @Test
    void overSellingIsRefusedWithNoSideEffects() {
        assertThrows(InsufficientStockException.class,
                () -> pharmacy.dispense(kofi, amoxicillin, 100, doctor));
        assertEquals(40, amoxicillin.stock());
        assertTrue(pharmacy.transactions().isEmpty());
        assertTrue(pharmacy.historyOf(kofi).isEmpty());
    }

    @Test
    void stockCanNeverGoBelowZeroOverManySales() throws Exception {
        for (int i = 0; i < 40; i++) {
            pharmacy.dispense(ama, amoxicillin, 1, doctor);
        }
        assertEquals(0, amoxicillin.stock());
        assertThrows(InsufficientStockException.class, () -> pharmacy.dispense(ama, amoxicillin, 1, doctor));
        assertEquals(0, amoxicillin.stock());
    }

    @Test
    void negativeQuantityCannotMintStock() {
        assertThrows(IllegalArgumentException.class, () -> pharmacy.dispense(kofi, paracetamol, -5));
        assertEquals(100, paracetamol.stock());
    }

    @Test
    void restockAddsStockAndIsLogged() {
        Restocking restock = pharmacy.restock(paracetamol, 50);
        assertEquals(150, paracetamol.stock());
        assertEquals(50, restock.quantity());
        assertEquals(1, pharmacy.transactions().size());
    }

    @Test
    void medicineOrCustomerFromElsewhereIsRejected() {
        Medication stranger = new OtcMedication("Ibuprofen", Money.of("5"), 10);
        assertThrows(IllegalArgumentException.class, () -> pharmacy.dispense(kofi, stranger, 1));
        assertThrows(IllegalArgumentException.class, () -> pharmacy.restock(stranger, 1));
        assertThrows(IllegalArgumentException.class,
                () -> pharmacy.dispense(new WalkInCustomer("Stranger"), paracetamol, 1));
    }

    @Test
    void auditTrailCannotBeEditedFromOutside() throws Exception {
        pharmacy.dispense(kofi, paracetamol, 1);
        assertThrows(UnsupportedOperationException.class, () -> pharmacy.transactions().clear());
        assertEquals(1, pharmacy.transactions().size());
    }

    @Test
    void customerHistoryIsDerivedFromTheLog() throws Exception {
        pharmacy.dispense(kofi, paracetamol, 2);
        pharmacy.dispense(ama, amoxicillin, 1, doctor);
        assertEquals(1, pharmacy.historyOf(kofi).size());
        assertEquals("Got 2 x Paracetamol 500mg", pharmacy.historyOf(kofi).get(0).historyEntry());
    }

    @Test
    void transactionIdsAreSequentialPerPharmacy() throws Exception {
        pharmacy.dispense(kofi, paracetamol, 1);
        pharmacy.restock(paracetamol, 5);
        assertEquals(1, pharmacy.transactions().get(0).id());
        assertEquals(2, pharmacy.transactions().get(1).id());

        var other = new Pharmacy("Other");
        Medication med = new OtcMedication("X", Money.of("1"), 1);
        other.addMedication(med);
        assertEquals(1, other.restock(med, 1).id(), "ids are not shared global state");
    }

    @Test
    void registeredPatientRequiresAPhoneNumber() {
        assertThrows(IllegalArgumentException.class, () -> new RegisteredPatient("No Phone", " "));
    }

    @Test
    void reportListsStockAndTransactionCount() throws Exception {
        pharmacy.dispense(kofi, paracetamol, 2);
        DayReport report = pharmacy.endOfDayReport();
        assertEquals(2, report.stock().size());
        assertEquals(98, report.stock().get(0).inStock());
        assertEquals(1, report.transactionCount());
        Transaction first = pharmacy.transactions().get(0);
        assertEquals("Paracetamol 500mg", first.medicationName());
    }
}
