package com.safemeds.safemedsbackend.dtos.ai;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiWarningRequestDTO {
    private boolean seen;
    private boolean resolve;
}