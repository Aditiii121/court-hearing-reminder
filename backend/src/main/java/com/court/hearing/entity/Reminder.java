package com.court.hearing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hearing_id", nullable = false)
    private Hearing hearing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    private LocalDateTime triggeredAt;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ReminderType {
        WITHIN_24_HOURS, WITHIN_7_DAYS
    }

    public enum ReminderStatus {
        PENDING, SENT, ACKNOWLEDGED
    }
}
