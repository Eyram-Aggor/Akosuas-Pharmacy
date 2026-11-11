package com.amalitech.pharmacy.exception;

/** The medicine can only be dispensed against a recorded prescriber. */
public class PrescriberRequiredException extends DispenseException {

    public PrescriberRequiredException(String medicationName) {
        super("Cannot dispense " + medicationName + " without a prescriber");
    }
}
