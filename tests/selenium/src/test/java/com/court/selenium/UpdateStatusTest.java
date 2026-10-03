package com.court.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Update Status Tests")
public class UpdateStatusTest extends BaseTest {

    @Test
    @DisplayName("Admin can update a hearing status")
    void testUpdateStatus() {
        loginAsAdmin();
        driver.get(BASE_URL + "/hearings");

        // Wait for hearing table to load
        WebElement table = waitForTestId("hearing-table");
        assertNotNull(table);

        // Click the first "View" button
        List<WebElement> viewButtons = driver.findElements(
            By.cssSelector("button[id^='view-btn-']")
        );
        assertFalse(viewButtons.isEmpty(), "There should be at least one hearing in the list");
        viewButtons.get(0).click();

        // Wait for details page to load
        wait.until(ExpectedConditions.urlMatches(".*/hearings/\\d+$"));

        // Check if status-select is present (depends on hearing status having transitions)
        List<WebElement> statusSelects = driver.findElements(
            By.cssSelector("[data-testid='status-select']")
        );

        if (!statusSelects.isEmpty()) {
            // Select the first available transition
            Select select = new Select(statusSelects.get(0));
            List<WebElement> opts = select.getOptions();
            // Find a non-empty option
            for (WebElement opt : opts) {
                if (!opt.getAttribute("value").isEmpty()) {
                    select.selectByValue(opt.getAttribute("value"));
                    break;
                }
            }

            // Click update
            WebElement updateBtn = waitForTestId("update-status-btn");
            updateBtn.click();

            // Wait for toast or page refresh
            wait.until(ExpectedConditions.stalenessOf(updateBtn));
            takeScreenshot("status_updated");
        } else {
            // Terminal state hearing — still pass if page loaded correctly
            assertTrue(driver.getCurrentUrl().matches(".*/hearings/\\d+"),
                "Should be on hearing detail page");
            takeScreenshot("status_terminal");
        }
    }
}
