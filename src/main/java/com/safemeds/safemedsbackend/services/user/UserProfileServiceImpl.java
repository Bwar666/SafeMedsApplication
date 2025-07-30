package com.safemeds.safemedsbackend.services.user;

import com.safemeds.safemedsbackend.dtos.user.AllergyRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileRequestDTO;
import com.safemeds.safemedsbackend.dtos.user.UserProfileResponseDTO;
import com.safemeds.safemedsbackend.entities.Allergy;
import com.safemeds.safemedsbackend.entities.UserProfile;
import com.safemeds.safemedsbackend.mappers.user.AllergyMapper;
import com.safemeds.safemedsbackend.mappers.user.UserProfileMapper;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final AllergyMapper allergyMapper;

    @Override
    public UserProfileResponseDTO createProfile(UserProfileRequestDTO dto) {
        UserProfile entity = userProfileMapper.toEntity(dto);

        if (entity.getAllergies() != null) {
            entity.getAllergies().forEach(allergy -> allergy.setUserProfile(entity));
        }

        UserProfile saved = userProfileRepository.save(entity);
        return userProfileMapper.toResponseDTO(saved);
    }

    @Override
    public UserProfileResponseDTO getProfileById(UUID id) {
        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return userProfileMapper.toResponseDTO(user);
    }

    @Override
    public UserProfileResponseDTO updateProfile(UUID id, UserProfileRequestDTO dto) {
        UserProfile existingUser = userProfileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        userProfileMapper.update(dto,existingUser);
        UserProfile updated = userProfileRepository.save(existingUser);

        return userProfileMapper.toResponseDTO(updated);
    }

    @Transactional
    @Override
    public UserProfileResponseDTO updateAllergies(UUID userId, List<AllergyRequestDTO> allergyDTOs) {
        UserProfile user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        List<AllergyRequestDTO> incomingAllergies = allergyDTOs != null
                ? allergyDTOs
                : Collections.emptyList();

        Map<UUID, Allergy> existingAllergies = user.getAllergies().stream()
                .collect(Collectors.toMap(Allergy::getId, Function.identity()));

        Set<UUID> incomingIds = incomingAllergies.stream()
                .map(AllergyRequestDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        user.getAllergies().removeIf(allergy -> !incomingIds.contains(allergy.getId()));

        for (AllergyRequestDTO dto : incomingAllergies) {
            if (dto.getId() != null && existingAllergies.containsKey(dto.getId())) {
                Allergy allergy = existingAllergies.get(dto.getId());
                allergyMapper.update(allergy, dto);
            } else {
                Allergy newAllergy = allergyMapper.toEntity(dto);
                newAllergy.setUserProfile(user);
                user.getAllergies().add(newAllergy);
            }
        }

        userProfileRepository.save(user);
        return userProfileMapper.toResponseDTO(user);
    }


}

