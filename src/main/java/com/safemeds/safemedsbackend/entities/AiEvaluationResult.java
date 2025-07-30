package com.safemeds.safemedsbackend.entities;

import com.safemeds.safemedsbackend.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_evaluation_results", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"medicine_name", "risk_type", "allergy_name"}),
        @UniqueConstraint(columnNames = {"medicine_name", "risk_type", "target_medicine_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_type", nullable = false)
    private AiWarningType riskType;

    @Column(name = "allergy_name")
    private String allergyName;


    @Column(name = "target_medicine_name")
    private String targetMedicineName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WarningSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiWarningStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiWarningSource source;

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}