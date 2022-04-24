package tqs.assign.api.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;

import java.time.Duration;
import java.util.Set;

@Component
public class VaccovidApi implements Api {

    private static final String API_HOST = "vaccovid-coronavirus-vaccine-and-treatment-tracker.p.rapidapi.com";
    private static final String BASE_URL = String.format("https://%s/api/", API_HOST);

    @Value("rapid-api.key")
    private static String apiKey;

    private static final WebClient apiClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-RapidAPI-Host", API_HOST)
            .defaultHeader("X-RapidAPI-Key", apiKey)
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(5))
            ))
            .build();

    @Override
    public Stats getStats(ApiQuery query) {
        // TODO
        return null;
    }

    @Override
    public Set<String> getSupportedCountries() {
        return null;
    }

}
