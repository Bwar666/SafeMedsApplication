package com.safemeds.safemedsbackend.dtos.ai;


import com.safemeds.safemedsbackend.enums.WarningSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConflictWarningDTO {

    private String type; // e.g., "ALLERGY", "INTERACTION", "DOSAGE"

    private String title; // e.g., "Potential Allergy Risk: Penicillin"

    private String description; // e.g., "This medicine may contain penicillin, which matches your allergy: 'Penicillin'."


    private WarningSeverity severity; // Optional: LOW, MEDIUM, HIGH
}
