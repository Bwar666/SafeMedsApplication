package com.safemeds.safemedsbackend.dtos.medicine;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequestDTO {
    @NotNull(message = "New inventory amount is required")
    @PositiveOrZero(message = "Inventory amount must be non-negative")
    private Double newInventoryAmount;

    private String updateReason;

    private Boolean resetToFull = false;
}