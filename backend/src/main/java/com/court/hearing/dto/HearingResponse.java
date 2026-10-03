package com.court.hearing.dto;

import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingResponse {
    private Long id;
    private String caseNumber;
    private String caseTitle;
    private String petitioner;
    private String respondent;
    private String courtName;
    private LocalDate hearingDate;
    private LocalTime hearingTime;
    private String judgeName;
    private String assignedLawyer;
    private String caseType;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public static HearingResponse from(Hearing h) {
        return HearingResponse.builder()
                .id(h.getId())
                .caseNumber(h.getCaseNumber())
                .caseTitle(h.getCaseTitle())
                .petitioner(h.getPetitioner())
                .respondent(h.getRespondent())
                .courtName(h.getCourtName())
                .hearingDate(h.getHearingDate())
                .hearingTime(h.getHearingTime())
                .judgeName(h.getJudgeName())
                .assignedLawyer(h.getAssignedLawyer())
                .caseType(h.getCaseType())
                .description(h.getDescription())
                .status(h.getStatus())
                .priority(h.getPriority())
                .createdAt(h.getCreatedAt())
                .updatedAt(h.getUpdatedAt())
                .createdBy(h.getCreatedBy())
                .updatedBy(h.getUpdatedBy())
                .build();
    }
}
