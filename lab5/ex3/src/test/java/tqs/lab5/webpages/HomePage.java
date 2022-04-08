package tqs.lab5.webpages;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class HomePage {

    private final WebDriver driver;

    private static final String PAGE_URL = "https://blazedemo.com/";

    @FindBy(name="fromPort")
    private WebElement dropdownFromPort;

    @FindBy(name="toPort")
    private WebElement dropdownToPort;

    @FindBy(css=".btn-primary")
    private WebElement buttonFindFlights;

    public HomePage(WebDriver driver) {
        this.driver = driver;

        driver.get(PAGE_URL);
        driver.manage().window().setSize(new Dimension(679, 764));

        PageFactory.initElements(driver, this);
    }

    public void clickOnDropdownFromPort() { dropdownFromPort.click(); }
    public void clickOnDropdownToPort() { dropdownToPort.click(); }

    public void chooseOptionOnDropdownFromPort(String option) {
        String xPath = String.format("//option[. = '%s']", option);
        dropdownFromPort.findElement(By.xpath(xPath)).click();
    }

    public void chooseOptionOnDropdownToPort(String option) {
        String xPath = String.format("//option[. = '%s']", option);
        dropdownToPort.findElement(By.xpath(xPath)).click();
    }

    public ReservePage clickOnButtonFindFlights() {
        buttonFindFlights.click();
        return new ReservePage(driver);
    }

}
