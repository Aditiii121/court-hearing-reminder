package com.court.hearing.repository;

import com.court.hearing.entity.Reminder;
import com.court.hearing.entity.Reminder.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    boolean existsByHearingIdAndReminderType(Long hearingId, ReminderType type);

    @Query("""
        SELECT r FROM Reminder r 
        JOIN FETCH r.hearing h 
        WHERE r.reminderType = :type
        ORDER BY h.hearingDate, h.hearingTime
    """)
    List<Reminder> findByReminderTypeWithHearing(@Param("type") ReminderType type);

    List<Reminder> findAllByOrderByCreatedAtDesc();

    // ── Client-scoped reminder query ──────────────────────────────────────
    @Query("""
        SELECT r FROM Reminder r
        JOIN FETCH r.hearing h
        WHERE h.clientEmail = :clientEmail
        ORDER BY h.hearingDate ASC, h.hearingTime ASC
    """)
    List<Reminder> findByHearingClientEmail(@Param("clientEmail") String clientEmail);
}
