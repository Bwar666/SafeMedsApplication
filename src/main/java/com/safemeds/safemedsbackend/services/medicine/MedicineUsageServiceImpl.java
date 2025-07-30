package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.*;
import com.safemeds.safemedsbackend.entities.*;
import com.safemeds.safemedsbackend.enums.IntakeStatus;
import com.safemeds.safemedsbackend.mappers.medicine.MedicineMapper;
import com.safemeds.safemedsbackend.repositories.medicine.*;
import com.safemeds.safemedsbackend.repositories.user.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicineUsageServiceImpl implements MedicineUsageService {

    private final IntakeEventRepository intakeEventRepository;
    private final MedicineRepository medicineRepository;
    private final UserProfileRepository userProfileRepository;
    private final MedicineMapper medicineMapper;

    @Override
    @Transactional
    public MedicineUsageResponseDTO takeMedicine(UUID userId, TakeMedicineRequestDTO request) {
        IntakeEvent event = getIntakeEventForUser(userId, request.getIntakeEventId());
        Medicine medicine = event.getMedicine();


        if (event.getStatus() == IntakeStatus.TAKEN) {
            throw new IllegalStateException("Medicine has already been taken");
        }

        if (medicine.isPausedOrInactive()) {
            throw new IllegalStateException("Cannot take paused or inactive medicine");
        }


        Double dosageToTake = request.getActualDosageAmount() != null ?
                request.getActualDosageAmount() : event.getDosageAmount();

        if (!medicine.hasInventory() && medicine.getCurrentInventory() != null) {
            throw new IllegalStateException("Insufficient inventory to take this medicine");
        }


        if (!event.canStillBeTaken(medicine.getLateIntakeWindowHours())) {
            throw new IllegalStateException("It's too late to take this dose");
        }

        LocalDateTime takeTime = request.getActualTakeTime() != null ?
                request.getActualTakeTime() : LocalDateTime.now();

        Double inventoryDeducted = null;
        if (request.getDeductFromInventory() && medicine.getAutoDeductInventory()) {
            medicine.deductInventory(dosageToTake);
            inventoryDeducted = dosageToTake;
            medicineRepository.save(medicine);
        }


        event.markAsTaken(takeTime, dosageToTake, request.getNote(), inventoryDeducted);
        intakeEventRepository.save(event);

        log.info("Medicine {} taken by user {} at {}", medicine.getName(), userId, takeTime);

        return buildUsageResponse(event, medicine);
    }

    @Override
    @Transactional
    public MedicineUsageResponseDTO skipMedicine(UUID userId, SkipMedicineRequestDTO request) {
        IntakeEvent event = getIntakeEventForUser(userId, request.getIntakeEventId());
        Medicine medicine = event.getMedicine();

        if (event.getStatus() == IntakeStatus.TAKEN) {
            throw new IllegalStateException("Cannot skip a medicine that has already been taken");
        }

        event.markAsSkipped(request.getSkipReason(), request.getNote());
        intakeEventRepository.save(event);

        log.info("Medicine {} skipped by user {}: {}", medicine.getName(), userId, request.getSkipReason());

        return buildUsageResponse(event, medicine);
    }

    @Override
    @Transactional
    public void pauseMedicine(UUID userId, UUID medicineId, PauseMedicineRequestDTO request) {
        Medicine medicine = getMedicineForUser(userId, medicineId);

        medicine.setIsPaused(true);
        medicine.setPausedAt(LocalDateTime.now());

        if (request.getPauseNotifications()) {
            medicine.setNotificationsEnabled(false);
        }

        medicineRepository.save(medicine);

        log.info("Medicine {} paused by user {}: {}", medicine.getName(), userId, request.getPauseReason());
    }

    @Override
    @Transactional
    public void resumeMedicine(UUID userId, UUID medicineId) {
        Medicine medicine = getMedicineForUser(userId, medicineId);

        medicine.setIsPaused(false);
        medicine.setPausedAt(null);
        medicine.setNotificationsEnabled(true);

        medicineRepository.save(medicine);

        log.info("Medicine {} resumed by user {}", medicine.getName(), userId);
    }

    @Override
    @Transactional
    public void toggleNotifications(UUID userId, UUID medicineId, boolean enabled) {
        Medicine medicine = getMedicineForUser(userId, medicineId);
        medicine.setNotificationsEnabled(enabled);
        medicineRepository.save(medicine);

        log.info("Notifications {} for medicine {} by user {}",
                enabled ? "enabled" : "disabled", medicine.getName(), userId);
    }

    @Override
    public DailyMedicineScheduleDTO getDailySchedule(UUID userId, LocalDate date) {
        validateUserExists(userId);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<IntakeEvent> events = intakeEventRepository
                .findByMedicineUserProfileIdAndScheduledDateTimeBetweenOrderByScheduledDateTime(
                        userId, startOfDay, endOfDay);

        List<IntakeEventDTO> eventDTOs = events.stream()
                .map(this::mapToIntakeEventDTO)
                .collect(Collectors.toList());

        return DailyMedicineScheduleDTO.builder()
                .date(date)
                .intakeEvents(eventDTOs)
                .totalScheduled((int) events.size())
                .totalTaken((int) events.stream().filter(e -> e.getStatus() == IntakeStatus.TAKEN).count())
                .totalSkipped((int) events.stream().filter(e -> e.getStatus() == IntakeStatus.SKIPPED).count())
                .totalMissed((int) events.stream().filter(e -> e.getStatus() == IntakeStatus.MISSED).count())
                .totalPending((int) events.stream().filter(e -> e.getStatus() == IntakeStatus.SCHEDULED).count())
                .build();
    }

    @Override
    public List<DailyMedicineScheduleDTO> getWeeklySchedule(UUID userId, LocalDate startDate) {
        List<DailyMedicineScheduleDTO> weeklySchedule = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            weeklySchedule.add(getDailySchedule(userId, date));
        }

        return weeklySchedule;
    }

    @Override
    public List<IntakeEventDTO> getUpcomingIntakes(UUID userId, int hours) {
        validateUserExists(userId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureTime = now.plusHours(hours);

        List<IntakeEvent> events = intakeEventRepository
                .findByMedicineUserProfileIdAndStatusAndScheduledDateTimeBetweenOrderByScheduledDateTime(
                        userId, IntakeStatus.SCHEDULED, now, futureTime);

        return events.stream()
                .filter(event -> !event.getMedicine().isPausedOrInactive())
                .map(this::mapToIntakeEventDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<IntakeEventDTO> getOverdueIntakes(UUID userId) {
        validateUserExists(userId);

        LocalDateTime now = LocalDateTime.now();

        List<IntakeEvent> events = intakeEventRepository
                .findByMedicineUserProfileIdAndStatusAndScheduledDateTimeBeforeOrderByScheduledDateTime(
                        userId, IntakeStatus.SCHEDULED, now);

        return events.stream()
                .filter(event -> !event.getMedicine().isPausedOrInactive())
                .filter(event -> event.canStillBeTaken(event.getMedicine().getLateIntakeWindowHours()))
                .map(this::mapToIntakeEventDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(UUID userId, UUID medicineId, InventoryUpdateRequestDTO request) {
        Medicine medicine = getMedicineForUser(userId, medicineId);

        medicine.setCurrentInventory(request.getNewInventoryAmount());

        if (request.getResetToFull()) {
            medicine.setTotalInventory(request.getNewInventoryAmount());
        }

        medicineRepository.save(medicine);

        log.info("Inventory updated for medicine {} by user {}: {} {}",
                medicine.getName(), userId, request.getNewInventoryAmount(), medicine.getInventoryUnit());
    }

    @Override
    public List<MedicineResponseDTO> getLowInventoryMedicines(UUID userId) {
        validateUserExists(userId);

        List<Medicine> medicines = medicineRepository.findByUserProfileIdAndIsActiveTrue(userId);

        List<Medicine> lowInventoryMedicines = medicines.stream()
                .filter(Medicine::isInventoryLow)
                .collect(Collectors.toList());

        return medicineMapper.toResponseDTOList(lowInventoryMedicines);
    }

    @Override
    @Transactional
    public void processMissedDoses(UUID userId) {
        validateUserExists(userId);

        List<IntakeEvent> scheduledEvents = intakeEventRepository
                .findByMedicineUserProfileIdAndStatus(userId, IntakeStatus.SCHEDULED);

        LocalDateTime now = LocalDateTime.now();
        List<IntakeEvent> missedEvents = new ArrayList<>();

        for (IntakeEvent event : scheduledEvents) {
            Medicine medicine = event.getMedicine();

            if (medicine.isPausedOrInactive()) {
                continue;
            }

            if (event.isMissedByThreshold(medicine.getMissedDoseThresholdMinutes())) {
                event.markAsMissed(true);
                missedEvents.add(event);
            }
        }

        if (!missedEvents.isEmpty()) {
            intakeEventRepository.saveAll(missedEvents);
            log.info("Marked {} doses as missed for user {}", missedEvents.size(), userId);
        }
    }

    @Override
    @Transactional
    public void markEventAsMissed(UUID userId, UUID intakeEventId, boolean automatic) {
        IntakeEvent event = getIntakeEventForUser(userId, intakeEventId);

        if (event.getStatus() != IntakeStatus.SCHEDULED) {
            throw new IllegalStateException("Can only mark scheduled events as missed");
        }

        event.markAsMissed(automatic);
        intakeEventRepository.save(event);

        log.info("Event {} marked as missed for user {} (automatic: {})", intakeEventId, userId, automatic);
    }


    private IntakeEvent getIntakeEventForUser(UUID userId, UUID intakeEventId) {
        IntakeEvent event = intakeEventRepository.findById(intakeEventId)
                .orElseThrow(() -> new EntityNotFoundException("Intake event not found"));

        if (!event.getMedicine().getUserProfile().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to intake event");
        }

        return event;
    }

    private Medicine getMedicineForUser(UUID userId, UUID medicineId) {
        Medicine medicine = medicineRepository.findByIdAndUserProfileId(medicineId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Medicine not found"));

        if (!medicine.getUserProfile().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to medicine");
        }

        return medicine;
    }

    private void validateUserExists(UUID userId) {
        if (!userProfileRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found");
        }
    }

    private MedicineUsageResponseDTO buildUsageResponse(IntakeEvent event, Medicine medicine) {
        return MedicineUsageResponseDTO.builder()
                .intakeEventId(event.getId())
                .medicineId(medicine.getId())
                .medicineName(medicine.getName())
                .status(event.getStatus())
                .scheduledDateTime(event.getScheduledDateTime())
                .actualDateTime(event.getActualTakeTime())
                .scheduledAmount(event.getDosageAmount())
                .actualAmount(event.getActualDosageAmount())
                .note(event.getNote())
                .remainingInventory(medicine.getCurrentInventory())
                .inventoryLow(medicine.isInventoryLow())
                .message(buildStatusMessage(event, medicine))
                .build();
    }

    private IntakeEventDTO mapToIntakeEventDTO(IntakeEvent event) {
        Medicine medicine = event.getMedicine();

        return IntakeEventDTO.builder()
                .id(event.getId())
                .medicineId(medicine.getId())
                .medicineName(medicine.getName())
                .medicineIcon(medicine.getIcon())
                .medicineColor(medicine.getColor())
                .scheduledDateTime(event.getScheduledDateTime())
                .actualDateTime(event.getActualTakeTime())
                .status(event.getStatus())
                .scheduledAmount(event.getDosageAmount())
                .actualAmount(event.getActualDosageAmount())
                .note(event.getNote())
                .canTakeLate(event.canStillBeTaken(medicine.getLateIntakeWindowHours()))
                .inventoryAvailable(medicine.hasInventory() || medicine.getCurrentInventory() == null)
                .formattedDosage(event.getFormattedDosage())
                .build();
    }

    private String buildStatusMessage(IntakeEvent event, Medicine medicine) {
        return switch (event.getStatus()) {
            case TAKEN -> {
                String message = "Medicine taken successfully";
                if (event.getIsLateIntake()) {
                    message += " (taken late)";
                }
                if (medicine.isInventoryLow()) {
                    message += ". Warning: Low inventory remaining.";
                }
                yield message;
            }
            case SKIPPED -> "Medicine skipped: ";
            case MISSED -> event.getMissedAutomatically() ?
                    "Automatically marked as missed" : "Manually marked as missed";
            case SCHEDULED -> "Scheduled for " + event.getScheduledDateTime();
            default -> "Unknown status";
        };
    }
}