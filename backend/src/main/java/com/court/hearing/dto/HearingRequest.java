package com.court.hearing.dto;

import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class HearingRequest {

    @NotBlank(message = "Case number is required")
    private String caseNumber;

    @NotBlank(message = "Case title is required")
    private String caseTitle;

    private String petitioner;
    private String respondent;

    @NotBlank(message = "Court name is required")
    private String courtName;

    @NotNull(message = "Hearing date is required")
    private LocalDate hearingDate;

    private LocalTime hearingTime;
    private String judgeName;
    private String assignedLawyer;
    private String caseType;
    private String description;

    @NotNull(message = "Status is required")
    private Status status;

    private Priority priority;
}
