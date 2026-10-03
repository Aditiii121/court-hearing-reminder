package com.court.hearing.service;

import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.entity.Reminder;
import com.court.hearing.entity.Reminder.ReminderType;
import com.court.hearing.repository.HearingRepository;
import com.court.hearing.repository.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderService Unit Tests")
class ReminderServiceTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private ReminderService reminderService;

    private Hearing todayHearing;
    private Hearing next3DaysHearing;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        todayHearing = Hearing.builder()
                .id(1L)
                .caseNumber("CASE-TODAY")
                .caseTitle("Today's Hearing")
                .courtName("City Court")
                .hearingDate(today)
                .hearingTime(LocalTime.of(10, 0))
                .status(Status.SCHEDULED)
                .build();

        next3DaysHearing = Hearing.builder()
                .id(2L)
                .caseNumber("CASE-SOON")
                .caseTitle("Soon's Hearing")
                .courtName("High Court")
                .hearingDate(today.plusDays(3))
                .hearingTime(LocalTime.of(11, 0))
                .status(Status.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Process reminders - creates 24h reminder for today's hearing")
    void processReminders_creates24hReminder() {
        LocalDate today = LocalDate.now();

        when(hearingRepository.findByHearingDateBetweenAndStatusNot(
                eq(today), eq(today.plusDays(1)), eq(Status.CANCELLED)))
                .thenReturn(List.of(todayHearing));
        when(hearingRepository.findByHearingDateAndStatusNot(eq(today), eq(Status.COMPLETED)))
                .thenReturn(List.of(todayHearing));
        when(hearingRepository.findByHearingDateBetweenAndStatusNot(
                eq(today), eq(today.plusDays(7)), eq(Status.CANCELLED)))
                .thenReturn(List.of(todayHearing, next3DaysHearing));
        when(reminderRepository.existsByHearingIdAndReminderType(1L, ReminderType.WITHIN_24_HOURS))
                .thenReturn(false);
        when(reminderRepository.existsByHearingIdAndReminderType(1L, ReminderType.WITHIN_7_DAYS))
                .thenReturn(false);
        when(reminderRepository.existsByHearingIdAndReminderType(2L, ReminderType.WITHIN_7_DAYS))
                .thenReturn(false);
        when(reminderRepository.save(any(Reminder.class))).thenAnswer(inv -> inv.getArgument(0));

        reminderService.processReminders();

        // Should save at least 2 reminders: 1 WITHIN_24_HOURS + 2 WITHIN_7_DAYS
        verify(reminderRepository, atLeast(2)).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Process reminders - skips already-created 24h reminder")
    void processReminders_skipsExisting24hReminder() {
        LocalDate today = LocalDate.now();

        when(hearingRepository.findByHearingDateBetweenAndStatusNot(any(), any(), eq(Status.CANCELLED)))
                .thenReturn(List.of(todayHearing));
        when(hearingRepository.findByHearingDateAndStatusNot(any(), eq(Status.COMPLETED)))
                .thenReturn(List.of());
        when(reminderRepository.existsByHearingIdAndReminderType(1L, ReminderType.WITHIN_24_HOURS))
                .thenReturn(true); // Already exists!
        when(reminderRepository.existsByHearingIdAndReminderType(1L, ReminderType.WITHIN_7_DAYS))
                .thenReturn(true);

        reminderService.processReminders();

        // Should NOT save a duplicate
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    @DisplayName("Get all reminders - returns list")
    void getUpcomingReminders_returnsList() {
        when(reminderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

        var result = reminderService.getUpcomingReminders();

        assertThat(result).isNotNull();
        verify(reminderRepository).findAllByOrderByCreatedAtDesc();
    }

    // Needed for assertThat
    private static <T> org.assertj.core.api.AbstractListAssert<?, List<? extends T>, T, ?> assertThat(List<T> list) {
        return org.assertj.core.api.Assertions.assertThat(list);
    }
}
