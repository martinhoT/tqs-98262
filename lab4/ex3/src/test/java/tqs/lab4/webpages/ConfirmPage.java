package tqs.lab4.webpages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ConfirmPage {

    private final WebDriver driver;

    @FindBy(css="h1")
    private WebElement header;

    ConfirmPage(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    public boolean isOpened() {
        return header.getText().equals("Thank you for your purchase today!")
                && driver.getTitle().equals("BlazeDemo Confirmation");
    }

}
