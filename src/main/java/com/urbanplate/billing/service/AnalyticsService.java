package com.urbanplate.billing.service;

import com.urbanplate.billing.dto.AnalyticsDTO;
import com.urbanplate.billing.model.Invoice;
import com.urbanplate.billing.model.Payment;
import com.urbanplate.billing.repository.InvoiceRepository;
import com.urbanplate.billing.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public AnalyticsDTO getDashboardData() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime startOfWeek = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);

        AnalyticsDTO analytics = new AnalyticsDTO();

        // Today's revenue
        BigDecimal todayRevenue = invoiceRepository.getTotalRevenueBetweenDates(startOfDay, endOfDay);
        analytics.setTodayRevenue(todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        // Weekly revenue
        BigDecimal weeklyRevenue = invoiceRepository.getTotalRevenueBetweenDates(startOfWeek, endOfDay);
        analytics.setWeeklyRevenue(weeklyRevenue != null ? weeklyRevenue : BigDecimal.ZERO);

        // Total invoices
        analytics.setTotalInvoices(invoiceRepository.count());

        // Paid invoices
        long paidInvoices = invoiceRepository.findByStatus(Invoice.InvoiceStatus.PAID).size();
        analytics.setPaidInvoices(paidInvoices);

        // Payment method distribution
        List<Payment> payments = paymentRepository.findAll();
        Map<String, Long> paymentMethodDistribution = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.COMPLETED)
                .collect(Collectors.groupingBy(
                        p -> p.getMethod().toString(),
                        Collectors.counting()
                ));
        analytics.setPaymentMethodDistribution(paymentMethodDistribution);

        // Average order value
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenueBetweenDates(
                LocalDateTime.of(2024, 1, 1, 0, 0), endOfDay);
        long totalOrders = invoiceRepository.count();
        if (totalOrders > 0 && totalRevenue != null) {
            analytics.setAverageOrderValue(totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            analytics.setAverageOrderValue(BigDecimal.ZERO);
        }

        return analytics;
    }
}