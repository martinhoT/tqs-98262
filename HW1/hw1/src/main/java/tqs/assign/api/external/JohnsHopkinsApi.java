package tqs.assign.api.external;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.NullStats;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static tqs.assign.Utils.gson;

@Component
@Slf4j
public class JohnsHopkinsApi implements Api {

    private static final String API_HOST = "covid-19-statistics.p.rapidapi.com";
    private static final String BASE_URL = String.format("https://%s", API_HOST);

    @Getter
    private boolean enabled;

    private final WebClient webClient;

    private final Set<JohnsHopkinsCountry> countries;
    private final Map<String, String> iso3ToIso2Map;
    private boolean authorized;



    public JohnsHopkinsApi(final String baseUrl, final String apiKey, final boolean enabled, final boolean autoFetchCountries) {
        this.enabled = enabled;

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-RapidAPI-Host", API_HOST)
                .defaultHeader("X-RapidAPI-Key", apiKey)
                .filter(logRequest())
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                ))
                .build();

        String[] iso2Countries = Locale.getISOCountries();
        iso3ToIso2Map = new HashMap<>(iso2Countries.length);
        for (String iso2 : iso2Countries) {
            Locale locale = new Locale("", iso2);
            iso3ToIso2Map.put(locale.getISO3Country().toUpperCase(), iso2);
        }

        countries = new HashSet<>();

        if (!this.enabled)
            return;

        if (autoFetchCountries)
            fetchSupportedCountries();
    }

    @Autowired
    public JohnsHopkinsApi(
            @Value("${rapid-api.key}") final String apiKey,
            @Value("${api.johns-hopkins.enabled}") final String enabled,
            @Value("${api.auto-fetch-countries}") final String autoFetchCountries) {
        this(BASE_URL, apiKey, Boolean.parseBoolean(enabled), Boolean.parseBoolean(autoFetchCountries));
    }



    @Override
    public Stats getStats(ApiQuery query) {
        if (!enabled)
            return new NullStats();

        if (!authorized ||
                query.getAfter() != null || query.getBefore() != null)
            throw new UnavailableExternalApiException("The API access is either unauthorized or the request has unsupported parameters");

        MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());

        if (query.getAtDate() != null)
            queryParams.add("date", query.getAtDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

        boolean hasCountry = query.getAtCountry() != null;
        val location = hasCountry ? "/reports" : "/reports/total";
        if (hasCountry) {
            Locale locale = new Locale("", query.getAtCountry());
            queryParams.add("iso", locale.getISO3Country());
        }

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
        if (jsonData.isJsonArray()) {
            JsonArray jsonDataArray = jsonData.getAsJsonArray();
            if (jsonDataArray.isEmpty())
                throw new UnavailableExternalApiException("The response doesn't have any data");
            jsonData = jsonDataArray.get(0);
        }

        JohnsHopkinsStats apiStats = gson.fromJson(jsonData, JohnsHopkinsStats.class);

        if (apiStats == null)
            throw new UnavailableExternalApiException("The obtained stats from the JSON conversion are null");

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
        return countries.stream()
                .map(JohnsHopkinsCountry::getIso)
                .filter(iso3ToIso2Map::containsKey)
                .map(iso3ToIso2Map::get)
                .collect(Collectors.toSet());
    }

    @Override
    public void fetchSupportedCountries() {
        String jsonResponse = null;
        authorized = true;
        try {
            jsonResponse = webClient.get()
                    .uri("/regions")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException.Unauthorized | WebClientResponseException.Forbidden ex) {
            authorized = false;
            log.error("WebClientResponseException.Unauthorized when obtaining country list: {}", ex.getMessage());
        }

        if (jsonResponse != null) {
            JsonObject jsonRoot = gson.fromJson(jsonResponse, JsonObject.class);
            jsonRoot.getAsJsonArray("data").forEach(
                    jElem -> countries.add(gson.fromJson(jElem, JohnsHopkinsCountry.class))
            );
        }
    }



    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Sent request's headers: {}", clientRequest.headers());
            return Mono.just(clientRequest);
        });
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
