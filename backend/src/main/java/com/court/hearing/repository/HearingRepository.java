package com.court.hearing.repository;

import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Hearing.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HearingRepository extends JpaRepository<Hearing, Long> {

    boolean existsByCaseNumber(String caseNumber);

    boolean existsByCaseNumberAndIdNot(String caseNumber, Long id);

    @Query("""
        SELECT h FROM Hearing h WHERE
        (:caseNumber IS NULL OR LOWER(h.caseNumber) LIKE LOWER(CONCAT('%', :caseNumber, '%'))) AND
        (:caseTitle IS NULL OR LOWER(h.caseTitle) LIKE LOWER(CONCAT('%', :caseTitle, '%'))) AND
        (:courtName IS NULL OR LOWER(h.courtName) LIKE LOWER(CONCAT('%', :courtName, '%'))) AND
        (:judgeName IS NULL OR LOWER(h.judgeName) LIKE LOWER(CONCAT('%', :judgeName, '%'))) AND
        (:assignedLawyer IS NULL OR LOWER(h.assignedLawyer) LIKE LOWER(CONCAT('%', :assignedLawyer, '%'))) AND
        (:status IS NULL OR h.status = :status) AND
        (:priority IS NULL OR h.priority = :priority) AND
        (:hearingDate IS NULL OR h.hearingDate = :hearingDate)
    """)
    Page<Hearing> searchHearings(
        @Param("caseNumber") String caseNumber,
        @Param("caseTitle") String caseTitle,
        @Param("courtName") String courtName,
        @Param("judgeName") String judgeName,
        @Param("assignedLawyer") String assignedLawyer,
        @Param("status") Status status,
        @Param("priority") Hearing.Priority priority,
        @Param("hearingDate") LocalDate hearingDate,
        Pageable pageable
    );

    long countByStatus(Status status);

    List<Hearing> findByHearingDateBetweenAndStatusNot(
        LocalDate from, LocalDate to, Status status);

    List<Hearing> findByHearingDateAndStatusNot(LocalDate date, Status status);

    @Query("SELECT COUNT(h) FROM Hearing h WHERE h.hearingDate = :date AND h.status NOT IN ('COMPLETED','CANCELLED')")
    long countUpcomingToday(@Param("date") LocalDate date);

    @Query("SELECT COUNT(h) FROM Hearing h WHERE h.hearingDate BETWEEN :from AND :to AND h.status NOT IN ('COMPLETED','CANCELLED')")
    long countUpcomingNext7Days(@Param("from") LocalDate from, @Param("to") LocalDate to);

    long countByPriority(Hearing.Priority priority);

    List<Hearing> findByAssignedLawyer(String assignedLawyer);

    @Query("SELECT h FROM Hearing h WHERE h.hearingDate BETWEEN :from AND :to AND h.status NOT IN ('COMPLETED','CANCELLED') ORDER BY h.hearingDate, h.hearingTime")
    List<Hearing> findUpcomingHearings(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ── Client-scoped queries ──────────────────────────────────────────────

    Page<Hearing> findByClientEmail(String clientEmail, Pageable pageable);

    Optional<Hearing> findByIdAndClientEmail(Long id, String clientEmail);

    long countByClientEmail(String clientEmail);

    long countByClientEmailAndStatus(String clientEmail, Status status);

    @Query("""
        SELECT h FROM Hearing h
        WHERE h.clientEmail = :clientEmail
          AND h.hearingDate >= :from
          AND h.status NOT IN ('COMPLETED','CANCELLED')
        ORDER BY h.hearingDate ASC, h.hearingTime ASC
    """)
    List<Hearing> findUpcomingHearingsForClient(@Param("clientEmail") String clientEmail,
                                                 @Param("from") LocalDate from);

    @Query("""
        SELECT COUNT(h) FROM Hearing h
        WHERE h.clientEmail = :clientEmail
          AND h.hearingDate >= :from
          AND h.status NOT IN ('COMPLETED','CANCELLED')
    """)
    long countUpcomingForClient(@Param("clientEmail") String clientEmail, @Param("from") LocalDate from);
}
