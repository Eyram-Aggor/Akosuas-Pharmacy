package com.amalitech.pharmacy.exception;

/** More was requested than is on the shelf. */
public class InsufficientStockException extends DispenseException {

    public InsufficientStockException(String medicationName, int requested, int available) {
        super("Cannot dispense " + requested + " x " + medicationName
                + ": only " + available + " in stock");
    }
}
