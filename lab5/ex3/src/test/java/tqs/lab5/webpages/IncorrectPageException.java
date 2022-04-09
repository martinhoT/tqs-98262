package tqs.lab5.webpages;

public class IncorrectPageException extends RuntimeException {

    public IncorrectPageException(Class<? extends WebPage> currentPage, Class<? extends WebPage> correctPage) {
        super(String.format("Expected to be on %s, but was on %s.",
                correctPage.getSimpleName(),
                currentPage.getSimpleName()
        ));
    }

}
