package com.urbanplate.billing.utils;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Base64;

@Component
public class QRCodeGenerator {

    public String generateQRCode(String invoiceNumber, BigDecimal amount) {
        // For now, we'll create a simple text-based QR code representation
        // In a real application, you'd use a library like ZXing to generate actual QR codes

        String paymentData = String.format(
                "UPI://urbanplate?invoice=%s&amount=%.2f&currency=LKR",
                invoiceNumber,
                amount
        );

        // Encode as base64 for storage
        return Base64.getEncoder().encodeToString(paymentData.getBytes());
    }

    // If you want to generate actual QR code images, add this dependency to pom.xml:
    /*
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>core</artifactId>
        <version>3.5.2</version>
    </dependency>
    <dependency>
        <groupId>com.google.zxing</groupId>
        <artifactId>javase</artifactId>
        <version>3.5.2</version>
    </dependency>
    */
}