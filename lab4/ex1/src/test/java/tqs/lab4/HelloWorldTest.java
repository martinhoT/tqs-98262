package tqs.lab4;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.lang.System.Logger;
import static java.lang.System.getLogger;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HelloWorldTest {

    private static final Logger logger = getLogger(HelloWorldTest.class.getSimpleName());

    private WebDriver driver;

    @BeforeEach
    void setup() {
        driver = new FirefoxDriver();
    }

    @AfterEach
    void teardown() {
        driver.quit();
    }

    @Test
    void test() {
        // Exercise
        String sutUrl = "https://bonigarcia.dev/selenium-webdriver-java/";
        driver.get(sutUrl);
        String title = driver.getTitle();
        logger.log(Logger.Level.DEBUG, "The title of {} is {}", sutUrl, title);

        // Verify
        assertThat(title, is("Hands-On Selenium WebDriver with Java"));
    }

}
