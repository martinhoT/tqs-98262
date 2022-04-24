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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Covid19FastestUpdateApi implements Api {

    public static final String BASE_URL = "https://api.covid19api.com";

    private final WebClient webClient;

    private Set<Covid19Country> countries;
    private Map<String, Covid19Country> countriesIsoMap;



    public Covid19FastestUpdateApi(String baseUrl) {
        webClient = WebClient.builder()
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

    public Covid19FastestUpdateApi() {
        this(BASE_URL);
    }



    @Override
    public Stats getStats(ApiQuery query) {
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
        if (atCountry == null)
            throw new UnavailableExternalApiException();
        return queryAtCountry(countriesIsoMap.get(atCountry).getSlug(), queryParams);
    }

    @Override
    public Set<String> getSupportedCountries() {
        return countriesIsoMap.keySet();
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
                .orElseThrow(UnavailableExternalApiException::new);

        if (covid19WorldStatsList.isEmpty())
            throw new UnavailableExternalApiException();

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
                .orElseThrow(UnavailableExternalApiException::new);

        if (covid19CountryStatsList.isEmpty())
            throw new UnavailableExternalApiException();

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
