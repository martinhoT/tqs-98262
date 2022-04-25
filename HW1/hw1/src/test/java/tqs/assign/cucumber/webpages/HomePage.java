package tqs.assign.cucumber.webpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HomePage implements WebPage {

    private final WebDriver driver;

    @FindBy(id="chk-world")
    private WebElement chkWorld;

    @FindBy(id="date-before")
    private WebElement dateBefore;

    @FindBy(id="date-after")
    private WebElement dateAfter;

    @FindBy(id="date-at")
    private WebElement dateAt;

    @FindBy(id="select-country")
    private WebElement selectCountry;

    @FindBy(id="covid-stats-submit")
    private WebElement btnCovidStatsSubmit;

    @FindBy(id="cache-stats-submit")
    private WebElement btnCacheStatsSubmit;

    public HomePage(WebDriver driver, int port) {
        this.driver = driver;

        driver.get("http://localhost:" + port);

        PageFactory.initElements(driver, this);
    }

    public void setWorld(boolean check) {
        if ( (!chkWorld.isSelected() && check) || (chkWorld.isSelected() && !check) )
            chkWorld.click();

        boolean selectCountryIsEditable = selectCountry.isEnabled() && selectCountry.getAttribute("readonly") == null;
        if (check)
            assertFalse(selectCountryIsEditable);
        else
            assertTrue(selectCountryIsEditable);
    }

    public void setDateBefore(LocalDate date) {
        dateBefore.click();
        dateBefore.sendKeys(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public void setDateAfter(LocalDate date) {
        dateAfter.click();
        dateAfter.sendKeys(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public void setDateAt(LocalDate date) {
        dateAt.click();
        dateAt.sendKeys(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public void setCountry(String country) {
        selectCountry.findElement(By.xpath("//option[. = '%s']".formatted(country))).click();
    }

    public WebPage submitToCovidStats() {
        btnCovidStatsSubmit.click();
        return new CovidStatsPage(driver);
    }

    public WebPage submitToCacheStats() {
        btnCacheStatsSubmit.click();
        return new CacheStatsPage(driver);
    }

    @Override
    public boolean isOpened() {
        return driver.getTitle().equals("Covid-19 Data API");
    }

}
