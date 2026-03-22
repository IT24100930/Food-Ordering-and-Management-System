package com.urbanplate.billing.controller;

import com.urbanplate.billing.dto.AnalyticsDTO;
import com.urbanplate.billing.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDTO> getDashboardData() {
        AnalyticsDTO analytics = analyticsService.getDashboardData();
        return ResponseEntity.ok(analytics);
    }
}