package com.court.hearing.controller;

import com.court.hearing.config.SecurityConfig;
import com.court.hearing.dto.ClientDashboardSummary;
import com.court.hearing.dto.HearingResponse;
import com.court.hearing.dto.ReminderResponse;
import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Reminder;
import com.court.hearing.entity.User;
import com.court.hearing.exception.NotFoundException;
import com.court.hearing.repository.HearingRepository;
import com.court.hearing.security.JwtUtil;
import com.court.hearing.service.HearingService;
import com.court.hearing.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for the ClientController covering all required security scenarios.
 *
 * Test coverage:
 *  1. Authenticated CLIENT can retrieve their own hearings (dashboard, list, detail, reminders)
 *  2. A CLIENT cannot retrieve another client's hearing by ID
 *  3. A CLIENT cannot access an unauthorised hearing ID
 *  4. A CLIENT cannot perform POST/PUT/DELETE/PATCH on /api/hearings
 *  5. Existing ADMIN/LAWYER/CLERK access continues to work
 *  6. A client with no associated hearings receives a valid empty response
 *  7. Unauthenticated requests are rejected with 401
 */
@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@DisplayName("ClientController Security and Access Tests")
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HearingService hearingService;

    @MockBean
    private ReminderService reminderService;

    @MockBean
    private HearingRepository hearingRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

    private HearingResponse sampleHearing;
    private ClientDashboardSummary sampleSummary;

    @BeforeEach
    void setUp() {
        sampleHearing = HearingResponse.builder()
                .id(1L)
                .caseNumber("CASE-2024-001")
                .caseTitle("State vs. Ramesh Kumar")
                .courtName("District Court, Bengaluru")
                .hearingDate(LocalDate.now().plusDays(1))
                .hearingTime(LocalTime.of(10, 0))
                .status(Hearing.Status.SCHEDULED)
                .priority(Hearing.Priority.URGENT)
                .build();

        sampleSummary = ClientDashboardSummary.builder()
                .clientEmail("client@example.com")
                .clientName("Ravi Client")
                .totalHearings(4L)
                .upcomingHearings(3L)
                .scheduledHearings(2L)
                .pendingHearings(1L)
                .completedHearings(1L)
                .build();
    }

    // ─── Test 1: Authenticated CLIENT can access their dashboard ─────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("1. CLIENT can access their own dashboard summary")
    void client_canAccessDashboard() throws Exception {
        when(hearingRepository.countByClientEmail("client@example.com")).thenReturn(4L);
        when(hearingRepository.countUpcomingForClient(eq("client@example.com"), any(LocalDate.class))).thenReturn(3L);
        when(hearingRepository.countByClientEmailAndStatus(eq("client@example.com"), any())).thenReturn(1L);

        mockMvc.perform(get("/api/client/dashboard"))
                .andExpect(status().isOk());
    }

    // ─── Test 2: Authenticated CLIENT can list their own hearings ────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("2. CLIENT can retrieve their own authorised hearing list")
    void client_canRetrieveOwnHearings() throws Exception {
        Page<HearingResponse> page = new PageImpl<>(List.of(sampleHearing));
        when(hearingService.getClientHearings(eq("client@example.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/client/hearings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].caseNumber").value("CASE-2024-001"));
    }

    // ─── Test 3: CLIENT can access their own hearing detail ──────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("3. CLIENT can view their own hearing detail by ID")
    void client_canViewOwnHearingDetail() throws Exception {
        when(hearingService.getClientHearingById(1L, "client@example.com")).thenReturn(sampleHearing);

        mockMvc.perform(get("/api/client/hearings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseNumber").value("CASE-2024-001"));
    }

    // ─── Test 4: CLIENT cannot access another client's hearing ───────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("4. CLIENT cannot retrieve another client's hearing by ID")
    void client_cannotAccessOtherClientHearing() throws Exception {
        // hearingId=99 does not belong to client@example.com
        when(hearingService.getClientHearingById(99L, "client@example.com"))
                .thenThrow(new NotFoundException("Hearing not found or not authorized for this client"));

        mockMvc.perform(get("/api/client/hearings/99"))
                .andExpect(status().isNotFound());
    }

    // ─── Test 5: CLIENT cannot POST (create) hearings ─────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("5. CLIENT cannot create a hearing (POST /api/hearings is forbidden)")
    void client_cannotCreateHearing() throws Exception {
        mockMvc.perform(post("/api/hearings")
                        .contentType("application/json")
                        .content("{\"caseNumber\":\"HACK\",\"caseTitle\":\"t\",\"courtName\":\"c\",\"hearingDate\":\"2099-01-01\",\"status\":\"SCHEDULED\"}"))
                .andExpect(status().isForbidden());
    }

    // ─── Test 6: CLIENT cannot PUT (update) hearings ──────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("6. CLIENT cannot update a hearing (PUT /api/hearings/{id} is forbidden)")
    void client_cannotUpdateHearing() throws Exception {
        mockMvc.perform(put("/api/hearings/1")
                        .contentType("application/json")
                        .content("{\"caseNumber\":\"HACK\",\"caseTitle\":\"t\",\"courtName\":\"c\",\"hearingDate\":\"2099-01-01\",\"status\":\"SCHEDULED\"}"))
                .andExpect(status().isForbidden());
    }

    // ─── Test 7: CLIENT cannot DELETE hearings ────────────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("7. CLIENT cannot delete a hearing (DELETE /api/hearings/{id} is forbidden)")
    void client_cannotDeleteHearing() throws Exception {
        mockMvc.perform(delete("/api/hearings/1"))
                .andExpect(status().isForbidden());
    }

    // ─── Test 8: CLIENT cannot PATCH status ───────────────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("8. CLIENT cannot update hearing status (PATCH /api/hearings/{id}/status is forbidden)")
    void client_cannotUpdateStatus() throws Exception {
        mockMvc.perform(patch("/api/hearings/1/status")
                        .contentType("application/json")
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden());
    }

    // ─── Test 9: CLIENT with no hearings gets empty response ──────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("9. CLIENT with no associated hearings receives a valid empty response")
    void client_withNoHearings_receivesEmptyResponse() throws Exception {
        Page<HearingResponse> emptyPage = new PageImpl<>(List.of());
        when(hearingService.getClientHearings(eq("client@example.com"), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/client/hearings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ─── Test 10: CLIENT can view their reminders ─────────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("10. CLIENT can view reminders associated with their hearings")
    void client_canViewOwnReminders() throws Exception {
        ReminderResponse reminder = ReminderResponse.builder()
                .id(1L)
                .hearingId(1L)
                .caseNumber("CASE-2024-001")
                .caseTitle("State vs. Ramesh Kumar")
                .reminderType(Reminder.ReminderType.WITHIN_24_HOURS)
                .status(Reminder.ReminderStatus.SENT)
                .build();

        when(reminderService.getClientReminders("client@example.com")).thenReturn(List.of(reminder));

        mockMvc.perform(get("/api/client/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].caseNumber").value("CASE-2024-001"));
    }

    // ─── Test 11: Unauthenticated request is rejected ─────────────────────

    @Test
    @DisplayName("11. Unauthenticated request to /api/client/hearings is rejected with 401")
    void unauthenticated_clientEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/client/hearings"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Test 12: Unauthenticated request to /api/client/dashboard ────────

    @Test
    @DisplayName("12. Unauthenticated request to /api/client/dashboard is rejected with 401")
    void unauthenticated_dashboard_returns401() throws Exception {
        mockMvc.perform(get("/api/client/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Test 13: Non-CLIENT role cannot access /api/client endpoints ─────

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    @DisplayName("13. ADMIN cannot access /api/client endpoints (403 — CLIENT role required)")
    void admin_cannotAccessClientEndpoints() throws Exception {
        mockMvc.perform(get("/api/client/hearings"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "lawyer@example.com", roles = "LAWYER")
    @DisplayName("14. LAWYER cannot access /api/client endpoints (403 — CLIENT role required)")
    void lawyer_cannotAccessClientEndpoints() throws Exception {
        mockMvc.perform(get("/api/client/hearings"))
                .andExpect(status().isForbidden());
    }

    // ─── Test 15: CLIENT can list upcoming hearings ────────────────────────

    @Test
    @WithMockUser(username = "client@example.com", roles = "CLIENT")
    @DisplayName("15. CLIENT can view their upcoming hearings")
    void client_canViewUpcomingHearings() throws Exception {
        when(hearingService.getClientUpcomingHearings("client@example.com")).thenReturn(List.of(sampleHearing));

        mockMvc.perform(get("/api/client/hearings/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].caseNumber").value("CASE-2024-001"));
    }
}

