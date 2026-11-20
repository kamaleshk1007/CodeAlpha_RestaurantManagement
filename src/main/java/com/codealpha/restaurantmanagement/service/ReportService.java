package com.codealpha.restaurantmanagement.service;

import com.codealpha.restaurantmanagement.dto.InventoryResponse;
import com.codealpha.restaurantmanagement.repository.InventoryRepository;
import com.codealpha.restaurantmanagement.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    public ReportService(OrderRepository orderRepository, InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public DailySalesReport dailySales(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        BigDecimal totalSales = orderRepository.totalAmountBetween(start, end);
        return new DailySalesReport(date, totalSales);
    }

    public List<InventoryResponse> stockAlerts(BigDecimal threshold) {
        return inventoryRepository.findByAvailableQuantityLessThan(threshold).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private InventoryResponse toResponse(com.codealpha.restaurantmanagement.entity.Inventory inventory) {
        return new InventoryResponse(inventory.getId(), inventory.getIngredientName(),
                inventory.getAvailableQuantity(), inventory.getUnit());
    }

    public record DailySalesReport(LocalDate date, BigDecimal totalSales) {
    }
}
