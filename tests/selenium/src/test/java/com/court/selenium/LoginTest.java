package com.court.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Login Tests")
public class LoginTest extends BaseTest {

    @Test
    @DisplayName("Admin can log in successfully")
    void testLoginSuccess() {
        driver.get(BASE_URL + "/login");

        // Page has login form
        assertNotNull(waitForTestId("email-input"));
        assertNotNull(waitForTestId("password-input"));
        assertNotNull(waitForTestId("login-btn"));

        waitAndType("email-input", ADMIN_EMAIL);
        waitAndType("password-input", ADMIN_PASSWORD);
        waitAndClick("login-btn");

        // After login, should land on dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"),
            "Should redirect to dashboard after login");

        // Dashboard summary should be visible
        assertNotNull(waitForTestId("dashboard-summary"));
        takeScreenshot("login_success");
    }

    @Test
    @DisplayName("Invalid credentials show error message")
    void testLoginFailure() {
        driver.get(BASE_URL + "/login");

        waitAndType("email-input", "wrong@example.com");
        waitAndType("password-input", "wrongpassword");
        waitAndClick("login-btn");

        // Error alert should appear
        var errorEl = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("[data-testid='login-error']")
        ));
        assertNotNull(errorEl);
        assertFalse(errorEl.getText().isBlank(), "Error message should not be blank");
        takeScreenshot("login_failure");
    }

    @Test
    @DisplayName("Unauthenticated access redirects to login")
    void testProtectedRouteRedirect() {
        driver.get(BASE_URL + "/dashboard");
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"),
            "Unauthenticated /dashboard should redirect to /login");
    }
}
