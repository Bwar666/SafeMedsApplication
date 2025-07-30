package com.safemeds.safemedsbackend.repositories.ai;

import com.safemeds.safemedsbackend.entities.AiWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiWarningRepository extends JpaRepository<AiWarning, UUID> {

    List<AiWarning> findAllByUserProfileIdOrderByCreatedAtDesc(UUID userProfileId);

    List<AiWarning> findByUserProfileIdAndResolvedFalse(UUID userProfileId);

    Optional<AiWarning> findByUserProfileIdAndMedicineIdAndEvaluationResultId(
            UUID userProfileId, UUID medicineId, UUID evaluationResultId);

    @Query("SELECT aw FROM AiWarning aw WHERE aw.userProfile.id = :userId AND aw.seen = false")
    List<AiWarning> findUnseenWarningsByUser(@Param("userId") UUID userId);
}