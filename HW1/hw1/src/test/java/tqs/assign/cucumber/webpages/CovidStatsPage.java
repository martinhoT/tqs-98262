package tqs.assign.cucumber.webpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class CovidStatsPage implements WebPage {

    private final WebDriver driver;

    @FindBy(id="country-str")
    private WebElement countryStr;

    @FindBy(id="date-before")
    private WebElement dateBefore;
    @FindBy(id="date-before-str")
    private WebElement dateBeforeStr;

    @FindBy(id="date-after")
    private WebElement dateAfter;
    @FindBy(id="date-after-str")
    private WebElement dateAfterStr;

    @FindBy(id="date-at")
    private WebElement dateAt;
    @FindBy(id="date-at-str")
    private WebElement dateAtStr;

    private final List<WebElement> uncheckedDateFields;

    public CovidStatsPage(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);

        List<WebElement> elementsFound = driver.findElements(By.id("date-at"));
        dateAt = elementsFound.isEmpty() ? null : elementsFound.get(0);
        elementsFound = driver.findElements(By.id("date-before"));
        dateBefore = elementsFound.isEmpty() ? null : elementsFound.get(0);
        elementsFound = driver.findElements(By.id("date-after"));
        dateAfter = elementsFound.isEmpty() ? null : elementsFound.get(0);

        uncheckedDateFields = Stream.of(
                dateBefore, dateAfter, dateAt
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean isFromWorld() {
        return countryStr.getText().equals("the world");
    }

    public boolean isFromCountry(String country) {
        return countryStr.getText().equals(country);
    }

    public boolean requestDateFieldEquals(String dateField, LocalDate date) {
        assertFalse(driver.findElements(By.id("date-" + dateField)).isEmpty());

        WebElement dateElem;
        WebElement dateElemStr;

        switch (dateField) {
            case "before":
                dateElem = dateBefore;
                dateElemStr = dateBeforeStr;
                break;
            case "after":
                dateElem = dateAfter;
                dateElemStr = dateAfterStr;
                break;
            case "at":
                dateElem = dateAt;
                dateElemStr = dateAtStr;
                break;
            default: return false;
        }

        uncheckedDateFields.remove(dateElem);

        return dateElemStr.getText().equals(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    public boolean noMoreDateFields() {
        return uncheckedDateFields.isEmpty();
    }

    @Override
    public boolean isOpened() {
        return driver.getTitle().equals("Covid stats");
    }

}
