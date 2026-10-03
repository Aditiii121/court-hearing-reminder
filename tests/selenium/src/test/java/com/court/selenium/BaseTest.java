package com.court.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public abstract class BaseTest {

    protected static final String BASE_URL =
            System.getProperty("app.base.url", "http://localhost:80");

    protected static final String ADMIN_EMAIL    = "admin@example.com";
    protected static final String ADMIN_PASSWORD = "Admin@123";
    protected static final String LAWYER_EMAIL   = "lawyer@example.com";
    protected static final String LAWYER_PASSWORD = "Lawyer@123";

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
            "--headless=new",
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--window-size=1280,900"
        );
        driver = new ChromeDriver(options);
        wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- Helpers ----------

    protected void login(String email, String password) {
        driver.get(BASE_URL + "/login");
        waitAndType("email-input", email);
        waitAndType("password-input", password);
        waitAndClick("login-btn");
        // Wait for redirect to dashboard
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    protected void loginAsAdmin() {
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    protected WebElement waitForTestId(String testId) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("[data-testid='" + testId + "']")
        ));
    }

    protected void waitAndType(String testId, String text) {
        WebElement el = waitForTestId(testId);
        el.clear();
        el.sendKeys(text);
    }

    protected void waitAndClick(String testId) {
        waitForTestId(testId).click();
    }

    protected void takeScreenshot(String name) {
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(dir.resolve(name + ".png"), screenshot);
        } catch (IOException e) {
            System.err.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
