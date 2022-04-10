package tqs.assign.api.covid.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.api.covid.Api;

@Component
public class VaccovidApi implements Api {

    private static final String BASE_URL = "vaccovid-coronavirus-vaccine-and-treatment-tracker.p.rapidapi.com";

    @Value("rapid-api.key")
    private static String API_KEY;

}
