package tqs.assign.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
        code = HttpStatus.GATEWAY_TIMEOUT,
        reason = "The specified resource is unavailable, since no external data providers can fulfill the request at the moment")
public class UnavailableApiException extends RuntimeException {
}
