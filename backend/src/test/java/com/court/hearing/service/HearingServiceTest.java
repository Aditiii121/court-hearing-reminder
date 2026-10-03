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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HearingService Unit Tests")
class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingService hearingService;

    private User adminUser;
    private User lawyerUser;
    private User clerkUser;
    private Hearing sampleHearing;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L).email("admin@example.com")
                .fullName("Admin").role(User.Role.ADMIN).enabled(true).build();
        lawyerUser = User.builder()
                .id(2L).email("lawyer@example.com")
                .fullName("Lawyer").role(User.Role.LAWYER).enabled(true).build();
        clerkUser = User.builder()
                .id(3L).email("clerk@example.com")
                .fullName("Clerk").role(User.Role.CLERK).enabled(true).build();

        sampleHearing = Hearing.builder()
                .id(1L)
                .caseNumber("CASE-001")
                .caseTitle("Test vs Test")
                .courtName("Test Court")
                .hearingDate(LocalDate.now().plusDays(1))
                .hearingTime(LocalTime.of(10, 0))
                .status(Status.SCHEDULED)
                .priority(Priority.MEDIUM)
                .createdBy("admin@example.com")
                .updatedBy("admin@example.com")
                .build();
    }

    private void setAuthentication(User user) {
        var auth = new UsernamePasswordAuthenticationToken(
                user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Create hearing - success")
    void createHearing_success() {
        setAuthentication(adminUser);
        HearingRequest request = buildRequest("CASE-NEW", Status.SCHEDULED, Priority.HIGH);
        when(hearingRepository.existsByCaseNumber("CASE-NEW")).thenReturn(false);
        when(hearingRepository.save(any())).thenReturn(sampleHearing);

        HearingResponse response = hearingService.createHearing(request);

        assertThat(response).isNotNull();
        verify(hearingRepository).save(any(Hearing.class));
    }

    @Test
    @DisplayName("Create hearing - duplicate case number throws ConflictException")
    void createHearing_duplicateCaseNumber() {
        setAuthentication(adminUser);
        HearingRequest request = buildRequest("CASE-001", Status.SCHEDULED, Priority.MEDIUM);
        when(hearingRepository.existsByCaseNumber("CASE-001")).thenReturn(true);

        assertThatThrownBy(() -> hearingService.createHearing(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CASE-001");
    }

    @Test
    @DisplayName("Get hearing by ID - not found throws NotFoundException")
    void getHearingById_notFound() {
        when(hearingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingService.getHearingById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Get hearing by ID - found")
    void getHearingById_found() {
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));

        HearingResponse response = hearingService.getHearingById(1L);

        assertThat(response.getCaseNumber()).isEqualTo("CASE-001");
    }

    @Test
    @DisplayName("Status transition: SCHEDULED -> IN_PROGRESS is allowed")
    void statusTransition_scheduledToInProgress_allowed() {
        setAuthentication(adminUser);
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));
        when(hearingRepository.save(any())).thenReturn(sampleHearing);

        HearingResponse response = hearingService.updateStatus(1L, Status.IN_PROGRESS);

        assertThat(response).isNotNull();
        verify(hearingRepository).save(any());
    }

    @Test
    @DisplayName("Status transition: COMPLETED -> any is blocked")
    void statusTransition_completedToScheduled_blocked() {
        setAuthentication(adminUser);
        sampleHearing.setStatus(Status.COMPLETED);
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));

        assertThatThrownBy(() -> hearingService.updateStatus(1L, Status.SCHEDULED))
                .isInstanceOf(StatusTransitionException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("Status transition: CANCELLED -> any is blocked")
    void statusTransition_cancelledToAny_blocked() {
        setAuthentication(adminUser);
        sampleHearing.setStatus(Status.CANCELLED);
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));

        assertThatThrownBy(() -> hearingService.updateStatus(1L, Status.PENDING))
                .isInstanceOf(StatusTransitionException.class);
    }

    @Test
    @DisplayName("CLERK cannot set status to IN_PROGRESS")
    void clerkCannotSetInProgressStatus() {
        setAuthentication(clerkUser);
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));

        assertThatThrownBy(() -> hearingService.updateStatus(1L, Status.IN_PROGRESS))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("CLERK");
    }

    @Test
    @DisplayName("CLERK can set status to SCHEDULED")
    void clerkCanSetScheduledStatus() {
        setAuthentication(clerkUser);
        sampleHearing.setStatus(Status.PENDING);
        when(hearingRepository.findById(1L)).thenReturn(Optional.of(sampleHearing));
        when(hearingRepository.save(any())).thenReturn(sampleHearing);

        assertThatNoException().isThrownBy(() -> hearingService.updateStatus(1L, Status.SCHEDULED));
    }

    @Test
    @DisplayName("Delete hearing - not found")
    void deleteHearing_notFound() {
        setAuthentication(adminUser);
        when(hearingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingService.deleteHearing(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private HearingRequest buildRequest(String caseNumber, Status status, Priority priority) {
        HearingRequest req = new HearingRequest();
        req.setCaseNumber(caseNumber);
        req.setCaseTitle("Test Case");
        req.setCourtName("Test Court");
        req.setHearingDate(LocalDate.now().plusDays(5));
        req.setStatus(status);
        req.setPriority(priority);
        return req;
    }
}
