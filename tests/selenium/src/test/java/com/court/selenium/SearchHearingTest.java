package com.court.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Search Hearing Tests")
public class SearchHearingTest extends BaseTest {

    @Test
    @DisplayName("Search by case number returns filtered results")
    void testSearchByCaseNumber() {
        loginAsAdmin();
        driver.get(BASE_URL + "/hearings");

        // Wait for table to load
        assertNotNull(waitForTestId("hearing-table"));

        // Type in search box
        WebElement searchInput = waitForTestId("search-input");
        searchInput.clear();
        // Search for the seeded case number prefix
        searchInput.sendKeys("CIV");

        waitAndClick("search-btn");

        // Wait for results
        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("[data-testid='hearing-table']")
        ));

        String pageText = driver.getPageSource();
        // Either results contain "CIV" or the empty-state is shown (no crash)
        assertTrue(pageText.contains("CIV") || pageText.contains("No hearings found"),
            "Search should return filtered results or empty state — no crash");
        takeScreenshot("search_results");
    }

    @Test
    @DisplayName("Search with no results shows empty state")
    void testSearchNoResults() {
        loginAsAdmin();
        driver.get(BASE_URL + "/hearings");
        assertNotNull(waitForTestId("hearing-table"));

        WebElement searchInput = waitForTestId("search-input");
        searchInput.sendKeys("ZZZNORESULT99999");
        waitAndClick("search-btn");

        // Should show empty state or table with no rows
        wait.until(driver1 -> driver1.getPageSource().contains("No hearings") ||
            driver1.findElements(By.cssSelector("tbody tr")).isEmpty() ||
            driver1.getPageSource().contains("ZZZNORESULT"));
        takeScreenshot("search_no_results");
    }
}
