package com.safemeds.safemedsbackend.entities;

import com.safemeds.safemedsbackend.enums.AiWarningType;
import com.safemeds.safemedsbackend.enums.WarningSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "ai_warnings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Type of the warning (e.g., allergy, overdose, interaction)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiWarningType type;

    // Severity level of the warning
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarningSeverity severity = WarningSeverity.MEDIUM;

    // Full message shown to the user
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    // Optional technical or structured info (could be JSON or plain text)
    @Column(columnDefinition = "TEXT")
    private String details;

    // Whether the user has seen the warning
    @Column(nullable = false)
    private boolean seen = false;

    // Related user profile
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_profile_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserProfile userProfile;

    // Related medicines (supports multi-drug interactions)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "ai_warning_medicines",
            joinColumns = @JoinColumn(name = "warning_id"),
            inverseJoinColumns = @JoinColumn(name = "medicine_id")
    )
    @Builder.Default
    private Set<Medicine> relatedMedicines = new HashSet<>();

    // Timestamp when user resolved the warning
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // Timestamp tracking
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

    // Helper method to add related medicines
    public void addRelatedMedicine(Medicine medicine) {
        if (medicine == null) return;
        this.relatedMedicines.add(medicine);
    }

    public void markAsResolved() {
        if (this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
    public boolean isResolved() {
        return this.resolvedAt != null;
    }
}