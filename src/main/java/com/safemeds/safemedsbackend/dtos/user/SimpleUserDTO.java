package com.safemeds.safemedsbackend.dtos.user;

import lombok.*;

import java.util.UUID;

@Data
@Builder
public class SimpleUserDTO {
    private UUID id;
    private String firstName;
    private String lastName;

}
