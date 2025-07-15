package com.safemeds.safemedsbackend.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DosageSchedule {

    private LocalTime time;
    private int amount; // e.g., 2 pills
}
