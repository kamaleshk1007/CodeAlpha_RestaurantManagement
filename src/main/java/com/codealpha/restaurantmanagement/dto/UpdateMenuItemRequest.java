package com.codealpha.restaurantmanagement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateMenuItemRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 1000) String description,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal price,
        @NotNull Boolean available) {
}
