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
public class IntakeSchedule {
    private LocalTime time;
    private Double amount;
}
