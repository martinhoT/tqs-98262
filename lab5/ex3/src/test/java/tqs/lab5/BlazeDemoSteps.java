package tqs.lab5;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import tqs.lab5.webpages.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlazeDemoSteps {

    private final WebDriver driver = new FirefoxDriver();

    private WebPage currentPage;

    @Given("I am in the BlazeDemo {page} page")
    public void iAmOnPage(String page) {
        if (page.equals("Home"))
            currentPage = new HomePage(driver);
        else
            currentPage = new NullPage();
    }

    @When("I find flights from {string} to {string}")
    public void iFindFlights(String from, String to) {
        if (!(currentPage instanceof HomePage))
            throw new IncorrectPageException(currentPage.getClass(), HomePage.class);

        HomePage homePage = (HomePage) currentPage;

        homePage.chooseOptionOnDropdownFromPort(from);
        homePage.chooseOptionOnDropdownToPort(to);

        currentPage = homePage.clickOnButtonFindFlights();
    }

    @When("I choose the {int}st/nd/rd/th reservation")
    public void iChooseReservation(int reservation) {
        if (!(currentPage instanceof ReservePage))
            throw new IncorrectPageException(currentPage.getClass(), ReservePage.class);

        ReservePage reservePage = (ReservePage) currentPage;
        currentPage = reservePage.clickOnNthReserveChoice(4);
    }

    @When("I purchase with the following details")
    public void iPurchaseWithDetails(DataTable table) {
        if (!(currentPage instanceof PurchasePage))
            throw new IncorrectPageException(currentPage.getClass(), PurchasePage.class);

        PurchasePage purchasePage = (PurchasePage) currentPage;

        List<Map<String, String>> rows = table.asMaps();
        assertEquals(rows.size(), 1);
        Map<String, String> row = rows.get(0);

        purchasePage.setName(row.get("name"));
        purchasePage.setAddress(row.get("address"));
        purchasePage.setCity(row.get("city"));
        purchasePage.setState(row.get("state"));
        purchasePage.setZipCode(row.get("zipcode"));
        purchasePage.chooseOptionOnDropdownCardType(row.get("card type"));
        purchasePage.setCreditCardNumber(row.get("credit card number"));

        currentPage = purchasePage.clickOnButtonPurchaseFlight();
    }

    @Then("I should be in the {page} page")
    public void iShouldBeInPage(String pageName) {
        assertTrue(correctPageName(currentPage, pageName), String.format("expected page name %s for page %s.", pageName, currentPage));
        assertTrue(currentPage.isOpened());
    }

    @ParameterType("\\w+")
    public String page(String pageName) {
        return pageName;
    }

    private boolean correctPageName(WebPage page, String pageName) {
        String pageClassName = page.getClass().getSimpleName();
        return pageClassName.startsWith(pageName) && pageClassName.substring(pageName.length()).equals("Page");
    }

}
