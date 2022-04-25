package tqs.assign.cucumber.webpages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CacheStatsPage implements WebPage {

    private final WebDriver driver;

    @FindBy(id="header")
    private WebElement header;

    public CacheStatsPage(WebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }

    @Override
    public boolean isOpened() {
        return driver.getTitle().equals("Cache stats") && header.getText().equals("Cache stats");
    }

}
