package com.safemeds.safemedsbackend.entities;

import com.safemeds.safemedsbackend.enums.*;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "medicines", indexes = @Index(columnList = "user_profile_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicineForm form;

    @Column(name = "condition_reason", nullable = false, length = 500)
    private String conditionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private FrequencyType frequencyType;

    @Embedded
    private FrequencyConfig frequencyConfig;

    @ElementCollection
    @CollectionTable(
            name = "medicine_intake_schedules",
            joinColumns = @JoinColumn(name = "medicine_id")
    )
    @OrderColumn(name = "schedule_order")
    private List<IntakeSchedule> intakeSchedules = new ArrayList<>();

    @Column(name = "refill_reminder_threshold")
    private Integer refillReminderThreshold;

    @Enumerated(EnumType.STRING)
    private FoodInstruction foodInstruction;

    @Column(length = 50)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "schedule_duration")
    private Integer scheduleDuration; // in days


    @Column(name = "is_paused", nullable = false)
    @Builder.Default
    private Boolean isPaused = false;

    @Column(name = "paused_at")
    private LocalDateTime pausedAt;

    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "current_inventory")
    private Double currentInventory;

    @Column(name = "total_inventory")
    private Double totalInventory;

    @Column(name = "inventory_unit", length = 20)
    private String inventoryUnit;

    @Column(name = "auto_deduct_inventory", nullable = false)
    @Builder.Default
    private Boolean autoDeductInventory = true;

    @Column(name = "missed_dose_threshold_minutes")
    @Builder.Default
    private Integer missedDoseThresholdMinutes = 60; // Auto-mark as missed after 1 hour

    @Column(name = "allow_late_intake", nullable = false)
    @Builder.Default
    private Boolean allowLateIntake = true;

    @Column(name = "late_intake_window_hours")
    @Builder.Default
    private Integer lateIntakeWindowHours = 4; // Allow taking up to 4 hours late

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserProfile userProfile;

    @ManyToMany
    @JoinTable(
            name = "medicine_allergy_links",
            joinColumns = @JoinColumn(name = "medicine_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> relatedAllergies = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;

        if (color == null) color = "#2196F3";
        if (icon == null) icon = "pill";
        if (isActive == null) isActive = true;
        if (scheduleDuration == null) scheduleDuration = 30;
        if (isPaused == null) isPaused = false;
        if (notificationsEnabled == null) notificationsEnabled = true;
        if (autoDeductInventory == null) autoDeductInventory = true;
        if (missedDoseThresholdMinutes == null) missedDoseThresholdMinutes = 60;
        if (allowLateIntake == null) allowLateIntake = true;
        if (lateIntakeWindowHours == null) lateIntakeWindowHours = 4;

        // Set default inventory unit based on form
        if (inventoryUnit == null) {
            inventoryUnit = switch (form) {
                case PILL -> "pills";
                case LIQUID -> "ml";
                case INJECTION -> "units";
                case DROPS -> "drops";
                default -> "doses";
            };
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


    public boolean isInventoryLow() {
        if (currentInventory == null || refillReminderThreshold == null) {
            return false;
        }
        return currentInventory <= refillReminderThreshold;
    }

    public boolean hasInventory() {
        return currentInventory != null && currentInventory > 0;
    }

    public void deductInventory(Double amount) {
        if (currentInventory != null && autoDeductInventory && amount != null) {
            currentInventory = Math.max(0, currentInventory - amount);
        }
    }

    public void addInventory(Double amount) {
        if (currentInventory != null && amount != null) {
            currentInventory += amount;
            if (totalInventory != null) {
                currentInventory = Math.min(currentInventory, totalInventory);
            }
        }
    }

    public boolean isPausedOrInactive() {
        return !isActive || isPaused;
    }
}