package com.court.hearing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardSummary {
    private String clientEmail;
    private String clientName;
    private long totalHearings;
    private long upcomingHearings;
    private long scheduledHearings;
    private long pendingHearings;
    private long inProgressHearings;
    private long completedHearings;
    private long adjournedHearings;
    private long cancelledHearings;
}

