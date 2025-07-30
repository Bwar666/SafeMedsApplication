package com.safemeds.safemedsbackend.services.user;

import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.AllergyResponseDTO;
import com.safemeds.safemedsbackend.entities.Allergy;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.mappers.user.AllergyMapper;
import com.safemeds.safemedsbackend.repositories.user.AllergyRepository;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {

    private final AllergyRepository allergyRepository;
    private final UserProfileRepository userProfileRepository;
    private final AllergyMapper allergyMapper;

    @Override
    @Transactional
    public AllergyResponseDTO createAllergy(UUID userId, AllergyRequestDTO dto) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Allergy allergy = allergyMapper.toEntity(dto);
        allergy.setUserProfile(user);
        return allergyMapper.toResponseDTO(allergyRepository.save(allergy));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllergyResponseDTO> getAllAllergiesByUserId(UUID userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
        return allergyMapper.toResponseDTOList(allergyRepository.findByUserProfileId(userId));
    }

    @Override
    @Transactional
    public AllergyResponseDTO updateAllergy(UUID userId, UUID allergyId, AllergyRequestDTO dto) {
        Allergy allergy = allergyRepository.findByIdAndUserProfileId(allergyId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Allergy not found for this user"));

        allergy.setName(dto.getName());
        allergy.setDescription(dto.getDescription());
        return allergyMapper.toResponseDTO(allergyRepository.save(allergy));
    }

    @Override
    @Transactional
    public void deleteAllergy(UUID userId, UUID allergyId) {
        Allergy allergy = allergyRepository.findByIdAndUserProfileId(allergyId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Allergy not found for this user"));
        allergyRepository.delete(allergy);
    }

    @Override
    @Transactional(readOnly = true)
    public AllergyResponseDTO getAllergyById(UUID userId, UUID allergyId) {
        // Fetch the allergy by ID and user ID (ensuring user ownership)
        Allergy allergy = allergyRepository.findByIdAndUserProfileId(allergyId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Allergy not found for this user"));

        return allergyMapper.toResponseDTO(allergy);
    }


}