package com.amalitech.pharmacy.transaction;

/** One entry in the pharmacy's audit trail. Immutable once created. */
public sealed interface Transaction permits Dispensing, Restocking {

    int id();

    String medicationName();

    int quantity();
}
