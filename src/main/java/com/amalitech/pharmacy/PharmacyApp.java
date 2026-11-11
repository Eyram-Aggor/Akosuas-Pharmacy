package com.amalitech.pharmacy;

import com.amalitech.pharmacy.common.Money;
import com.amalitech.pharmacy.common.Prescriber;
import com.amalitech.pharmacy.customer.RegisteredPatient;
import com.amalitech.pharmacy.customer.WalkInCustomer;
import com.amalitech.pharmacy.exception.DispenseException;
import com.amalitech.pharmacy.medication.Medication;
import com.amalitech.pharmacy.medication.OtcMedication;
import com.amalitech.pharmacy.medication.PrescriptionMedication;
import com.amalitech.pharmacy.service.Pharmacy;
import com.amalitech.pharmacy.ui.ConsoleReporter;

import java.io.PrintStream;

/** Runs the same day's scenario as the original {@code PharmacyTracker}. */
public final class PharmacyApp {

    private PharmacyApp() {
    }

    public static void main(String[] args) {
        run(System.out);
    }

    /** The demo day, writing to {@code out} so tests can capture it. */
    public static void run(PrintStream out) {
        var reporter = new ConsoleReporter(out);
        var pharmacy = new Pharmacy("Akosua's Pharmacy");

        Medication paracetamol = new OtcMedication("Paracetamol 500mg", Money.of("12.50"), 100);
        Medication amoxicillin = new PrescriptionMedication("Amoxicillin 250mg", Money.of("30.00"), 40);
        pharmacy.addMedication(paracetamol);
        pharmacy.addMedication(amoxicillin);

        var kofi = new WalkInCustomer("Kofi Mensah");
        var ama = new RegisteredPatient("Ama Owusu", "0244 000 000");
        pharmacy.addCustomer(kofi);
        pharmacy.addCustomer(ama);

        var drBoateng = new Prescriber("Dr. Boateng");

        try {
            // a normal morning
            reporter.dispensed(pharmacy.dispense(kofi, paracetamol, 2));
            reporter.dispensed(pharmacy.dispense(ama, amoxicillin, 1, drBoateng));
            reporter.restocked(pharmacy.restock(paracetamol, 50));
        } catch (DispenseException e) {
            reporter.refused(e);
        }

        // the sort of thing that keeps Akosua up at night: now refused, stock stays valid
        try {
            reporter.dispensed(pharmacy.dispense(kofi, amoxicillin, 100, drBoateng));
        } catch (DispenseException e) {
            reporter.refused(e);
        }

        reporter.endOfDay(pharmacy.endOfDayReport());
    }
}
