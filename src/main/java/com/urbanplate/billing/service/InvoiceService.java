package com.urbanplate.billing.service;

import com.urbanplate.billing.dto.InvoiceRequestDTO;
import com.urbanplate.billing.dto.InvoiceResponseDTO;
import com.urbanplate.billing.exception.ResourceNotFoundException;
import com.urbanplate.billing.model.*;
import com.urbanplate.billing.repository.*;
import com.urbanplate.billing.utils.InvoiceNumberGenerator;
import com.urbanplate.billing.utils.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final TaxRepository taxRepository;
    private final DiscountRepository discountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final QRCodeGenerator qrCodeGenerator;

    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO request) {
        log.info("Creating invoice for order: {}", request.getOrderId());

        // Validate input
        if (request.getOrderTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order total must be greater than zero");
        }

        // Calculate subtotal
        BigDecimal subtotal = request.getOrderTotal();

        // Apply discount if coupon code provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        Discount appliedDiscount = null;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            java.util.Optional<Discount> discountOpt = discountRepository.findByCouponCode(request.getCouponCode().toUpperCase());

            if (discountOpt.isPresent()) {
                appliedDiscount = discountOpt.get();

                if (appliedDiscount.isValid() &&
                        subtotal.compareTo(appliedDiscount.getMinimumOrderAmount()) >= 0) {
                    discountAmount = appliedDiscount.calculateDiscount(subtotal);
                    appliedDiscount.setUsageCount(appliedDiscount.getUsageCount() + 1);
                    discountRepository.save(appliedDiscount);
                    log.info("Applied discount {} of {}", request.getCouponCode(), discountAmount);
                } else {
                    log.warn("Discount {} is not valid or doesn't meet minimum order amount", request.getCouponCode());
                }
            } else {
                log.warn("Coupon code {} not found", request.getCouponCode());
            }
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // Calculate taxes
        BigDecimal taxAmount = calculateTaxes(afterDiscount);

        // Calculate total
        BigDecimal totalAmount = afterDiscount.add(taxAmount);

        // Generate invoice number
        String invoiceNumber = invoiceNumberGenerator.generateInvoiceNumber();

        // Generate QR code for payment
        String qrCode = qrCodeGenerator.generateQRCode(invoiceNumber, totalAmount);

        // Create invoice
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setOrderId(request.getOrderId());
        invoice.setCustomerId(request.getCustomerId());
        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(Invoice.InvoiceStatus.ISSUED);
        invoice.setQrCode(qrCode);
        invoice.setNotes(request.getNotes() != null ? request.getNotes() : "");

        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice created successfully with number: {}", invoiceNumber);

        // Log transaction
        logTransaction(savedInvoice, "INVOICE_CREATED", "Invoice created with total: " + totalAmount);

        return mapToResponseDTO(savedInvoice);
    }

    private BigDecimal calculateTaxes(BigDecimal amount) {
        List<Tax> activeTaxes = taxRepository.findByActiveTrue();
        if (activeTaxes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return activeTaxes.stream()
                .map(tax -> tax.calculateTax(amount))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return mapToResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with number: " + invoiceNumber));
        return mapToResponseDTO(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void voidInvoice(Long id, String reason) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot void a paid invoice. Process refund instead.");
        }

        if (invoice.getStatus() == Invoice.InvoiceStatus.VOID) {
            throw new IllegalStateException("Invoice is already voided");
        }

        invoice.setStatus(Invoice.InvoiceStatus.VOID);
        invoiceRepository.save(invoice);

        logTransaction(invoice, "INVOICE_VOIDED", "Reason: " + reason);
        log.info("Invoice {} voided successfully", invoice.getInvoiceNumber());
    }

    @Transactional
    public InvoiceResponseDTO updateInvoiceStatus(Long id, String status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));

        try {
            Invoice.InvoiceStatus newStatus = Invoice.InvoiceStatus.valueOf(status.toUpperCase());
            invoice.setStatus(newStatus);
            Invoice updatedInvoice = invoiceRepository.save(invoice);

            logTransaction(invoice, "STATUS_UPDATED", "Status changed to: " + status);
            return mapToResponseDTO(updatedInvoice);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void logTransaction(Invoice invoice, String action, String details) {
        TransactionLog log = new TransactionLog();
        log.setInvoice(invoice);
        log.setAction(action);
        log.setDetails(details);
        log.setUserId(1L); // Default admin user
        transactionLogRepository.save(log);
    }

    public InvoiceResponseDTO mapToResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setOrderId(invoice.getOrderId());
        dto.setCustomerId(invoice.getCustomerId());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getStatus().toString());
        dto.setQrCode(invoice.getQrCode());
        dto.setCreatedAt(invoice.getCreatedAt());

        // Calculate paid amount
        BigDecimal paidAmount = paymentRepository.sumAmountByInvoiceAndStatus(
                invoice.getId(), Payment.PaymentStatus.COMPLETED);
        dto.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        dto.setRemainingBalance(dto.getTotalAmount().subtract(dto.getPaidAmount()));

        return dto;
    }
}