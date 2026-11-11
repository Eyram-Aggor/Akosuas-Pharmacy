package com.amalitech.pharmacy.service;

import java.util.List;

/** Snapshot of the end of the day. Pure data: how it is shown is the UI's concern. */
public record DayReport(String pharmacyName, List<StockLine> stock, int transactionCount) {

    public DayReport {
        stock = List.copyOf(stock);
    }

    /** One medicine's closing stock. */
    public record StockLine(String medicationName, String category, int inStock) {
    }
}
