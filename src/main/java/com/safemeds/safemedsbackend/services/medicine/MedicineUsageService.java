package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.dtos.medicine.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MedicineUsageService {


    MedicineUsageResponseDTO takeMedicine(UUID userId, TakeMedicineRequestDTO request);
    MedicineUsageResponseDTO skipMedicine(UUID userId, SkipMedicineRequestDTO request);
    void pauseMedicine(UUID userId, UUID medicineId, PauseMedicineRequestDTO request);
    void resumeMedicine(UUID userId, UUID medicineId);
    void toggleNotifications(UUID userId, UUID medicineId, boolean enabled);


    DailyMedicineScheduleDTO getDailySchedule(UUID userId, LocalDate date);
    List<DailyMedicineScheduleDTO> getWeeklySchedule(UUID userId, LocalDate startDate);
    List<IntakeEventDTO> getUpcomingIntakes(UUID userId, int hours);
    List<IntakeEventDTO> getOverdueIntakes(UUID userId);


    void updateInventory(UUID userId, UUID medicineId, InventoryUpdateRequestDTO request);
    List<MedicineResponseDTO> getLowInventoryMedicines(UUID userId);


    void processMissedDoses(UUID userId);
    void markEventAsMissed(UUID userId, UUID intakeEventId, boolean automatic);
}
