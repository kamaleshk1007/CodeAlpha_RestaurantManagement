package com.codealpha.restaurantmanagement.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateReservationRequest(
        @NotBlank @Size(max = 120) String customerName,
        @NotBlank @Size(max = 120) String customerContact,
        @NotNull @Positive Integer partySize,
        @NotNull LocalDateTime reservationStartTime,
        @NotNull LocalDateTime reservationEndTime,
        @NotNull @Positive Long restaurantTableId) {

    @AssertTrue(message = "reservation end time must be after reservation start time")
    public boolean isReservationTimeRangeValid() {
        return reservationStartTime == null || reservationEndTime == null
                || reservationEndTime.isAfter(reservationStartTime);
    }
}
