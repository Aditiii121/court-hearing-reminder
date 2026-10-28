package com.court.hearing.controller;

import com.court.hearing.dto.ClientDashboardSummary;
import com.court.hearing.dto.HearingResponse;
import com.court.hearing.dto.ReminderResponse;
import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.User;
import com.court.hearing.repository.HearingRepository;
import com.court.hearing.service.HearingService;
import com.court.hearing.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Client-only endpoints.
 * All methods are restricted to ROLE_CLIENT.
 * Data is always scoped to the authenticated client's email — no cross-client access is possible.
 */
@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CLIENT')")
public class ClientController {

    private final HearingService hearingService;
    private final ReminderService reminderService;
    private final HearingRepository hearingRepository;

    /**
     * Client dashboard summary — counts scoped to the authenticated client.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardSummary> getDashboard(
            @AuthenticationPrincipal UserDetails currentUser) {

        String email = resolveEmail(currentUser);
        String name = resolveFullName(currentUser);
        LocalDate today = LocalDate.now();

        ClientDashboardSummary summary = ClientDashboardSummary.builder()
                .clientEmail(email)
                .clientName(name)
                .totalHearings(hearingRepository.countByClientEmail(email))
                .upcomingHearings(hearingRepository.countUpcomingForClient(email, today))
                .scheduledHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.SCHEDULED))
                .pendingHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.PENDING))
                .inProgressHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.IN_PROGRESS))
                .completedHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.COMPLETED))
                .adjournedHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.ADJOURNED))
                .cancelledHearings(hearingRepository.countByClientEmailAndStatus(email, Hearing.Status.CANCELLED))
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Paginated list of all hearings associated with the authenticated client.
     */
    @GetMapping("/hearings")
    public ResponseEntity<Page<HearingResponse>> getMyHearings(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "hearingDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hearingService.getClientHearings(resolveEmail(currentUser), pageable));
    }

    /**
     * Single hearing detail — only accessible if the hearing belongs to the authenticated client.
     */
    @GetMapping("/hearings/{id}")
    public ResponseEntity<HearingResponse> getMyHearing(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(hearingService.getClientHearingById(id, resolveEmail(currentUser)));
    }

    /**
     * Upcoming (non-terminal) hearings for the authenticated client.
     */
    @GetMapping("/hearings/upcoming")
    public ResponseEntity<List<HearingResponse>> getMyUpcomingHearings(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(hearingService.getClientUpcomingHearings(resolveEmail(currentUser)));
    }

    /**
     * Reminders associated with the authenticated client's hearings.
     */
    @GetMapping("/reminders")
    public ResponseEntity<List<ReminderResponse>> getMyReminders(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(reminderService.getClientReminders(resolveEmail(currentUser)));
    }

    private String resolveEmail(UserDetails user) {
        return user != null ? user.getUsername() : null;
    }

    private String resolveFullName(UserDetails user) {
        if (user instanceof User u) {
            return u.getFullName();
        }
        return user != null ? user.getUsername() : "Client";
    }
}

