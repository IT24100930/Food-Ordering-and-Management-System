package com.urbanplate.billing.repository;

import com.urbanplate.billing.model.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    List<TransactionLog> findByInvoiceId(Long invoiceId);

    List<TransactionLog> findByAction(String action);
}