package com.urbanplate.billing.controller;

import com.urbanplate.billing.dto.InvoiceRequestDTO;
import com.urbanplate.billing.dto.InvoiceResponseDTO;
import com.urbanplate.billing.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    // GET all invoices - THIS WAS MISSING!
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    // GET invoice history - alias for getAllInvoices
    @GetMapping("/history")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoiceHistory() {
        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        InvoiceResponseDTO createdInvoice = invoiceService.createInvoice(request);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        InvoiceResponseDTO invoice = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByCustomer(@PathVariable Long customerId) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByCustomer(customerId);
        return ResponseEntity.ok(invoices);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> voidInvoice(
            @PathVariable Long id,
            @RequestParam String reason) {
        invoiceService.voidInvoice(id, reason);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Invoice voided successfully");
        response.put("invoiceId", id.toString());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponseDTO> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        InvoiceResponseDTO invoice = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(invoice);
    }
}