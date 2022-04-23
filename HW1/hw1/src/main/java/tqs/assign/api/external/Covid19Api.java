package tqs.assign.api.external;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
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
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Covid19Api implements Api {

    public static final String BASE_URL = "https://api.covid19api.com";

    private Set<Covid19Country> countries;
    private Map<String, Covid19Country> countriesIsoMap;

    private WebClient webClient;



    public Covid19Api(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(5))
                ))
                .build();

        Optional<Set<Covid19Country>> countriesOptional = webClient.get()
                .uri("/countries")
                .retrieve()
                .bodyToFlux(Covid19Country.class)
                .collect(Collectors.toSet()).blockOptional();

        if (countriesOptional.isPresent()) {
            countries = countriesOptional.get();
            countriesIsoMap = countries.stream().collect(Collectors.toMap(Covid19Country::getIso2, c -> c));
        }
        else {
            countries = new HashSet<>();
            countriesIsoMap = new HashMap<>();
        }
    }

    public Covid19Api() {
        this(BASE_URL);
    }



    @Override
    public Stats getStats(ApiQuery query) {
        String location = query.getAtCountry() != null
                ? "/country/" + countriesIsoMap.get(query.getAtCountry()).getSlug()
                : "/world";

        MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());
        if (query.getAtDate() != null) {
            LocalDateTime atDate = convertLocalDate(query.getAtDate());
            queryParams.add("from", formatLocalDateTime( atDate.minusDays(1) ));
            queryParams.add("to", formatLocalDateTime( atDate ));
        }
        else {
            if (query.getAfter() != null)
                queryParams.add("from", formatLocalDateTime( convertLocalDate(query.getAfter()) ));
            if (query.getBefore() != null)
                queryParams.add("to", formatLocalDateTime( convertLocalDate(query.getBefore()) ));
        }

        Optional<List<Covid19Stats>> covid19StatsListOptional = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(location)
                        .queryParams(queryParams)
                        .build())
                .exchangeToFlux(response -> response.bodyToFlux(Covid19Stats.class))
                .collectList().blockOptional();

        List<Covid19Stats> covid19StatsList = covid19StatsListOptional
                .orElseThrow(UnavailableExternalApiException::new);

        if (covid19StatsList.isEmpty())
            throw new UnavailableExternalApiException();

        Covid19Stats last = covid19StatsList.get(covid19StatsList.size() - 1);
        Covid19Stats first = covid19StatsList.get(0);
        return new Stats(
                last.getConfirmed(),
                last.getConfirmed() - first.getConfirmed(),
                last.getDeaths(),
                last.getDeaths() - first.getDeaths(),
                last.getRecovered(),
                last.getRecovered() - first.getRecovered(),
                last.getActive(),
                ((double) last.getDeaths() / last.getConfirmed()) * 100);
    }

    @Override
    public Set<String> getSupportedCountries() {
        return countriesIsoMap.keySet();
    }

    private LocalDateTime convertLocalDate(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    private String formatLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }



    @Getter
    public static class Covid19Country {
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
    public static class Covid19Stats {
        private final String country;
        private final String countryCode;
        private final int confirmed;
        private final int deaths;
        private final int recovered;
        private final int active;
        private final LocalDateTime date;

        @JsonCreator
        public Covid19Stats(
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

}
