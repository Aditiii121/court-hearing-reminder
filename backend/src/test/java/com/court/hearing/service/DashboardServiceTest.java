package com.court.hearing.service;

import com.court.hearing.dto.DashboardSummary;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.repository.HearingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getSummary returns correct counts from DB")
    void getSummary_returnsCorrectCounts() {
        when(hearingRepository.count()).thenReturn(15L);
        when(hearingRepository.countByStatus(Status.SCHEDULED)).thenReturn(5L);
        when(hearingRepository.countByStatus(Status.PENDING)).thenReturn(3L);
        when(hearingRepository.countByStatus(Status.IN_PROGRESS)).thenReturn(2L);
        when(hearingRepository.countByStatus(Status.COMPLETED)).thenReturn(3L);
        when(hearingRepository.countByStatus(Status.ADJOURNED)).thenReturn(1L);
        when(hearingRepository.countByStatus(Status.CANCELLED)).thenReturn(1L);
        when(hearingRepository.countByPriority(Priority.URGENT)).thenReturn(4L);
        when(hearingRepository.countUpcomingToday(any(LocalDate.class))).thenReturn(2L);
        when(hearingRepository.countUpcomingNext7Days(any(LocalDate.class), any(LocalDate.class))).thenReturn(7L);

        DashboardSummary summary = dashboardService.getSummary();

        assertThat(summary.getTotal()).isEqualTo(15L);
        assertThat(summary.getScheduled()).isEqualTo(5L);
        assertThat(summary.getPending()).isEqualTo(3L);
        assertThat(summary.getInProgress()).isEqualTo(2L);
        assertThat(summary.getCompleted()).isEqualTo(3L);
        assertThat(summary.getUrgent()).isEqualTo(4L);
        assertThat(summary.getUpcomingNext7Days()).isEqualTo(7L);
    }
}
