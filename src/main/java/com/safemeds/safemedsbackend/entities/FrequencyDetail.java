package com.safemeds.safemedsbackend.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrequencyDetail {

    // Example: every 2 days, or Mon/Wed/Fri
    private String recurrencePattern; // e.g., "Mon,Wed,Fri" or "Every 3 days"
    private LocalDate startDate;
    private Integer repeatInterval; // e.g., every 3 days = 3
}
