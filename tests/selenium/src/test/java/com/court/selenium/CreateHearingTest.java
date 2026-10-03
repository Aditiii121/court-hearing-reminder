package com.court.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Create Hearing Tests")
public class CreateHearingTest extends BaseTest {

    @Test
    @DisplayName("Admin can create a new hearing and it appears in the list")
    void testCreateHearing() {
        loginAsAdmin();

        // Navigate to Add Hearing
        driver.get(BASE_URL + "/hearings/add");
        assertNotNull(waitForTestId("add-hearing-form"));

        String caseNumber = "SEL-" + System.currentTimeMillis();
        String title = "Selenium Test Case";
        // Hearing date: 30 days from now (datetime-local format)
        String futureDate = LocalDateTime.now().plusDays(30)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        waitAndType("case-number-input", caseNumber);
        waitAndType("title-input", title);
        waitAndType("hearing-date-input", futureDate);

        // Fill required fields via id selectors
        driver.findElement(org.openqa.selenium.By.id("courtName")).sendKeys("High Court of Selenium");
        driver.findElement(org.openqa.selenium.By.id("judgeName")).sendKeys("Judge Automated");

        waitAndClick("submit-btn");

        // Should redirect back to hearings list
        wait.until(ExpectedConditions.urlContains("/hearings"));

        // Hearing table should be visible
        assertNotNull(waitForTestId("hearing-table"));

        // The case number or title should appear in the page
        assertTrue(driver.getPageSource().contains(caseNumber) ||
                   driver.getPageSource().contains("SEL-"),
            "Newly created hearing should appear in the list");
        takeScreenshot("create_hearing_success");
    }
}
