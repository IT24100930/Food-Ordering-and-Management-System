package com.urbanplate.billing.utils;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InvoiceNumberGenerator {

    private static final String PREFIX = "INV";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(1);

    public String generateInvoiceNumber() {
        String date = LocalDateTime.now().format(DATE_FORMATTER);
        long sequence = counter.getAndIncrement();

        // Reset counter daily (optional)
        if (sequence > 9999) {
            counter.set(1);
            sequence = 1;
        }

        return String.format("%s-%s-%04d", PREFIX, date, sequence);
    }
}