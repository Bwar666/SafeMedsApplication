package com.safemeds.safemedsbackend.repositories.ai;

import com.safemeds.safemedsbackend.entities.AiEvaluationResult;
import com.safemeds.safemedsbackend.enums.AiWarningType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiEvaluationResultRepository extends JpaRepository<AiEvaluationResult, UUID> {

    Optional<AiEvaluationResult> findByMedicineNameIgnoreCaseAndRiskTypeAndAllergyNameIgnoreCase(
            String medicineName, AiWarningType riskType, String allergyName);

    Optional<AiEvaluationResult> findByMedicineNameIgnoreCaseAndRiskTypeAndTargetMedicineNameIgnoreCase(
            String medicineName, AiWarningType riskType, String targetMedicineName);

    Optional<AiEvaluationResult> findByMedicineNameIgnoreCaseAndRiskTypeAndAllergyNameIsNullAndTargetMedicineNameIsNull(
            String medicineName, AiWarningType riskType);

    @Query("SELECT aer FROM AiEvaluationResult aer WHERE aer.medicineName = :medicineName AND aer.riskType = :riskType")
    List<AiEvaluationResult> findByMedicineAndRiskType(@Param("medicineName") String medicineName,
                                                       @Param("riskType") AiWarningType riskType);
}