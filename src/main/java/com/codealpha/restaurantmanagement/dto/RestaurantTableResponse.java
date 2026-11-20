package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.TableStatus;

public record RestaurantTableResponse(
        Long id,
        Integer tableNumber,
        Integer seatingCapacity,
        TableStatus status) {
}
