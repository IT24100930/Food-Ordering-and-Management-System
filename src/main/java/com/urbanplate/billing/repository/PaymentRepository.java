package com.urbanplate.billing.repository;

import com.urbanplate.billing.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.invoice.id = :invoiceId AND p.status = :status")
    BigDecimal sumAmountByInvoiceAndStatus(@Param("invoiceId") Long invoiceId,
                                           @Param("status") Payment.PaymentStatus status);

    List<Payment> findByStatus(Payment.PaymentStatus status);
}