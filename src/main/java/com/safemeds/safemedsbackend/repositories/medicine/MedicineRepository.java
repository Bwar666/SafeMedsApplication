package com.safemeds.safemedsbackend.repositories.medicine;

import com.safemeds.safemedsbackend.entities.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

    List<Medicine> findByUserProfileId(UUID userId);
    Optional<Medicine> findByIdAndUserProfileId(UUID id, UUID userId);
    List<Medicine> findByUserProfileIdAndIsActiveTrue(UUID userId);
    List<Medicine> findByIsActiveTrue();
    List<Medicine> findByUserProfileIdAndIsPausedTrue(UUID userId);
    List<Medicine> findByUserProfileIdAndNotificationsEnabledFalse(UUID userId);

    @Query("SELECT m.name FROM Medicine m WHERE m.userProfile.id = :userId AND m.isActive = true")
    List<String> findActiveMedicineNamesByUserId(@Param("userId") UUID userId);

    // Find medicines with low inventory
    @Query("SELECT m FROM Medicine m WHERE m.userProfile.id = :userId " +
            "AND m.isActive = true " +
            "AND m.currentInventory IS NOT NULL " +
            "AND m.refillReminderThreshold IS NOT NULL " +
            "AND m.currentInventory <= m.refillReminderThreshold")
    List<Medicine> findLowInventoryMedicines(@Param("userId") UUID userId);

    // Find medicines with no inventory
    @Query("SELECT m FROM Medicine m WHERE m.userProfile.id = :userId " +
            "AND m.isActive = true " +
            "AND m.currentInventory IS NOT NULL " +
            "AND m.currentInventory <= 0")
    List<Medicine> findOutOfStockMedicines(@Param("userId") UUID userId);

    // Find active, non-paused medicines
    @Query("SELECT m FROM Medicine m WHERE m.userProfile.id = :userId " +
            "AND m.isActive = true " +
            "AND m.isPaused = false")
    List<Medicine> findActiveMedicinesNotPaused(@Param("userId") UUID userId);

    // Get medicines that need automatic resumption (if you implement scheduled resume)
    @Query("SELECT m FROM Medicine m WHERE m.isPaused = true " +
            "AND m.pausedAt IS NOT NULL " +
            "AND m.pausedAt < :beforeDateTime")
    List<Medicine> findMedicinesToAutoResume(@Param("beforeDateTime") java.time.LocalDateTime beforeDateTime);

    // Count active medicines for a user
    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.userProfile.id = :userId AND m.isActive = true")
    Long countActiveMedicinesByUserId(@Param("userId") UUID userId);

    // Find medicines requiring refill (broader than just low inventory)
    @Query("SELECT m FROM Medicine m WHERE m.userProfile.id = :userId " +
            "AND m.isActive = true " +
            "AND ((m.currentInventory IS NOT NULL AND m.refillReminderThreshold IS NOT NULL " +
            "AND m.currentInventory <= m.refillReminderThreshold) " +
            "OR (m.currentInventory IS NOT NULL AND m.currentInventory <= 0))")
    List<Medicine> findMedicinesNeedingRefill(@Param("userId") UUID userId);
}