package com.urbanplate.billing.controller;

import com.urbanplate.billing.dto.PaymentRequestDTO;
import com.urbanplate.billing.dto.PaymentResponseDTO;
import com.urbanplate.billing.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // GET all payments - THIS WAS MISSING!
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        List<PaymentResponseDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // GET payment history - alias for getAllPayments
    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentHistory() {
        List<PaymentResponseDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(@Valid @RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO payment = paymentService.processPayment(request);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, String>> processRefund(
            @PathVariable Long paymentId,
            @RequestParam String reason,
            @RequestParam(required = false) BigDecimal amount) {

        paymentService.processRefund(paymentId, reason, amount);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Refund processed successfully");
        response.put("paymentId", paymentId.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/methods")
    public ResponseEntity<Map<String, List<String>>> getPaymentMethods() {
        Map<String, List<String>> response = new HashMap<>();
        response.put("methods", List.of("CASH", "CREDIT_CARD", "DEBIT_CARD", "PAYPAL", "QR_CODE", "MOBILE_MONEY", "DIGITAL_WALLET"));
        return ResponseEntity.ok(response);
    }
}