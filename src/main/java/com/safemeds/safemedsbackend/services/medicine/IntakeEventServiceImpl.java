package com.safemeds.safemedsbackend.services.medicine;

import com.safemeds.safemedsbackend.entities.IntakeEvent;
import com.safemeds.safemedsbackend.entities.IntakeSchedule;
import com.safemeds.safemedsbackend.entities.Medicine;
import com.safemeds.safemedsbackend.enums.IntakeStatus;
import com.safemeds.safemedsbackend.repositories.medicine.IntakeEventRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IntakeEventServiceImpl implements IntakeEventService {
    private final IntakeEventRepository intakeEventRepository;

    @Override
    public List<IntakeEvent> generateIntakeEvents(Medicine medicine) {
        List<IntakeEvent> events = calculateIntakeSchedule(medicine);
        return intakeEventRepository.saveAll(events);
    }

    @Override
    public void deleteIntakeEventsForMedicine(UUID medicineId) {
        intakeEventRepository.deleteByMedicineId(medicineId);
    }

    @Override
    public void updateIntakeEvents(Medicine medicine) {
        deleteIntakeEventsForMedicine(medicine.getId());
        generateIntakeEvents(medicine);
    }

    private List<IntakeEvent> calculateIntakeSchedule(Medicine medicine) {
        List<IntakeEvent> events = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(medicine.getScheduleDuration());

        switch (medicine.getFrequencyType()) {
            case DAILY:
                generateDailyEvents(medicine, now, endDate, events);
                break;
            case EVERY_OTHER_DAY:
                generateAlternateDayEvents(medicine, now, endDate, events);
                break;
            case SPECIFIC_DAYS_OF_WEEK:
                generateSpecificDaysEvents(medicine, now, endDate, events);
                break;
            case EVERY_X_DAYS:
                generateEveryXDaysEvents(medicine, now, endDate, events);
                break;
            case EVERY_X_WEEKS:
                generateEveryXWeeksEvents(medicine, now, endDate, events);
                break;
            case EVERY_X_MONTHS:
                generateEveryXMonthsEvents(medicine, now, endDate, events);
                break;
            case CYCLE_BASED:
                generateCycleBasedEvents(medicine, now, endDate, events);
                break;
            default:
                throw new IllegalArgumentException("Unsupported frequency type: " + medicine.getFrequencyType());
        }

        return events;
    }

    private void generateDailyEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        while (current.isBefore(end)) {
            for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                LocalDateTime scheduledTime = current
                        .withHour(schedule.getTime().getHour())
                        .withMinute(schedule.getTime().getMinute());
                if (scheduledTime.isAfter(start)) {
                    events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                }
            }
            current = current.plusDays(1);
        }
    }

    private void generateAlternateDayEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        while (current.isBefore(end)) {
            for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                LocalDateTime scheduledTime = current
                        .withHour(schedule.getTime().getHour())
                        .withMinute(schedule.getTime().getMinute());
                if (scheduledTime.isAfter(start)) {
                    events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                }
            }
            current = current.plusDays(2);
        }
    }

    private void generateSpecificDaysEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        Set<DayOfWeek> scheduledDays = medicine.getFrequencyConfig().getSpecificDays();

        while (current.isBefore(end)) {
            if (scheduledDays.contains(current.getDayOfWeek())) {
                for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                    LocalDateTime scheduledTime = current
                            .withHour(schedule.getTime().getHour())
                            .withMinute(schedule.getTime().getMinute());
                    if (scheduledTime.isAfter(start)) {
                        events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                    }
                }
            }
            current = current.plusDays(1);
        }
    }

    private void generateEveryXDaysEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        int daysInterval = medicine.getFrequencyConfig().getIntervalDays();

        while (current.isBefore(end)) {
            for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                LocalDateTime scheduledTime = current
                        .withHour(schedule.getTime().getHour())
                        .withMinute(schedule.getTime().getMinute());
                if (scheduledTime.isAfter(start)) {
                    events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                }
            }
            current = current.plusDays(daysInterval);
        }
    }

    private void generateEveryXWeeksEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        int weeksInterval = medicine.getFrequencyConfig().getIntervalDays();
        Set<DayOfWeek> scheduledDays = medicine.getFrequencyConfig().getSpecificDays();

        while (!scheduledDays.contains(current.getDayOfWeek()) && current.isBefore(end)) {
            current = current.plusDays(1);
        }

        while (current.isBefore(end)) {
            for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                LocalDateTime scheduledTime = current
                        .withHour(schedule.getTime().getHour())
                        .withMinute(schedule.getTime().getMinute());
                if (scheduledTime.isAfter(start)) {
                    events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                }
            }
            current = current.plusWeeks(weeksInterval);
        }
    }

    private void generateEveryXMonthsEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        int monthsInterval = medicine.getFrequencyConfig().getIntervalDays();
        int dayOfMonth = medicine.getFrequencyConfig().getDayOfMonth();

        if (current.getDayOfMonth() > dayOfMonth) {
            current = current.plusMonths(1);
        }
        current = current.withDayOfMonth(Math.min(dayOfMonth, current.toLocalDate().lengthOfMonth()));

        while (current.isBefore(end)) {
            for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                LocalDateTime scheduledTime = current
                        .withHour(schedule.getTime().getHour())
                        .withMinute(schedule.getTime().getMinute());
                if (scheduledTime.isAfter(start)) {
                    events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                }
            }
            current = current.plusMonths(monthsInterval);
            current = current.withDayOfMonth(Math.min(dayOfMonth, current.toLocalDate().lengthOfMonth()));
        }
    }

    private void generateCycleBasedEvents(Medicine medicine, LocalDateTime start, LocalDateTime end, List<IntakeEvent> events) {
        LocalDateTime current = start.toLocalDate().atStartOfDay();
        int activeDays = medicine.getFrequencyConfig().getCycleActiveDays();
        int restDays = medicine.getFrequencyConfig().getCycleRestDays();
        int totalCycleDays = activeDays + restDays;

        long daysSinceStart = ChronoUnit.DAYS.between(medicine.getCreatedAt().toLocalDate(), start.toLocalDate());
        int currentCycleDay = (int) (daysSinceStart % totalCycleDays);

        while (current.isBefore(end)) {
            if (currentCycleDay < activeDays) {
                for (IntakeSchedule schedule : medicine.getIntakeSchedules()) {
                    LocalDateTime scheduledTime = current
                            .withHour(schedule.getTime().getHour())
                            .withMinute(schedule.getTime().getMinute());
                    if (scheduledTime.isAfter(start)) {
                        events.add(createIntakeEvent(medicine, scheduledTime, schedule.getAmount()));
                    }
                }
            }

            current = current.plusDays(1);
            currentCycleDay = (currentCycleDay + 1) % totalCycleDays;
        }
    }

    private IntakeEvent createIntakeEvent(Medicine medicine, LocalDateTime scheduledTime, Double amount) {
        return IntakeEvent.builder()
                .medicine(medicine)
                .scheduledDateTime(scheduledTime)
                .status(IntakeStatus.SCHEDULED)
                .dosageAmount(amount)
                .medicineNameSnapshot(medicine.getName())
                .medicineFormSnapshot(medicine.getForm())
                .build();
    }
}