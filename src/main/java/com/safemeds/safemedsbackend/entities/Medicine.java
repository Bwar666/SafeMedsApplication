package com.safemeds.safemedsbackend.entities;

import com.safemeds.safemedsbackend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MedicineForm form;

    @Column(name = "condition_reason")
    private String conditionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private FrequencyType frequencyType;

    @Embedded
    private FrequencyDetail frequencyDetail;

    @ElementCollection
    @CollectionTable(name = "intake_times", joinColumns = @JoinColumn(name = "medicine_id"))
    @Column(name = "intake_time")
    private List<LocalTime> intakeTimes;

    @ElementCollection
    @CollectionTable(name = "dosage_schedule", joinColumns = @JoinColumn(name = "medicine_id"))
    private List<DosageSchedule> dosageSchedules;

    @Column(name = "refill_reminder_threshold")
    private Integer refillReminderThreshold;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_instruction")
    private FoodInstruction foodInstruction;

    private String icon;
    private String color;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserProfile userProfile;

    @ManyToMany
    @JoinTable(
            name = "medicine_allergies",
            joinColumns = @JoinColumn(name = "medicine_id"),
            inverseJoinColumns = @JoinColumn(name = "allergy_id")
    )
    private Set<Allergy> relatedAllergies = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}