package com.court.hearing.service;

import com.court.hearing.dto.HearingRequest;
import com.court.hearing.dto.HearingResponse;
import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.entity.User;
import com.court.hearing.exception.ConflictException;
import com.court.hearing.exception.ForbiddenException;
import com.court.hearing.exception.NotFoundException;
import com.court.hearing.exception.StatusTransitionException;
import com.court.hearing.repository.HearingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HearingService {

    private final HearingRepository hearingRepository;

    // Status transition map: from -> allowed to
    private static final java.util.Map<Status, Set<Status>> ALLOWED_TRANSITIONS = new java.util.HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(Status.SCHEDULED, EnumSet.of(Status.PENDING, Status.IN_PROGRESS, Status.ADJOURNED, Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Status.PENDING, EnumSet.of(Status.SCHEDULED, Status.IN_PROGRESS, Status.ADJOURNED, Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Status.IN_PROGRESS, EnumSet.of(Status.COMPLETED, Status.ADJOURNED, Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Status.ADJOURNED, EnumSet.of(Status.SCHEDULED, Status.PENDING, Status.CANCELLED));
        ALLOWED_TRANSITIONS.put(Status.COMPLETED, EnumSet.noneOf(Status.class));
        ALLOWED_TRANSITIONS.put(Status.CANCELLED, EnumSet.noneOf(Status.class));
    }

    public HearingResponse createHearing(HearingRequest request) {
        String username = getCurrentUsername();
        if (hearingRepository.existsByCaseNumber(request.getCaseNumber())) {
            throw new ConflictException("Case number already exists: " + request.getCaseNumber());
        }
        Hearing hearing = Hearing.builder()
                .caseNumber(request.getCaseNumber())
                .caseTitle(request.getCaseTitle())
                .petitioner(request.getPetitioner())
                .respondent(request.getRespondent())
                .courtName(request.getCourtName())
                .hearingDate(request.getHearingDate())
                .hearingTime(request.getHearingTime())
                .judgeName(request.getJudgeName())
                .assignedLawyer(request.getAssignedLawyer())
                .caseType(request.getCaseType())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Status.SCHEDULED)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .createdBy(username)
                .updatedBy(username)
                .build();
        Hearing saved = hearingRepository.save(hearing);
        log.info("Hearing created: {} by {}", saved.getCaseNumber(), username);
        return HearingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<HearingResponse> getAllHearings(Pageable pageable) {
        return hearingRepository.findAll(pageable).map(HearingResponse::from);
    }

    @Transactional(readOnly = true)
    public HearingResponse getHearingById(Long id) {
        return HearingResponse.from(findById(id));
    }

    public HearingResponse updateHearing(Long id, HearingRequest request) {
        String username = getCurrentUsername();
        Hearing existing = findById(id);

        if (hearingRepository.existsByCaseNumberAndIdNot(request.getCaseNumber(), id)) {
            throw new ConflictException("Case number already exists: " + request.getCaseNumber());
        }

        existing.setCaseNumber(request.getCaseNumber());
        existing.setCaseTitle(request.getCaseTitle());
        existing.setPetitioner(request.getPetitioner());
        existing.setRespondent(request.getRespondent());
        existing.setCourtName(request.getCourtName());
        existing.setHearingDate(request.getHearingDate());
        existing.setHearingTime(request.getHearingTime());
        existing.setJudgeName(request.getJudgeName());
        existing.setAssignedLawyer(request.getAssignedLawyer());
        existing.setCaseType(request.getCaseType());
        existing.setDescription(request.getDescription());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());
        if (request.getPriority() != null) existing.setPriority(request.getPriority());
        existing.setUpdatedBy(username);

        Hearing saved = hearingRepository.save(existing);
        log.info("Hearing updated: {} by {}", saved.getCaseNumber(), username);
        return HearingResponse.from(saved);
    }

    public void deleteHearing(Long id) {
        Hearing hearing = findById(id);
        hearingRepository.delete(hearing);
        log.info("Hearing deleted: {} by {}", hearing.getCaseNumber(), getCurrentUsername());
    }

    public HearingResponse updateStatus(Long id, Status newStatus) {
        String username = getCurrentUsername();
        User currentUser = getCurrentUser();
        Hearing hearing = findById(id);

        Status currentStatus = hearing.getStatus();
        validateStatusTransition(currentStatus, newStatus);
        validateRoleForStatusChange(currentUser, newStatus);

        hearing.setStatus(newStatus);
        hearing.setUpdatedBy(username);
        Hearing saved = hearingRepository.save(hearing);
        log.info("Hearing {} status changed: {} -> {} by {}", hearing.getCaseNumber(), currentStatus, newStatus, username);
        return HearingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<HearingResponse> searchHearings(
            String caseNumber, String caseTitle, String courtName,
            String judgeName, String assignedLawyer,
            Status status, Priority priority, LocalDate hearingDate,
            Pageable pageable) {
        return hearingRepository.searchHearings(
                nullIfBlank(caseNumber), nullIfBlank(caseTitle), nullIfBlank(courtName),
                nullIfBlank(judgeName), nullIfBlank(assignedLawyer),
                status, priority, hearingDate, pageable
        ).map(HearingResponse::from);
    }

    @Transactional(readOnly = true)
    public List<HearingResponse> getUpcomingHearings(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        return hearingRepository.findUpcomingHearings(from, to)
                .stream().map(HearingResponse::from).toList();
    }

    // ── Client-scoped methods ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<HearingResponse> getClientHearings(String clientEmail, Pageable pageable) {
        return hearingRepository.findByClientEmail(clientEmail, pageable).map(HearingResponse::from);
    }

    @Transactional(readOnly = true)
    public HearingResponse getClientHearingById(Long id, String clientEmail) {
        Hearing hearing = hearingRepository.findByIdAndClientEmail(id, clientEmail)
                .orElseThrow(() -> new NotFoundException("Hearing not found or not authorized for this client"));
        return HearingResponse.from(hearing);
    }

    @Transactional(readOnly = true)
    public List<HearingResponse> getClientUpcomingHearings(String clientEmail) {
        return hearingRepository.findUpcomingHearingsForClient(clientEmail, LocalDate.now())
                .stream().map(HearingResponse::from).toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Hearing findById(Long id) {
        return hearingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hearing not found with id: " + id));
    }

    private void validateStatusTransition(Status from, Status to) {
        Set<Status> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, EnumSet.noneOf(Status.class));
        if (!allowed.contains(to)) {
            throw new StatusTransitionException(
                    String.format("Cannot transition from %s to %s", from, to));
        }
    }

    private void validateRoleForStatusChange(User user, Status newStatus) {
        User.Role role = user.getRole();
        if (role == User.Role.CLERK) {
            // Clerks can only set SCHEDULED or PENDING
            if (newStatus != Status.SCHEDULED && newStatus != Status.PENDING) {
                throw new ForbiddenException("CLERK cannot set status to " + newStatus);
            }
        }
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new ForbiddenException("Not authenticated");
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
