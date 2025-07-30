package com.safemeds.safemedsbackend.repositories.medicine;

import com.safemeds.safemedsbackend.entities.IntakeEvent;
import com.safemeds.safemedsbackend.enums.IntakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface IntakeEventRepository extends JpaRepository<IntakeEvent, UUID> {

    void deleteByMedicineId(UUID medicineId);
    List<IntakeEvent> findByMedicineIdOrderByScheduledDateTimeAsc(UUID medicineId);
    List<IntakeEvent> findByScheduledDateTimeBefore(LocalDateTime dateTime);
    List<IntakeEvent> findByMedicineIdAndScheduledDateTimeAfter(UUID medicineId, LocalDateTime dateTime);


    // Find events by user and date range
    @Query("SELECT ie FROM IntakeEvent ie WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.scheduledDateTime BETWEEN :startDateTime AND :endDateTime " +
            "ORDER BY ie.scheduledDateTime ASC")
    List<IntakeEvent> findByMedicineUserProfileIdAndScheduledDateTimeBetweenOrderByScheduledDateTime(
            @Param("userId") UUID userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    // Find events by user, status, and date range
    @Query("SELECT ie FROM IntakeEvent ie WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.status = :status " +
            "AND ie.scheduledDateTime BETWEEN :startDateTime AND :endDateTime " +
            "ORDER BY ie.scheduledDateTime ASC")
    List<IntakeEvent> findByMedicineUserProfileIdAndStatusAndScheduledDateTimeBetweenOrderByScheduledDateTime(
            @Param("userId") UUID userId,
            @Param("status") IntakeStatus status,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    // Find overdue events (scheduled before a certain time)
    @Query("SELECT ie FROM IntakeEvent ie WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.status = :status " +
            "AND ie.scheduledDateTime < :beforeDateTime " +
            "ORDER BY ie.scheduledDateTime ASC")
    List<IntakeEvent> findByMedicineUserProfileIdAndStatusAndScheduledDateTimeBeforeOrderByScheduledDateTime(
            @Param("userId") UUID userId,
            @Param("status") IntakeStatus status,
            @Param("beforeDateTime") LocalDateTime beforeDateTime);

    // Find all events by user and status
    @Query("SELECT ie FROM IntakeEvent ie WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.status = :status")
    List<IntakeEvent> findByMedicineUserProfileIdAndStatus(
            @Param("userId") UUID userId,
            @Param("status") IntakeStatus status);

    // Get adherence statistics for a user in a date range
    @Query("SELECT ie.status, COUNT(ie) FROM IntakeEvent ie " +
            "WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.scheduledDateTime BETWEEN :startDateTime AND :endDateTime " +
            "GROUP BY ie.status")
    List<Object[]> getAdherenceStatistics(
            @Param("userId") UUID userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    // Find next upcoming intake for a specific medicine
    @Query("SELECT ie FROM IntakeEvent ie WHERE ie.medicine.id = :medicineId " +
            "AND ie.status = :status " +
            "AND ie.scheduledDateTime > :afterDateTime " +
            "ORDER BY ie.scheduledDateTime ASC")
    List<IntakeEvent> findNextIntakeForMedicine(
            @Param("medicineId") UUID medicineId,
            @Param("status") IntakeStatus status,
            @Param("afterDateTime") LocalDateTime afterDateTime);

    // Count missed doses in the last X days
    @Query("SELECT COUNT(ie) FROM IntakeEvent ie " +
            "WHERE ie.medicine.userProfile.id = :userId " +
            "AND ie.status = :status " +
            "AND ie.scheduledDateTime >= :afterDateTime")
    Long countMissedDosesSince(
            @Param("userId") UUID userId,
            @Param("status") IntakeStatus status,
            @Param("afterDateTime") LocalDateTime afterDateTime);

    // Find events that should be automatically marked as missed
    @Query("SELECT ie FROM IntakeEvent ie " +
            "WHERE ie.status = :status " +
            "AND ie.medicine.isActive = true " +
            "AND ie.medicine.isPaused = false " +
            "AND ie.scheduledDateTime < :beforeDateTime")
    List<IntakeEvent> findEventsToMarkAsMissed(
            @Param("status") IntakeStatus status,
            @Param("beforeDateTime") LocalDateTime beforeDateTime);
}