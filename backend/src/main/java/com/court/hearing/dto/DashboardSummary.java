package com.court.hearing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long total;
    private long scheduled;
    private long pending;
    private long inProgress;
    private long completed;
    private long adjourned;
    private long cancelled;
    private long urgent;
    private long upcomingToday;
    private long upcomingNext7Days;
    private long upcomingNext24Hours;
}
