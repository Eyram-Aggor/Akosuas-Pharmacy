package com.amalitech.pharmacy.exception;

/**
 * A dispense was refused for a business reason. Checked, so every caller must decide
 * what to do with a refused sale. Nothing has changed (no stock moved, nothing logged)
 * when one of these is thrown.
 */
public class DispenseException extends Exception {

    public DispenseException(String message) {
        super(message);
    }
}
