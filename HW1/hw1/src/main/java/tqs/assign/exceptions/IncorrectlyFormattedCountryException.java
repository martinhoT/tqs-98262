package tqs.assign.exceptions;

public class IncorrectlyFormattedCountryException extends IncorrectlyFormattedParametersException {

    public IncorrectlyFormattedCountryException(String country) {
        super("Country", country, "ISO 3166-1 alpha code");
    }

}
