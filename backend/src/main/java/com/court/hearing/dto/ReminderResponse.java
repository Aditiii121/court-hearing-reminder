package com.court.hearing.dto;

import com.court.hearing.entity.Reminder;
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
public class ReminderResponse {
    private Long id;
    private Long hearingId;
    private String caseNumber;
    private String caseTitle;
    private String courtName;
    private LocalDate hearingDate;
    private LocalTime hearingTime;
    private String judgeName;
    private String assignedLawyer;
    private Reminder.ReminderType reminderType;
    private Reminder.ReminderStatus status;
    private LocalDateTime triggeredAt;
    private String message;

    public static ReminderResponse from(Reminder r) {
        return ReminderResponse.builder()
                .id(r.getId())
                .hearingId(r.getHearing().getId())
                .caseNumber(r.getHearing().getCaseNumber())
                .caseTitle(r.getHearing().getCaseTitle())
                .courtName(r.getHearing().getCourtName())
                .hearingDate(r.getHearing().getHearingDate())
                .hearingTime(r.getHearing().getHearingTime())
                .judgeName(r.getHearing().getJudgeName())
                .assignedLawyer(r.getHearing().getAssignedLawyer())
                .reminderType(r.getReminderType())
                .status(r.getStatus())
                .triggeredAt(r.getTriggeredAt())
                .message(r.getMessage())
                .build();
    }
}
