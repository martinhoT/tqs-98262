package tqs.assign.cucumber;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.TestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.api.CovidCache;
import tqs.assign.cucumber.webpages.*;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration
public class CucumberSteps {

    // Javascript is used to enable/disable the countries dropdown
    private final WebDriver driver = new HtmlUnitDriver(true);

    private WebPage currentPage;

    @Autowired
    public CucumberSteps(CovidApi covidApi, CovidCache covidCache) {
        Map<ApiQuery, Stats> queryResponses = Map.of(
                ApiQuery.builder()
                        .atDate(LocalDate.of(2021, 1, 1))
                        .build(), TestUtils.randomStats(),
                ApiQuery.builder()
                        .after(LocalDate.of(2021, 12, 12))
                        .atCountry("PT")
                        .build(), TestUtils.randomStats()
        );

        Set<String> supportedCountries = Set.of("PT");

        ReflectionTestUtils.setField(covidApi, "supportedCountries", supportedCountries);
        covidCache.setTtl(Long.MAX_VALUE);
        queryResponses.forEach(covidCache::store);
    }

    @Given("I am in the {page} page")
    public void IAmInPage(WebPage page) {
        currentPage = page;
        assertTrue(page.isOpened());
    }

    @When("I {checkOp} to obtain world data")
    public void checkWorld(boolean check) {
        HomePage homePage = asHomePage();
        
        homePage.setWorld(check);
    }

    @When("I choose stats {beforeAfter} {isoLocalDate}")
    public void chooseStatsBeforeAfter(String beforeAfter, LocalDate date) {
        HomePage homePage = asHomePage();
        
        if (beforeAfter.equals("before"))
            homePage.setDateBefore(date);
        else
            homePage.setDateAfter(date);
    }

    @When("I choose the date {isoLocalDate}")
    public void chooseStatsAtDate(LocalDate date) {
        HomePage homePage = asHomePage();
        
        homePage.setDateAt(date);
    }
    
    @When("I choose the country {string}")
    public void chooseCountry(String country) {
        HomePage homePage = asHomePage();
        
        homePage.setCountry(country);
    }
    
    @When("I click {string} under the {dataSection} section")
    public void clickUnderSection(String clickable, String section) {
        HomePage homePage = asHomePage();

        if (clickable.equals("Submit")) {
            if (section.equals("covid"))
                currentPage = homePage.submitToCovidStats();
            else if (section.equals("cache"))
                currentPage = homePage.submitToCacheStats();
        }
    }

    @Then("I should receive {dataSection} stats from( the) {string}")
    public void receivedStatsFrom(String dataSection, String location) {
        if (dataSection.equals("covid")) {
            CovidStatsPage covidStatsPage = asCovidStatsPage();
            assertTrue(covidStatsPage.isOpened());

            if (location.equals("world"))
                assertTrue(covidStatsPage.isFromWorld());
            else
                assertTrue(covidStatsPage.isFromCountry(location));
        }
    }

    @Then("I should receive {dataSection} stats")
    public void receivedStats(String dataSection) {
        if (dataSection.equals("covid")) {
            CovidStatsPage covidStatsPage = asCovidStatsPage();
            assertTrue(covidStatsPage.isOpened());
        }
        else if (dataSection.equals("cache")) {
            CacheStatsPage cacheStatsPage = asCacheStatsPage();
            assertTrue(cacheStatsPage.isOpened());
        }
    }

    @Then("the date {dateField} field should be {isoLocalDate}")
    public void hasDateField(String dateField, LocalDate date) {
        CovidStatsPage covidStatsPage = asCovidStatsPage();

        assertTrue(covidStatsPage.requestDateFieldEquals(dateField, date));
    }

    @Then("no other date field should appear")
    public void noMoreDateFields() {
        CovidStatsPage covidStatsPage = asCovidStatsPage();

        assertTrue(covidStatsPage.noMoreDateFields());
    }



    @ParameterType("\\w+")
    public WebPage page(String pageName) {
        return pageName.equals("Home") ? new HomePage(driver, 8080) : new NullPage();
    }

    @ParameterType("covid|cache")
    public String dataSection(String dataSection) {
        return dataSection;
    }

    @ParameterType("check|uncheck")
    public boolean checkOp(String checkStr) {
        return checkStr.equals("check");
    }

    @ParameterType("before|after")
    public String beforeAfter(String beforeAfter) {
        return beforeAfter;
    }

    @ParameterType("'(before|after|at)'")
    public String dateField(String dateField) {
        return dateField;
    }

    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    public LocalDate isoLocalDate(String year, String month, String day){
        return LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
    }



    private HomePage asHomePage() {
        if (!(currentPage instanceof HomePage page))
            throw new IncorrectPageException(currentPage.getClass(), HomePage.class);

        return page;
    }

    private CovidStatsPage asCovidStatsPage() {
        if (!(currentPage instanceof CovidStatsPage page))
            throw new IncorrectPageException(currentPage.getClass(), CovidStatsPage.class);

        return page;
    }

    private CacheStatsPage asCacheStatsPage() {
        if (!(currentPage instanceof CacheStatsPage page))
            throw new IncorrectPageException(currentPage.getClass(), CacheStatsPage.class);

        return page;
    }

}
