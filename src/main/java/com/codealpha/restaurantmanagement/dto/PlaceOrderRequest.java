package com.codealpha.restaurantmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PlaceOrderRequest(
        @Positive Long restaurantTableId,
        @NotEmpty @Size(max = 100) List<@Valid OrderItemRequest> items) {
}
