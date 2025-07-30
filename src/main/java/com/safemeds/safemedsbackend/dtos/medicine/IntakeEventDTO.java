package com.safemeds.safemedsbackend.dtos.medicine;

import com.safemeds.safemedsbackend.enums.IntakeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntakeEventDTO {
    private UUID id;
    private UUID medicineId;
    private String medicineName;
    private String medicineIcon;
    private String medicineColor;
    private LocalDateTime scheduledDateTime;
    private LocalDateTime actualDateTime;
    private IntakeStatus status;
    private Double scheduledAmount;
    private Double actualAmount;
    private String note;
    private String skipReason;
    private Boolean canTakeLate;
    private Boolean inventoryAvailable;
    private String formattedDosage;
}