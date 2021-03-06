package tqs.lab4;

// Generated by Selenium IDE

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

import io.github.bonigarcia.seljup.SeleniumJupiter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@ExtendWith(SeleniumJupiter.class)
public class PurchaseTest {

//  private WebDriver driver;
//  private Map<String, Object> vars;
//  JavascriptExecutor js;

//  @BeforeEach
//  public void setUp() {
//    driver = new FirefoxDriver();
//    js = (JavascriptExecutor) driver;
//    vars = new HashMap<>();
//  }

//public void purchaseTest() {
  @Test
  public void purchaseTest(FirefoxDriver driver) {
    driver.get("https://blazedemo.com/");
    driver.manage().window().setSize(new Dimension(679, 764));
    driver.findElement(By.name("fromPort")).click();
    {
      WebElement dropdown = driver.findElement(By.name("fromPort"));
      dropdown.findElement(By.xpath("//option[. = 'San Diego']")).click();
    }
    driver.findElement(By.cssSelector(".form-inline:nth-child(1) > option:nth-child(5)")).click();
    {
      WebElement element = driver.findElement(By.name("toPort"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).clickAndHold().perform();
    }
    {
      WebElement element = driver.findElement(By.name("toPort"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).perform();
    }
    {
      WebElement element = driver.findElement(By.name("toPort"));
      Actions builder = new Actions(driver);
      builder.moveToElement(element).release().perform();
    }
    driver.findElement(By.name("toPort")).click();
    {
      WebElement dropdown = driver.findElement(By.name("toPort"));
      dropdown.findElement(By.xpath("//option[. = 'Cairo']")).click();
    }
    driver.findElement(By.cssSelector(".form-inline:nth-child(4) > option:nth-child(7)")).click();
    driver.findElement(By.cssSelector(".btn-primary")).click();
    assertThat(driver.findElement(By.cssSelector("h3")).getText(), is("Flights from San Diego to Cairo:"));
    driver.findElement(By.cssSelector("tr:nth-child(4) .btn")).click();
    driver.findElement(By.cssSelector("p:nth-child(2)")).click();
    driver.findElement(By.cssSelector("p:nth-child(2)")).click();
    assertThat(driver.findElement(By.cssSelector("p:nth-child(2)")).getText(), is("Airline: United"));
    driver.findElement(By.id("inputName")).click();
    driver.findElement(By.id("inputName")).sendKeys("Bro moment");
    driver.findElement(By.id("address")).click();
    driver.findElement(By.id("address")).sendKeys("Address example");
    driver.findElement(By.id("city")).click();
    driver.findElement(By.id("city")).sendKeys("Portugal");
    driver.findElement(By.id("state")).sendKeys("Europe");
    driver.findElement(By.id("zipCode")).sendKeys("12345");
    driver.findElement(By.id("cardType")).click();
    {
      WebElement dropdown = driver.findElement(By.id("cardType"));
      dropdown.findElement(By.xpath("//option[. = 'American Express']")).click();
    }
    driver.findElement(By.cssSelector("option:nth-child(2)")).click();
    driver.findElement(By.id("creditCardNumber")).sendKeys("you wish");
    driver.findElement(By.cssSelector(".btn-primary")).click();
    driver.findElement(By.cssSelector("h1")).click();
    assertThat(driver.findElement(By.cssSelector("h1")).getText(), is("Thank you for your purchase today!"));
    assertThat(driver.getTitle(), is("BlazeDemo Confirmation"));
  }
}
