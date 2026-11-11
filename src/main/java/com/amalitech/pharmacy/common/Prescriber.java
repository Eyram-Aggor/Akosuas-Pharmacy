package com.amalitech.pharmacy.common;

/**
 * The person who authorised a prescription-only or controlled sale.
 *
 * <p>A blank name is rejected, so "no prescriber" can never be disguised as one.
 *
 * @param name the prescriber's name, e.g. {@code "Dr. Boateng"}
 */
public record Prescriber(String name) {

    public Prescriber {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Prescriber name is required");
        }
        name = name.strip();
    }

    @Override
    public String toString() {
        return name;
    }
}
