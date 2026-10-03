package com.court.hearing.scheduler;

import com.court.hearing.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    /**
     * Run every hour to check for upcoming hearings.
     * Also runs on application startup (initialDelay = 30s).
     */
    @Scheduled(fixedRateString = "${reminder.schedule.rate:3600000}",
               initialDelayString = "${reminder.schedule.initialDelay:30000}")
    public void runReminderJob() {
        log.info("=== Reminder Scheduler: starting job run ===");
        try {
            reminderService.processReminders();
        } catch (Exception e) {
            log.error("Reminder scheduler encountered an error: {}", e.getMessage(), e);
        }
        log.info("=== Reminder Scheduler: job run complete ===");
    }
}
