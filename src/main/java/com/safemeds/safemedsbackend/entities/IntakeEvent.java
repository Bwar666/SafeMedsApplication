// Enhanced IntakeEvent.java - Add usage tracking fields
package com.safemeds.safemedsbackend.entities;

import com.safemeds.safemedsbackend.enums.IntakeStatus;
import com.safemeds.safemedsbackend.enums.MedicineForm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "intake_events", indexes = {
        @Index(columnList = "medicine_id, scheduled_date_time"),
        @Index(columnList = "status"),
        @Index(columnList = "medicine_id, status"),
        @Index(columnList = "scheduled_date_time")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IntakeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "scheduled_date_time", nullable = false)
    private LocalDateTime scheduledDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IntakeStatus status;

    @Column(name = "dosage_amount", nullable = false)
    private Double dosageAmount;

    @Column(name = "medicine_name_snapshot", nullable = false)
    private String medicineNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "medicine_form_snapshot", nullable = false)
    private MedicineForm medicineFormSnapshot;

    @Column(name = "note")
    private String note;


    @Column(name = "actual_take_time")
    private LocalDateTime actualTakeTime;

    @Column(name = "actual_dosage_amount")
    private Double actualDosageAmount;

    @Column(name = "inventory_deducted")
    private Double inventoryDeducted;

    @Column(name = "is_late_intake", nullable = false)
    @Builder.Default
    private Boolean isLateIntake = false;

    @Column(name = "missed_automatically", nullable = false)
    @Builder.Default
    private Boolean missedAutomatically = false;

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    @Column(name = "can_take_late", nullable = false)
    @Builder.Default
    private Boolean canTakeLate = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void captureSnapshot() {
        if (medicine != null) {
            this.medicineNameSnapshot = medicine.getName();
            this.medicineFormSnapshot = medicine.getForm();
        }
        if (statusChangedAt == null) {
            statusChangedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void updateStatusTime() {
        this.statusChangedAt = LocalDateTime.now();
    }


    public boolean isPastDue() {
        return LocalDateTime.now().isAfter(scheduledDateTime);
    }

    public boolean isMissedByThreshold(int thresholdMinutes) {
        if (status != IntakeStatus.SCHEDULED) {
            return false;
        }
        return LocalDateTime.now().isAfter(scheduledDateTime.plusMinutes(thresholdMinutes));
    }

    public boolean canStillBeTaken(int lateWindowHours) {
        if (status != IntakeStatus.SCHEDULED && status != IntakeStatus.MISSED) {
            return false;
        }
        return LocalDateTime.now().isBefore(scheduledDateTime.plusHours(lateWindowHours));
    }

    public void markAsTaken(LocalDateTime takeTime, Double actualAmount, String userNote, Double inventoryDeducted) {
        this.status = IntakeStatus.TAKEN;
        this.actualTakeTime = takeTime != null ? takeTime : LocalDateTime.now();
        this.actualDosageAmount = actualAmount != null ? actualAmount : dosageAmount;
        this.note = userNote;
        this.inventoryDeducted = inventoryDeducted;
        this.isLateIntake = this.actualTakeTime.isAfter(scheduledDateTime);
        this.statusChangedAt = LocalDateTime.now();
    }

    public void markAsSkipped(String reason, String userNote) {
        this.status = IntakeStatus.SKIPPED;
        this.note = userNote;
        this.statusChangedAt = LocalDateTime.now();
    }

    public void markAsMissed(boolean automatic) {
        this.status = IntakeStatus.MISSED;
        this.missedAutomatically = automatic;
        this.statusChangedAt = LocalDateTime.now();
    }

    public String getFormattedDosage() {
        Double amount = actualDosageAmount != null ? actualDosageAmount : dosageAmount;
        String unit = switch (medicineFormSnapshot) {
            case PILL -> "pill(s)";
            case LIQUID -> "ml";
            case INJECTION -> "unit(s)";
            case DROPS -> "drop(s)";
            default -> "dose(s)";
        };
        return amount + " " + unit;
    }
}