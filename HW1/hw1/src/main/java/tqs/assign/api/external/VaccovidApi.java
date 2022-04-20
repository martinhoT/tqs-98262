package tqs.assign.api.external;

import io.netty.handler.timeout.ReadTimeoutException;
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
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.Duration;

@Component
public class VaccovidApi implements Api {

    private static final String API_HOST = "vaccovid-coronavirus-vaccine-and-treatment-tracker.p.rapidapi.com";
    private static final String BASE_URL = String.format("https://%s/api/", API_HOST);
    private static final Logger logger = LogManager.getLogger(VaccovidApi.class);

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
    public Stats getGlobalStats() {
        return apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("npm-covid-data/world")
                        .build())
                .exchangeToMono(response -> response.bodyToMono(Stats.class))
                .onErrorMap(ReadTimeoutException.class, ex -> new UnavailableExternalApiException())
                .block();
    }

    @Override
    public Stats getCountryStats(String countryISO) {
        return apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("api-covid-data/reports/{country-iso-code}")
                        .build(countryISO))
                .exchangeToMono(response -> response.bodyToMono(Stats.class))
                .onErrorMap(ReadTimeoutException.class, ex -> new UnavailableExternalApiException())
                .block();
    }

    @Override
    public Stats getStats(ApiQuery query) {
        // TODO
        return null;
    }

}
