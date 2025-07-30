package com.safemeds.safemedsbackend.dtos.medicine;

import com.safemeds.safemedsbackend.enums.IntakeStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineUsageResponseDTO {
    private UUID intakeEventId;
    private UUID medicineId;
    private String medicineName;
    private IntakeStatus status;
    private LocalDateTime scheduledDateTime;
    private LocalDateTime actualDateTime;
    private Double scheduledAmount;
    private Double actualAmount;
    private String note;
    private String skipReason;
    private Double remainingInventory;
    private Boolean inventoryLow;
    private String message;
}