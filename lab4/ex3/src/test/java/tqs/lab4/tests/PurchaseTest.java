package tqs.lab4.tests;

import static io.github.bonigarcia.seljup.BrowserType.CHROME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.bonigarcia.seljup.DockerBrowser;
import io.github.bonigarcia.seljup.SeleniumJupiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import tqs.lab4.webpages.ConfirmPage;
import tqs.lab4.webpages.HomePage;
import tqs.lab4.webpages.PurchasePage;
import tqs.lab4.webpages.ReservePage;

@ExtendWith(SeleniumJupiter.class)
public class PurchaseTest {

  @Test
  public void purchaseTestFirefox(FirefoxDriver driver) {
    purchaseTest(driver);
  }

  @Test
  public void purchaseTestHtmlUnit(HtmlUnitDriver driver) {
    purchaseTest(driver);
  }

  @Test
  public void purchaseTestDockerChrome(@DockerBrowser(type = CHROME) WebDriver driver) {
    purchaseTest(driver);
  }

  private void purchaseTest(WebDriver driver) {
      HomePage homePage = new HomePage(driver);

      homePage.clickOnDropdownFromPort();
      homePage.chooseOptionOnDropdownFromPort("San Diego");
      homePage.clickOnDropdownToPort();
      homePage.chooseOptionOnDropdownToPort("Cairo");
      ReservePage reservePage = homePage.clickOnButtonFindFlights();

      assertThat(reservePage.getTextHeader(), is("Flights from San Diego to Cairo:"));
      PurchasePage purchasePage = reservePage.clickOnNthReserveChoice(4);

      assertThat(purchasePage.getTextAirline(), is("Airline: United"));
      purchasePage.setName("Bro moment");
      purchasePage.setAddress("Address example");
      purchasePage.setCity("Portugal");
      purchasePage.setState("Europe");
      purchasePage.setZipCode("12345");
      purchasePage.clickOnDropdownCardType();
      purchasePage.chooseOptionOnDropdownCardType("American Express");
      purchasePage.setCreditCardNumber("you wish");
      ConfirmPage confirmPage = purchasePage.clickOnButtonPurchaseFlight();

      assertTrue(confirmPage.isOpened());
    }

}
