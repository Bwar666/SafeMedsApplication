package com.safemeds.safemedsbackend.services.user;

import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileResponseDTO;

import java.util.List;
import java.util.UUID;

public interface UserProfileService {

    UserProfileResponseDTO createProfile(UserProfileRequestDTO dto);

    UserProfileResponseDTO getProfileById(UUID id);

    UserProfileResponseDTO updateProfile(UUID id, UserProfileRequestDTO dto);

    UserProfileResponseDTO updateAllergies(UUID userId, List<AllergyRequestDTO> allergies);
}
