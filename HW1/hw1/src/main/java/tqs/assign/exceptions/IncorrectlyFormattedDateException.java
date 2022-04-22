package tqs.assign.exceptions;

public class IncorrectlyFormattedDateException extends IncorrectlyFormattedParametersException {

    public IncorrectlyFormattedDateException(String date) {
        super("Date", date, "ISO Local Date format: yyyy-MM-dd");
    }

}
