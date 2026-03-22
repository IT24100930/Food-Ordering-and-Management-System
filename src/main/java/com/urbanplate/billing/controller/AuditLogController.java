package com.urbanplate.billing.controller;

import com.urbanplate.billing.model.TransactionLog;
import com.urbanplate.billing.repository.TransactionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final TransactionLogRepository transactionLogRepository;

    // GET all logs
    @GetMapping
    public ResponseEntity<List<TransactionLog>> getAllLogs() {
        List<TransactionLog> logs = transactionLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    // GET logs by invoice ID
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<TransactionLog>> getLogsByInvoice(@PathVariable Long invoiceId) {
        List<TransactionLog> logs = transactionLogRepository.findByInvoiceId(invoiceId);
        return ResponseEntity.ok(logs);
    }

    // GET logs by action
    @GetMapping("/action/{action}")
    public ResponseEntity<List<TransactionLog>> getLogsByAction(@PathVariable String action) {
        List<TransactionLog> logs = transactionLogRepository.findByAction(action);
        return ResponseEntity.ok(logs);
    }
}