package com.codealpha.restaurantmanagement.controller;

import com.codealpha.restaurantmanagement.dto.InventoryResponse;
import com.codealpha.restaurantmanagement.service.ReportService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily-sales")
    public ResponseEntity<ReportService.DailySalesReport> dailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reportService.dailySales(date));
    }

    @GetMapping("/stock-alerts")
    public ResponseEntity<List<InventoryResponse>> stockAlerts(
            @RequestParam BigDecimal threshold) {
        return ResponseEntity.ok(reportService.stockAlerts(threshold));
    }
}
