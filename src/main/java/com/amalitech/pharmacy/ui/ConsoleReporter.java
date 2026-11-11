package com.amalitech.pharmacy.ui;

import com.amalitech.pharmacy.exception.DispenseException;
import com.amalitech.pharmacy.service.DayReport;
import com.amalitech.pharmacy.transaction.Dispensing;
import com.amalitech.pharmacy.transaction.Restocking;

import java.io.PrintStream;

/**
 * All console wording lives here, so the business rules never print anything and the
 * output can be tested by pointing this at an in-memory stream.
 */
public final class ConsoleReporter {

    private final PrintStream out;

    public ConsoleReporter(PrintStream out) {
        this.out = out;
    }

    public void dispensed(Dispensing d) {
        String prescribedBy = d.prescriber()
                .map(p -> " (prescribed by " + p + ")")
                .orElse("");
        out.println("Dispensed " + d.quantity() + " x " + d.medicationName()
                + " to " + d.customer().name() + prescribedBy + " for " + d.total());
    }

    public void restocked(Restocking r) {
        out.println("Restocked " + r.quantity() + " x " + r.medicationName());
    }

    public void refused(DispenseException e) {
        out.println(e.getMessage());
    }

    public void endOfDay(DayReport report) {
        out.println();
        out.println("=== " + report.pharmacyName() + " - end of day ===");
        out.println("Stock levels:");
        for (DayReport.StockLine line : report.stock()) {
            out.println("  " + line.medicationName() + " (" + line.category() + "): "
                    + line.inStock() + " in stock");
        }
        out.println("Transactions today: " + report.transactionCount());
    }
}
