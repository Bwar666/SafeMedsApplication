package com.safemeds.safemedsbackend.repositories.user;

import com.safemeds.safemedsbackend.entities.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
    List<Allergy> findByUserProfileId(UUID userId);
    Optional<Allergy> findByIdAndUserProfileId(UUID allergyId, UUID userId);
    Set<Allergy> findByUserProfileIdAndIdIn(UUID userId, Set<UUID> allergyIds);
}
