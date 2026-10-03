package com.court.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dashboard Tests")
public class DashboardTest extends BaseTest {

    @Test
    @DisplayName("Dashboard loads with non-zero hearing counts")
    void testDashboardStatCards() {
        loginAsAdmin();

        // Should be on dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // Dashboard summary container exists
        WebElement summary = waitForTestId("dashboard-summary");
        assertNotNull(summary, "Dashboard summary should be visible");

        // Stat cards should show numbers — find all stat-value elements
        java.util.List<WebElement> statValues = driver.findElements(By.className("stat-value"));
        assertFalse(statValues.isEmpty(), "Dashboard should have stat cards");

        // At least total hearings should be > 0 (seeded 15 hearings)
        boolean foundNonZero = statValues.stream().anyMatch(el -> {
            try {
                int val = Integer.parseInt(el.getText().trim());
                return val > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        assertTrue(foundNonZero, "At least one stat card should show a non-zero value (seeded data)");
        takeScreenshot("dashboard_stats");
    }

    @Test
    @DisplayName("Dashboard has charts rendered")
    void testDashboardCharts() {
        loginAsAdmin();
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        waitForTestId("dashboard-summary");

        // Recharts renders SVG elements
        java.util.List<WebElement> svgs = driver.findElements(By.tagName("svg"));
        assertFalse(svgs.isEmpty(), "Dashboard should contain chart SVG elements");
        takeScreenshot("dashboard_charts");
    }
}
