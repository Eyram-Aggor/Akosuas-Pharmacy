package com.amalitech.pharmacy;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Behaviour-preservation evidence. The expected lines are the original program's output
 * (captured before any code changed and recorded in DIAGNOSIS.md), with the single
 * intended difference: the impossible 100-unit Amoxicillin sale is now refused.
 */
class PharmacyAppTest {

    @Test
    void demoDayMatchesTheOriginalOutputExceptTheRefusedSale() {
        var bytes = new ByteArrayOutputStream();
        PharmacyApp.run(new PrintStream(bytes, true, StandardCharsets.UTF_8));

        List<String> expected = List.of(
                "Dispensed 2 x Paracetamol 500mg to Kofi Mensah for GHS 25.0",
                "Dispensed 1 x Amoxicillin 250mg to Ama Owusu (prescribed by Dr. Boateng) for GHS 28.5",
                "Restocked 50 x Paracetamol 500mg",
                "Cannot dispense 100 x Amoxicillin 250mg: only 39 in stock", // was: dispensed, stock -61
                "",
                "=== Akosua's Pharmacy - end of day ===",
                "Stock levels:",
                "  Paracetamol 500mg (OTC): 148 in stock",
                "  Amoxicillin 250mg (PRESCRIPTION): 39 in stock", // was: -61
                "Transactions today: 3"); // was: 4

        assertEquals(expected, bytes.toString(StandardCharsets.UTF_8).lines().toList());
    }
}
