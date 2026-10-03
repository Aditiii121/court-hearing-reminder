package com.court.hearing.service;

import com.court.hearing.dto.DashboardSummary;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.repository.HearingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final HearingRepository hearingRepository;

    public DashboardSummary getSummary() {
        LocalDate today = LocalDate.now();
        LocalDate next7Days = today.plusDays(7);

        return DashboardSummary.builder()
                .total(hearingRepository.count())
                .scheduled(hearingRepository.countByStatus(Status.SCHEDULED))
                .pending(hearingRepository.countByStatus(Status.PENDING))
                .inProgress(hearingRepository.countByStatus(Status.IN_PROGRESS))
                .completed(hearingRepository.countByStatus(Status.COMPLETED))
                .adjourned(hearingRepository.countByStatus(Status.ADJOURNED))
                .cancelled(hearingRepository.countByStatus(Status.CANCELLED))
                .urgent(hearingRepository.countByPriority(Priority.URGENT))
                .upcomingToday(hearingRepository.countUpcomingToday(today))
                .upcomingNext24Hours(hearingRepository.countUpcomingToday(today))
                .upcomingNext7Days(hearingRepository.countUpcomingNext7Days(today, next7Days))
                .build();
    }
}
