package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        LocalDateTime orderTimestamp,
        BigDecimal totalAmount,
        Long restaurantTableId,
        List<OrderItemResponse> items) {
}
