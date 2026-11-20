package com.codealpha.restaurantmanagement.dto;

import com.codealpha.restaurantmanagement.enums.ReservationStatus;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        String customerName,
        String customerContact,
        Integer partySize,
        LocalDateTime reservationStartTime,
        LocalDateTime reservationEndTime,
        ReservationStatus status,
        Long restaurantTableId) {
}
