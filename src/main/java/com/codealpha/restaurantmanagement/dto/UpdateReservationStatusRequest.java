package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReservationStatusRequest(@NotNull ReservationStatus status) {
}
