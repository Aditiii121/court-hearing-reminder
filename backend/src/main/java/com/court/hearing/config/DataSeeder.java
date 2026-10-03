package com.court.hearing.config;

import com.court.hearing.entity.Hearing;
import com.court.hearing.entity.Hearing.Priority;
import com.court.hearing.entity.Hearing.Status;
import com.court.hearing.entity.User;
import com.court.hearing.entity.User.Role;
import com.court.hearing.repository.HearingRepository;
import com.court.hearing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final HearingRepository hearingRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() == 0) {
                seedUsers();
            }
            if (hearingRepository.count() == 0) {
                seedHearings();
            }
            log.info("Data seeding complete. Users: {}, Hearings: {}",
                    userRepository.count(), hearingRepository.count());
        };
    }

    private void seedUsers() {
        log.info("Seeding demo users...");
        // Passwords: Admin@123, Lawyer@123, Clerk@123
        userRepository.save(User.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("Admin User")
                .role(Role.ADMIN)
                .enabled(true)
                .build());
        userRepository.save(User.builder()
                .email("lawyer@example.com")
                .password(passwordEncoder.encode("Lawyer@123"))
                .fullName("Jane Lawyer")
                .role(Role.LAWYER)
                .enabled(true)
                .build());
        userRepository.save(User.builder()
                .email("clerk@example.com")
                .password(passwordEncoder.encode("Clerk@123"))
                .fullName("John Clerk")
                .role(Role.CLERK)
                .enabled(true)
                .build());
        log.info("Seeded 3 demo users");
    }

    private void seedHearings() {
        log.info("Seeding demo hearings...");
        LocalDate today = LocalDate.now();

        // Within 24 hours (today + tomorrow)
        save("CASE-2024-001", "State vs. Ramesh Kumar", "State of Karnataka", "Ramesh Kumar",
                "District Court, Bengaluru", today, LocalTime.of(10, 0), "Hon. Justice Sharma",
                "lawyer@example.com", "Criminal", Status.SCHEDULED, Priority.URGENT,
                "admin@example.com", "Bail hearing for accused");

        save("CASE-2024-002", "Priya Mehta vs. ABC Corp", "Priya Mehta", "ABC Corporation",
                "High Court, Mumbai", today, LocalTime.of(14, 30), "Hon. Justice Patel",
                "lawyer@example.com", "Civil", Status.PENDING, Priority.HIGH,
                "clerk@example.com", "Preliminary hearing on contract dispute");

        // Within 7 days
        save("CASE-2024-003", "Raj Properties vs. City Council", "Raj Properties Ltd", "City Municipal Council",
                "City Civil Court, Chennai", today.plusDays(2), LocalTime.of(11, 0), "Hon. Justice Reddy",
                "lawyer@example.com", "Property", Status.SCHEDULED, Priority.MEDIUM,
                "admin@example.com", "Land acquisition appeal");

        save("CASE-2024-004", "Insurance Claim - Sharma Family", "Sharma Family", "National Insurance Co.",
                "Consumer Forum, Hyderabad", today.plusDays(3), LocalTime.of(10, 30), "Presiding Officer Gupta",
                "lawyer@example.com", "Consumer", Status.SCHEDULED, Priority.LOW,
                "clerk@example.com", "Delayed insurance claim settlement");

        save("CASE-2024-005", "Amit Sinha vs. Employer", "Amit Sinha", "Tech Solutions Pvt Ltd",
                "Labour Court, Pune", today.plusDays(5), LocalTime.of(9, 0), "Labour Judge Mishra",
                "lawyer@example.com", "Labour", Status.SCHEDULED, Priority.HIGH,
                "admin@example.com", "Wrongful termination case");

        save("CASE-2024-006", "Tax Dispute - Verma Industries", "Verma Industries", "Income Tax Department",
                "Tax Tribunal, New Delhi", today.plusDays(6), LocalTime.of(15, 0), "Tax Judge Agrawal",
                "lawyer@example.com", "Tax", Status.PENDING, Priority.URGENT,
                "clerk@example.com", "Assessment year 2023-24 dispute");

        // Future (more than 7 days)
        save("CASE-2024-007", "Divorce Petition - Singh vs Singh", "Geeta Singh", "Harish Singh",
                "Family Court, Jaipur", today.plusDays(14), LocalTime.of(11, 30), "Family Judge Sharma",
                "lawyer@example.com", "Family", Status.SCHEDULED, Priority.MEDIUM,
                "admin@example.com", "Child custody proceedings");

        save("CASE-2024-008", "Patent Infringement - TechStart", "TechStart Innovations", "Global Tech Corp",
                "Intellectual Property Division, Delhi", today.plusDays(21), LocalTime.of(10, 0), "IP Judge Nair",
                "lawyer@example.com", "IP", Status.SCHEDULED, Priority.HIGH,
                "clerk@example.com", "Software patent violation claim");

        save("CASE-2024-009", "Environmental Violation - Factory", "Pollution Control Board", "Green Valley Factory",
                "National Green Tribunal, Mumbai", today.plusDays(30), LocalTime.of(14, 0), "NGT Judge Pillai",
                "lawyer@example.com", "Environmental", Status.PENDING, Priority.URGENT,
                "admin@example.com", "River pollution case");

        // Past hearings
        save("CASE-2024-010", "Traffic Accident Claim", "Suresh Nair", "City Transport Corp",
                "Motor Accident Claims Tribunal, Kochi", today.minusDays(7), LocalTime.of(10, 0), "MACT Judge Joseph",
                "lawyer@example.com", "Civil", Status.COMPLETED, Priority.LOW,
                "clerk@example.com", "Compensation hearing concluded");

        save("CASE-2024-011", "Fraud Case - Bank vs Gupta", "Punjab National Bank", "Rajesh Gupta",
                "Special CBI Court, New Delhi", today.minusDays(14), LocalTime.of(9, 30), "Special Judge Mehra",
                "lawyer@example.com", "Criminal", Status.ADJOURNED, Priority.HIGH,
                "admin@example.com", "Adjourned - witness not available");

        save("CASE-2024-012", "Property Dispute - Brothers", "Mohan Lal", "Sohan Lal",
                "Civil Court, Lucknow", today.minusDays(3), LocalTime.of(10, 30), "Civil Judge Tripathi",
                "lawyer@example.com", "Property", Status.IN_PROGRESS, Priority.MEDIUM,
                "clerk@example.com", "Evidence recording in progress");

        save("CASE-2024-013", "Cheque Bounce Case", "Creditor Finance Ltd", "Dinesh Patel",
                "Magistrate Court, Ahmedabad", today.minusDays(1), LocalTime.of(11, 0), "Magistrate Shah",
                "lawyer@example.com", "Criminal", Status.COMPLETED, Priority.LOW,
                "admin@example.com", "Convicted - sentence announced");

        save("CASE-2024-014", "Custody Battle - Kapoor Family", "Neha Kapoor", "Vikram Kapoor",
                "Family Court, Chandigarh", today.minusDays(5), LocalTime.of(14, 0), "Family Judge Malhotra",
                "lawyer@example.com", "Family", Status.CANCELLED, Priority.MEDIUM,
                "clerk@example.com", "Settled out of court");

        save("CASE-2024-015", "Municipal Tax Appeal", "Bharat Steel Ltd", "Municipal Corporation",
                "Revenue Court, Bhopal", today.plusDays(1), LocalTime.of(10, 0), "Revenue Judge Saxena",
                "lawyer@example.com", "Tax", Status.SCHEDULED, Priority.HIGH,
                "admin@example.com", "Annual property tax reassessment");

        log.info("Seeded 15 demo hearings");
    }

    private void save(String caseNum, String title, String petitioner, String respondent,
                      String court, LocalDate date, LocalTime time, String judge,
                      String lawyer, String caseType, Status status, Priority priority,
                      String createdBy, String description) {
        hearingRepository.save(Hearing.builder()
                .caseNumber(caseNum)
                .caseTitle(title)
                .petitioner(petitioner)
                .respondent(respondent)
                .courtName(court)
                .hearingDate(date)
                .hearingTime(time)
                .judgeName(judge)
                .assignedLawyer(lawyer)
                .caseType(caseType)
                .status(status)
                .priority(priority)
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .description(description)
                .build());
    }
}
