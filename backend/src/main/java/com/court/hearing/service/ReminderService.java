package com.court.hearing.service;

import com.court.hearing.dto.ReminderResponse;
import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Reminder;
import com.court.hearing.entity.Reminder.ReminderStatus;
import com.court.hearing.entity.Reminder.ReminderType;
import com.court.hearing.repository.HearingRepository;
import com.court.hearing.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final HearingRepository hearingRepository;

    @Transactional
    public void processReminders() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        LocalDate next7 = today.plusDays(7);

        log.info("Running reminder job at {}", LocalDateTime.now());

        // Within 24 hours (today and tomorrow)
        List<Hearing> within24h = new ArrayList<>(hearingRepository.findByHearingDateBetweenAndStatusNot(
                today, tomorrow, Hearing.Status.CANCELLED));
        within24h.addAll(hearingRepository.findByHearingDateAndStatusNot(
                today, Hearing.Status.COMPLETED));

        for (Hearing h : within24h.stream().distinct().toList()) {
            if (!reminderRepository.existsByHearingIdAndReminderType(h.getId(), ReminderType.WITHIN_24_HOURS)) {
                Reminder reminder = Reminder.builder()
                        .hearing(h)
                        .reminderType(ReminderType.WITHIN_24_HOURS)
                        .status(ReminderStatus.SENT)
                        .triggeredAt(LocalDateTime.now())
                        .message(String.format("URGENT: Hearing '%s' (%s) is scheduled at %s in %s",
                                h.getCaseTitle(), h.getCaseNumber(), h.getHearingDate(), h.getCourtName()))
                        .build();
                reminderRepository.save(reminder);
                log.info("[REMINDER 24H] Case: {} - {}", h.getCaseNumber(), h.getCaseTitle());
            }
        }

        // Within 7 days
        List<Hearing> within7d = hearingRepository.findByHearingDateBetweenAndStatusNot(
                today, next7, Hearing.Status.CANCELLED);

        for (Hearing h : within7d) {
            if (!reminderRepository.existsByHearingIdAndReminderType(h.getId(), ReminderType.WITHIN_7_DAYS)) {
                Reminder reminder = Reminder.builder()
                        .hearing(h)
                        .reminderType(ReminderType.WITHIN_7_DAYS)
                        .status(ReminderStatus.SENT)
                        .triggeredAt(LocalDateTime.now())
                        .message(String.format("REMINDER: Hearing '%s' (%s) is scheduled on %s in %s",
                                h.getCaseTitle(), h.getCaseNumber(), h.getHearingDate(), h.getCourtName()))
                        .build();
                reminderRepository.save(reminder);
                log.info("[REMINDER 7D] Case: {} - {}", h.getCaseNumber(), h.getCaseTitle());
            }
        }

        log.info("Reminder job complete. Processed {} 24h, {} 7d hearings",
                within24h.size(), within7d.size());
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getUpcomingReminders() {
        return reminderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ReminderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersByType(ReminderType type) {
        return reminderRepository.findByReminderTypeWithHearing(type)
                .stream()
                .map(ReminderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getClientReminders(String clientEmail) {
        return reminderRepository.findByHearingClientEmail(clientEmail)
                .stream()
                .map(ReminderResponse::from)
                .toList();
    }
}
