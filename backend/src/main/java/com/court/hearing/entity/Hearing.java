package com.court.hearing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "hearings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hearing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Case number is required")
    private String caseNumber;

    @Column(nullable = false)
    @NotBlank(message = "Case title is required")
    private String caseTitle;

    private String petitioner;
    private String respondent;

    @Column(nullable = false)
    @NotBlank(message = "Court name is required")
    private String courtName;

    @Column(nullable = false)
    @NotNull(message = "Hearing date is required")
    private LocalDate hearingDate;

    private LocalTime hearingTime;
    private String judgeName;
    private String assignedLawyer;
    private String caseType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Status is required")
    @Builder.Default
    private Status status = Status.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(updatable = false)
    private String createdBy;

    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Status {
        SCHEDULED, PENDING, IN_PROGRESS, COMPLETED, ADJOURNED, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
}
