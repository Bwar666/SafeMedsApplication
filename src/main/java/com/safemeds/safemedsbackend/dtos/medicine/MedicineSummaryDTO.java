package com.safemeds.safemedsbackend.dtos.medicine;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class MedicineSummaryDTO {
    private UUID id;
    private String name;
    private String icon;
    private String color;
    private String nextIntakeTime; // e.g., "08:00"
}
