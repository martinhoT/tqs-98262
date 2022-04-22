package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public abstract class IncorrectlyFormattedParametersException extends RuntimeException {

    protected IncorrectlyFormattedParametersException(String argType, String argSupplied, String argFormat) {
        super("%s argument '%s' is not properly formatted (%s)".formatted(argType, argSupplied, argFormat));
    }

}
