package com.safemeds.safemedsbackend.services.user;


import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.AllergyResponseDTO;

import java.util.List;
import java.util.UUID;

public interface AllergyService {
    AllergyResponseDTO createAllergy(UUID userId, AllergyRequestDTO dto);
    List<AllergyResponseDTO> getAllAllergiesByUserId(UUID userId);
    AllergyResponseDTO updateAllergy(UUID userId, UUID allergyId, AllergyRequestDTO dto);
    void deleteAllergy(UUID userId, UUID allergyId);
    AllergyResponseDTO getAllergyById(UUID userId, UUID allergyId);
}
