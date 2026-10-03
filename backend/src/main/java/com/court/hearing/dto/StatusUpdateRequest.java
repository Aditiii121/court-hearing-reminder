package com.court.hearing.dto;

import com.court.hearing.entity.Hearing.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull(message = "Status is required")
    private Status status;
    private String reason;
}
