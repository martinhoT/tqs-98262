package tqs.lab4.webpages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class ReservePage {

    private final WebDriver driver;

    @FindBy(css="h3")
    private WebElement header;

    @FindBys(@FindBy(css="tr .btn"))
    private List<WebElement> reserveChoices;

    ReservePage(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    public String getTextHeader() {
        return header.getText();
    }

    public PurchasePage clickOnNthReserveChoice(int nth) {
        reserveChoices.get(nth).click();
        return new PurchasePage(driver);
    }

}
