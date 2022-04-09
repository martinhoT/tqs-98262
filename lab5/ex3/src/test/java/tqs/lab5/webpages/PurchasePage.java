package tqs.lab5.webpages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PurchasePage implements WebPage {

    private final WebDriver driver;

    @FindBy(id="inputName")
    private WebElement name;
    @FindBy(id="address")
    private WebElement address;
    @FindBy(id="city")
    private WebElement city;
    @FindBy(id="state")
    private WebElement state;
    @FindBy(id="zipCode")
    private WebElement zipCode;
    @FindBy(id="creditCardNumber")
    private WebElement creditCardNumber;

    @FindBy(id="cardType")
    private WebElement dropdownCardType;

    @FindBy(css=".btn-primary")
    private WebElement buttonPurchaseFlight;

    PurchasePage(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    public void setName(String input) {
        name.clear();
        name.sendKeys(input);
    }
    public void setAddress(String input) {
        address.clear();
        address.sendKeys(input);
    }
    public void setCity(String input) {
        city.clear();
        city.sendKeys(input);
    }
    public void setState(String input) {
        state.clear();
        state.sendKeys(input);
    }
    public void setZipCode(String input) {
        zipCode.clear();
        zipCode.sendKeys(input);
    }
    public void setCreditCardNumber(String input) {
        creditCardNumber.clear();
        creditCardNumber.sendKeys(input);
    }

    public void chooseOptionOnDropdownCardType(String option) {
        String xPath = String.format("//option[. = '%s']", option);
        dropdownCardType.findElement(By.xpath(xPath)).click();
    }

    public ConfirmPage clickOnButtonPurchaseFlight() {
        buttonPurchaseFlight.click();
        return new ConfirmPage(driver);
    }

    @Override
    public boolean isOpened() {
        return driver.getTitle().equals("BlazeDemo Purchase");
    }

}
