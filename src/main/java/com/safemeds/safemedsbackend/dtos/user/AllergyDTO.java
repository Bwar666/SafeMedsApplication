package com.safemeds.safemedsbackend.dtos.user;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllergyDTO {

    private UUID id;
    private String name;
    private String description;
}
