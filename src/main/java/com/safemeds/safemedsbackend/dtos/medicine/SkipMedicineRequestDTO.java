package com.safemeds.safemedsbackend.dtos.medicine;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkipMedicineRequestDTO {
    @NotNull(message = "Intake event ID is required")
    private UUID intakeEventId;

    @NotBlank(message = "Skip reason is required")
    private String skipReason;

    private String note;
}