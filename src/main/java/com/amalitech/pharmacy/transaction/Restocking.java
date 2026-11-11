package com.amalitech.pharmacy.transaction;

/** Stock delivered by a supplier. */
public record Restocking(int id, String medicationName, int quantity) implements Transaction {
}
