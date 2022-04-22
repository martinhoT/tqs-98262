package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class UnsupportedCountryISOException extends RuntimeException {

    public UnsupportedCountryISOException(String countryISO) {
        super("The specified country ISO code '%s' does not exist in this platform".formatted(countryISO));
    }

}
