package tqs.assign.api.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.NullStats;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Covid19FastestUpdateApi implements Api {

    public static final String BASE_URL = "https://api.covid19api.com";

    @Getter
    private final boolean enabled;

    private final boolean allowIncompleteResponses;

    private final WebClient webClient;

    private Set<Covid19Country> countries;
    private Map<String, Covid19Country> countriesIsoMap;



    public Covid19FastestUpdateApi(final String baseUrl,
                                   final boolean enabled,
                                   final boolean allowIncompleteResponses,
                                   final boolean autoFetchCountries) {
        this.enabled = enabled;
        this.allowIncompleteResponses = allowIncompleteResponses;

        countries = new HashSet<>();
        countriesIsoMap = new HashMap<>();

        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                ))
                .build();

        if (!this.enabled)
            return;

        if (autoFetchCountries)
            fetchSupportedCountries();
    }

    @Autowired
    public Covid19FastestUpdateApi(
            @Value("${api.covid-fu.enabled}") final String enabled,
            @Value("${api.covid-fu.incomplete-responses}") final String allowIncompleteResponses,
            @Value("${api.auto-fetch-countries}") final String autoFetchCountries) {
        this(BASE_URL, Boolean.parseBoolean(enabled), Boolean.parseBoolean(allowIncompleteResponses), Boolean.parseBoolean(autoFetchCountries));
    }



    @Override
    public Stats getStats(ApiQuery query) {
        if (!enabled)
            return new NullStats();

        MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());
        if (query.getAtDate() != null) {
            LocalDate atDate = query.getAtDate();
            queryParams.add("from", formatLocalDate( atDate.minusDays(1L) ));
            queryParams.add("to", formatLocalDate( atDate ));
        }
        else {
            if (query.getAfter() != null)
                queryParams.add("from", formatLocalDate( query.getAfter() ));
            if (query.getBefore() != null)
                queryParams.add("to", formatLocalDate( query.getBefore() ));
        }

        String atCountry = query.getAtCountry();
        if (atCountry == null) {
            if (allowIncompleteResponses)
                return queryAtWorld(queryParams);
            else
                throw new UnavailableExternalApiException("Responses to 'world' requests are incomplete and the 'allowIncompleteResponses' flag is set to 'false'");
        }
        return queryAtCountry(countriesIsoMap.get(atCountry).getSlug(), queryParams);
    }

    @Override
    public Set<String> getSupportedCountries() {
        return countriesIsoMap.keySet();
    }

    @Override
    public void fetchSupportedCountries() {
        Optional<Set<Covid19Country>> countriesOptional = webClient.get()
                .uri("/countries")
                .retrieve()
                .bodyToFlux(Covid19Country.class)
                .collect(Collectors.toSet()).blockOptional();

        if (countriesOptional.isPresent()) {
            countries = countriesOptional.get();
            countriesIsoMap = countries.stream().collect(Collectors.toMap(Covid19Country::getIso2, c -> c));
        }
    }



    private String formatLocalDate(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // The endpoint is a WIP, and is missing some data
    private Stats queryAtWorld(MultiValueMap<String, String> queryParams) {
        Optional<List<Covid19WorldStats>> covid19StatsListOptional = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/world")
                        .queryParams(queryParams)
                        .build())
                .exchangeToFlux(response -> response.bodyToFlux(Covid19WorldStats.class))
                .collectList().blockOptional();

        List<Covid19WorldStats> covid19WorldStatsList = covid19StatsListOptional
                .orElseThrow(() -> new UnavailableExternalApiException("Optional response is empty"));

        if (covid19WorldStatsList.isEmpty())
            throw new UnavailableExternalApiException("Response's list of results is empty");

        Covid19WorldStats last = covid19WorldStatsList.get(covid19WorldStatsList.size() - 1);
        Covid19WorldStats first = covid19WorldStatsList.get(0);
        return new Stats(
                last.getConfirmed(),
                last.getConfirmed() - first.getConfirmed(),
                last.getDeaths(),
                last.getDeaths() - first.getDeaths(),
                last.getRecovered(),
                last.getRecovered() - first.getRecovered(),
                Stats.UNSUPPORTED_FIELD,
                Stats.UNSUPPORTED_FIELD,
                ((double) last.getDeaths() / last.getConfirmed()) * 100);
    }

    private Stats queryAtCountry(String country, MultiValueMap<String, String> queryParams) {
        Optional<List<Covid19CountryStats>> covid19StatsListOptional = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/country/" + country)
                        .queryParams(queryParams)
                        .build())
                .exchangeToFlux(response -> response.bodyToFlux(Covid19CountryStats.class))
                .collectList().blockOptional();

        List<Covid19CountryStats> covid19CountryStatsList = covid19StatsListOptional
                .orElseThrow(() -> new UnavailableExternalApiException("Optional response is empty"));

        if (covid19CountryStatsList.isEmpty())
            throw new UnavailableExternalApiException("Response's list of results is empty");

        Covid19CountryStats last = covid19CountryStatsList.get(covid19CountryStatsList.size() - 1);
        Covid19CountryStats first = covid19CountryStatsList.get(0);
        return new Stats(
                last.getConfirmed(),
                last.getConfirmed() - first.getConfirmed(),
                last.getDeaths(),
                last.getDeaths() - first.getDeaths(),
                last.getRecovered(),
                last.getRecovered() - first.getRecovered(),
                last.getActive(),
                last.getActive() - first.getActive(),
                ((double) last.getDeaths() / last.getConfirmed()) * 100);
    }



    @Getter
    static class Covid19Country {
        private final String country;
        private final String slug;
        private final String iso2;

        @JsonCreator
        public Covid19Country(
                @JsonProperty("Country") String country,
                @JsonProperty("Slug") String slug,
                @JsonProperty("ISO2") String iso2) {
            this.country = country;
            this.slug = slug;
            this.iso2 = iso2;
        }
    }

    @Getter
    static class Covid19CountryStats {
        private final String country;
        private final String countryCode;
        private final int confirmed;
        private final int deaths;
        private final int recovered;
        private final int active;
        private final LocalDateTime date;

        @JsonCreator
        public Covid19CountryStats(
                @JsonProperty("Country") String country,
                @JsonProperty("CountryCode") String countryCode,
                @JsonProperty("Confirmed") int confirmed,
                @JsonProperty("Deaths") int deaths,
                @JsonProperty("Recovered") int recovered,
                @JsonProperty("Active") int active,
                @JsonProperty("Date") LocalDateTime date) {
            this.country = country;
            this.countryCode = countryCode;
            this.confirmed = confirmed;
            this.deaths = deaths;
            this.recovered = recovered;
            this.active = active;
            this.date = date;
        }
    }

    @Getter
    static class Covid19WorldStats {
        private final int confirmed;
        private final int deaths;
        private final int recovered;
        private final LocalDateTime date;

        @JsonCreator
        public Covid19WorldStats(
                @JsonProperty("TotalConfirmed") int confirmed,
                @JsonProperty("TotalDeaths") int deaths,
                @JsonProperty("TotalRecovered") int recovered,
                @JsonProperty("Date") LocalDateTime date) {
            this.confirmed = confirmed;
            this.deaths = deaths;
            this.recovered = recovered;
            this.date = date;
        }
    }

}
