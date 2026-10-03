package com.court.hearing.controller;

import com.court.hearing.dto.ReminderResponse;
import com.court.hearing.entity.Reminder.ReminderType;
import com.court.hearing.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders(
            @RequestParam(required = false) ReminderType type) {
        if (type != null) {
            return ResponseEntity.ok(reminderService.getRemindersByType(type));
        }
        return ResponseEntity.ok(reminderService.getUpcomingReminders());
    }

    @PostMapping("/trigger")
    public ResponseEntity<Void> triggerReminders() {
        reminderService.processReminders();
        return ResponseEntity.ok().build();
    }
}
