package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnsupportedCountryISOException extends ResponseStatusException {

    public UnsupportedCountryISOException(String countryISO) {
        super(HttpStatus.NOT_FOUND, "The specified country ISO code '%s' does not exist in this platform".formatted(countryISO));
    }

}
