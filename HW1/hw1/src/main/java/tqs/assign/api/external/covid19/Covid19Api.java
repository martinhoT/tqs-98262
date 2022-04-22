package tqs.assign.api.external.covid19;

import lombok.Getter;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Covid19Api implements Api {

    private static final String BASE_URL = "https://api.covid19api.com";

    private Set<Covid19Country> countries;
    private Map<String, Covid19Country> countriesIsoMap;

    private WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(5))
            ))
            .build();

    public Covid19Api() {
        Optional<Set<Covid19Country>> countriesOptional = webClient.get()
                .uri("/countries")
                .exchangeToFlux(response -> response.bodyToFlux(Covid19Country.class))
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

    @Override
    public Stats getStats(ApiQuery query) {
        if (query.getAtDate() != null)
            throw new UnavailableExternalApiException();

        String location = query.getAtCountry() != null
                ? "/country/" + countriesIsoMap.get( query.getAtCountry() )
                : "/world";

        MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());
        if (query.getAfter() != null)
            queryParams.add("from", convertLocalDate(query.getAfter()));
        if (query.getBefore() != null)
            queryParams.add("to", convertLocalDate(query.getBefore()));

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

    private String convertLocalDate(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN).toString();
    }

}
