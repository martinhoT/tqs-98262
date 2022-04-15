package tqs.assign.api.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tqs.assign.api.Api;
import tqs.assign.data.Stats;

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
            .build();

    @Override
    public Stats getGlobalStats() {
        return apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("npm-covid-data/world")
                        .build())
                .exchangeToMono(response -> response.bodyToMono(Stats.class))
                .block();
    }

    @Override
    public Stats getStats(String countryIsoCode) {
        return apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("api-covid-data/reports/{country-iso-code}")
                        .build(countryIsoCode))
                .exchangeToMono(response -> response.bodyToMono(Stats.class))
                .block();
    }

}
