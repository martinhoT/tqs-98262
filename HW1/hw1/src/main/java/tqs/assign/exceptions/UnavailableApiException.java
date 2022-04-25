package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnavailableApiException extends ResponseStatusException {

    public UnavailableApiException() {
        super(HttpStatus.OK, "The specified resource is unavailable, since no external data providers can fulfil the request at the moment");
    }

}
