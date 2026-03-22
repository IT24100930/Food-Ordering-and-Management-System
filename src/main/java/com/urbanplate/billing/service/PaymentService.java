package com.urbanplate.billing.service;

import com.urbanplate.billing.dto.PaymentRequestDTO;
import com.urbanplate.billing.dto.PaymentResponseDTO;
import com.urbanplate.billing.exception.ResourceNotFoundException;
import com.urbanplate.billing.model.*;
import com.urbanplate.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final TransactionLogRepository transactionLogRepository;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        log.info("Processing payment for invoice: {}", request.getInvoiceId());

        // Validate payment method
        if (request.getMethod() == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // Get invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        // Check if invoice is already paid
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice is already paid");
        }

        // Check if invoice is voided
        if (invoice.getStatus() == Invoice.InvoiceStatus.VOID) {
            throw new IllegalStateException("Cannot process payment for voided invoice");
        }

        // Calculate total paid amount so far
        BigDecimal totalPaid = paymentRepository.sumAmountByInvoiceAndStatus(
                invoice.getId(), Payment.PaymentStatus.COMPLETED);
        if (totalPaid == null) totalPaid = BigDecimal.ZERO;

        // Check if payment amount exceeds remaining balance
        BigDecimal remainingBalance = invoice.getTotalAmount().subtract(totalPaid);
        if (request.getAmount().compareTo(remainingBalance) > 0) {
            throw new IllegalArgumentException(
                    "Payment amount exceeds remaining balance. Remaining: Rs. " + remainingBalance);
        }

        // Create payment record
        Payment payment = new Payment();
        payment.setPaymentReference(generatePaymentReference());
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setStatus(Payment.PaymentStatus.COMPLETED);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        payment.setNotes(request.getNotes() != null ? request.getNotes() : "");

        if (request.getCardLastFour() != null && !request.getCardLastFour().isEmpty()) {
            payment.setCardLastFour(request.getCardLastFour());
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully. Reference: {}", savedPayment.getPaymentReference());

        // Update invoice status
        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
            invoiceRepository.save(invoice);
            log.info("Invoice {} fully paid", invoice.getInvoiceNumber());
        } else {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
            invoiceRepository.save(invoice);
            log.info("Invoice {} partially paid. Remaining: Rs. {}",
                    invoice.getInvoiceNumber(), remainingBalance.subtract(request.getAmount()));
        }

        // Log transaction
        logTransaction(invoice, "PAYMENT_PROCESSED",
                String.format("Payment of Rs. %.2f via %s", request.getAmount(), request.getMethod()));

        return mapToResponseDTO(savedPayment);
    }

    @Transactional
    public PaymentResponseDTO processRefund(Long paymentId, String reason, BigDecimal refundAmount) {
        log.info("Processing refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded. Current status: " + payment.getStatus());
        }

        BigDecimal amountToRefund = refundAmount != null ? refundAmount : payment.getAmount();

        if (amountToRefund.compareTo(payment.getAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        if (amountToRefund.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero");
        }

        // Create refund record (update payment status)
        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        Payment savedPayment = paymentRepository.save(payment);

        // Update invoice status if needed
        Invoice invoice = payment.getInvoice();
        BigDecimal totalPaid = paymentRepository.sumAmountByInvoiceAndStatus(
                invoice.getId(), Payment.PaymentStatus.COMPLETED);

        if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
            invoiceRepository.save(invoice);
            log.info("Invoice {} status updated to ISSUED after refund", invoice.getInvoiceNumber());
        } else if (totalPaid.compareTo(invoice.getTotalAmount()) < 0) {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
            invoiceRepository.save(invoice);
            log.info("Invoice {} status updated to PARTIALLY_PAID after refund", invoice.getInvoiceNumber());
        }

        logTransaction(invoice, "REFUND_PROCESSED",
                String.format("Refund of Rs. %.2f for payment %s. Reason: %s", amountToRefund, payment.getPaymentReference(), reason));

        log.info("Refund processed successfully for payment: {}", payment.getPaymentReference());

        return mapToResponseDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponseDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private String generatePaymentReference() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void logTransaction(Invoice invoice, String action, String details) {
        TransactionLog log = new TransactionLog();
        log.setInvoice(invoice);
        log.setAction(action);
        log.setDetails(details);
        log.setUserId(1L); // Default admin user
        transactionLogRepository.save(log);
    }

    public PaymentResponseDTO mapToResponseDTO(Payment payment) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(payment.getId());
        dto.setPaymentReference(payment.getPaymentReference());
        dto.setInvoiceId(payment.getInvoice().getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod().toString());
        dto.setStatus(payment.getStatus().toString());
        dto.setTransactionId(payment.getTransactionId());
        dto.setCurrency(payment.getCurrency());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setCardLastFour(payment.getCardLastFour());
        dto.setNotes(payment.getNotes());
        return dto;
    }
}