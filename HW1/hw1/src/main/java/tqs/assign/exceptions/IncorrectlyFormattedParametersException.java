package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public abstract class IncorrectlyFormattedParametersException extends ResponseStatusException {

    protected IncorrectlyFormattedParametersException(String argType, String argSupplied, String argFormat) {
        super(HttpStatus.BAD_REQUEST, "%s argument '%s' is not properly formatted (%s)".formatted(argType, argSupplied, argFormat));
    }

}
