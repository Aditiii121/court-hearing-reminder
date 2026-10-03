package com.court.hearing.controller;

import com.court.hearing.dto.HearingRequest;
import com.court.hearing.dto.HearingResponse;
import com.court.hearing.dto.StatusUpdateRequest;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.service.HearingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/hearings")
@RequiredArgsConstructor
public class HearingController {

    private final HearingService hearingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LAWYER','CLERK')")
    public ResponseEntity<HearingResponse> createHearing(@Valid @RequestBody HearingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hearingService.createHearing(request));
    }

    @GetMapping
    public ResponseEntity<Page<HearingResponse>> getAllHearings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "hearingDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hearingService.getAllHearings(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HearingResponse> getHearing(@PathVariable Long id) {
        return ResponseEntity.ok(hearingService.getHearingById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LAWYER')")
    public ResponseEntity<HearingResponse> updateHearing(
            @PathVariable Long id, @Valid @RequestBody HearingRequest request) {
        return ResponseEntity.ok(hearingService.updateHearing(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHearing(@PathVariable Long id) {
        hearingService.deleteHearing(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LAWYER','CLERK')")
    public ResponseEntity<HearingResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(hearingService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HearingResponse>> searchHearings(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String caseTitle,
            @RequestParam(required = false) String courtName,
            @RequestParam(required = false) String judgeName,
            @RequestParam(required = false) String assignedLawyer,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hearingDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "hearingDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hearingService.searchHearings(
                caseNumber, caseTitle, courtName, judgeName, assignedLawyer,
                status, priority, hearingDate, pageable));
    }
}
