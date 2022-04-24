package tqs.assign.api.external;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static tqs.assign.Utils.gson;

@Component
public class JohnsHopkinsApi implements Api {

    private static final String API_HOST = "covid-19-statistics.p.rapidapi.com";
    private static final String BASE_URL = String.format("https://%s/api/", API_HOST);

    @Value("${rapid-api.key}")
    private String apiKey;

    private final WebClient webClient;

    private final Set<JohnsHopkinsCountry> countries;

    private boolean unauthorized;



    public JohnsHopkinsApi(String baseUrl) {
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-RapidAPI-Host", API_HOST)
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                ))
                .build();

        String jsonResponse = null;
        unauthorized = false;
        try {
            jsonResponse = webClient.get()
                    .uri("/regions")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized ex) {
            unauthorized = true;
        }

        countries = new HashSet<>();
        if (jsonResponse != null) {
            JsonObject jsonRoot = gson.fromJson(jsonResponse, JsonObject.class);
            jsonRoot.getAsJsonArray("data").forEach(
                    jElem -> countries.add(gson.fromJson(jElem, JohnsHopkinsCountry.class))
            );
        }
    }

    public JohnsHopkinsApi() {
        this(BASE_URL);
    }



    @Override
    public Stats getStats(ApiQuery query) {
        if (unauthorized ||
                query.getAfter() != null || query.getBefore() != null)
            throw new UnavailableExternalApiException();

        MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());

        if (query.getAtDate() != null)
            queryParams.add("date", query.getAtDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

        boolean hasCountry = query.getAtCountry() != null;
        val location = hasCountry ? "/reports" : "/reports/total";
        if (hasCountry)
            queryParams.add("iso", query.getAtCountry());

        String jsonResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(location)
                        .queryParams(queryParams)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonObject jsonRoot = gson.fromJson(jsonResponse, JsonObject.class);
        JsonElement jsonData = jsonRoot.get("data");
        if (jsonData.isJsonArray())
            throw new UnavailableExternalApiException();
        JohnsHopkinsStats apiStats = gson.fromJson(jsonData, JohnsHopkinsStats.class);

        if (apiStats == null)
            throw new UnavailableExternalApiException();

        return new Stats(
                apiStats.getConfirmed(),
                apiStats.getConfirmedDiff(),
                apiStats.getDeaths(),
                apiStats.getDeathsDiff(),
                apiStats.getRecovered(),
                apiStats.getRecoveredDiff(),
                apiStats.getActive(),
                apiStats.getActiveDiff(),
                apiStats.getFatalityRate()
        );
    }

    @Override
    public Set<String> getSupportedCountries() {
        return countries.stream().map(JohnsHopkinsCountry::getIso).collect(Collectors.toSet());
    }



    @Getter
    @AllArgsConstructor
    static class JohnsHopkinsCountry {
        private final String iso;
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    static class JohnsHopkinsStats {
        private final LocalDate date;
        private final int confirmed;
        private final int deaths;
        private final int recovered;
        private final int active;
        @SerializedName("confirmed_diff") private final int confirmedDiff;
        @SerializedName("deaths_diff") private final int deathsDiff;
        @SerializedName("recovered_diff") private final int recoveredDiff;
        @SerializedName("active_diff") private final int activeDiff;
        @SerializedName("fatality_rate") private final double fatalityRate;
    }

}
