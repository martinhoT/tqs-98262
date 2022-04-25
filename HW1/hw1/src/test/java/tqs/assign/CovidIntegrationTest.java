package tqs.assign;

import io.cucumber.spring.CucumberContextConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.api.CovidCache;
import tqs.assign.api.external.Covid19FastestUpdateApi;
import tqs.assign.api.external.JohnsHopkinsApi;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        locations = "/application-test.properties",
        properties = "api.covid-fu.incomplete-responses=true")
class CovidIntegrationTest {

    @LocalServerPort
    int randomServerPort;

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private CovidCache covidCache;
    @Autowired private CovidApi covidApi;

    @MockBean private JohnsHopkinsApi johnsHopkinsApi;
    @MockBean private Covid19FastestUpdateApi covid19FastestUpdateApi;

    private final Map<ApiQuery, ApiQueryInfo> queryResponses = Map.of(
            ApiQuery.builder().build(), new ApiQueryInfo("/api/covid/stats"),
            ApiQuery.builder()
                    .atCountry("PT")
                    .build(), new ApiQueryInfo("/api/covid/stats/PT"),
            ApiQuery.builder()
                    .after(LocalDate.now().minusDays(6))
                    .build(), new ApiQueryInfo("/api/covid/stats?after=%s".formatted(LocalDate.now().minusDays(6))),
            ApiQuery.builder()
                    .before(LocalDate.of(2022, 1, 1).plusDays(3))
                    .atCountry("PT")
                    .build(), new ApiQueryInfo("/api/covid/stats/PT?before=%s".formatted(LocalDate.of(2022, 1, 1).plusDays(3))),
            ApiQuery.builder()
                    .atCountry("GB")
                    .atDate(LocalDate.of(2022, 4, 1))
                    .build(), new ApiQueryInfo("/api/covid/stats/GB?date=%s".formatted(LocalDate.of(2022, 4, 1)))
    );

    @Getter
    private static class ApiQueryInfo {
        private final Stats response;
        private final String requestUrl;

        public ApiQueryInfo(String url) { response = TestUtils.randomStats(); this.requestUrl = url; }
    }



    @BeforeEach
    void setUp() {
        queryResponses.forEach((query, info) -> registerQueryResponse(query, info.getResponse()));

        Set<String> supportedCountries = Set.of("PT", "GB");

        ReflectionTestUtils.setField(covidApi, "supportedCountries", supportedCountries);
        ReflectionTestUtils.setField(covidApi, "supportedApis", List.of(johnsHopkinsApi, covid19FastestUpdateApi));
    }

    @AfterEach
    void tearDown() {
        covidCache.clear();
        covidCache.resetStats();
    }



    @Test
    @DisplayName("Responses are cached")
    void whenGetCovidStatsRepeatedly_thenReturnCachedCovidStats() {
        ApiQuery apiQuery = ApiQuery.builder().build();

        ResponseEntity<Stats> statsResponse = restTemplate.getForEntity("/api/covid/stats", Stats.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        ResponseData actualResponse = queryResponses.get(apiQuery).getResponse();
        assertEquals(actualResponse, statsResponse.getBody());

        statsResponse = restTemplate.getForEntity("/api/covid/stats", Stats.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        actualResponse = queryResponses.get(apiQuery).getResponse();
        assertEquals(actualResponse, statsResponse.getBody());

        ResponseEntity<CacheStats> cacheStatsResponse = restTemplate.getForEntity("/api/cache/stats", CacheStats.class);
        assertEquals(HttpStatus.OK, cacheStatsResponse.getStatusCode());
        actualResponse = new CacheStats(1, 1, 1, covidCache.getTtl());
        assertEquals(actualResponse, cacheStatsResponse.getBody());
    }

    @Test
    @DisplayName("Querying with parameters")
    void whenGetCovidStatsWithParams_thenReturnSpecificCovidStats() {
        queryResponses.forEach((query, info) -> {
            ResponseEntity<Stats> statsResponse = restTemplate.getForEntity(info.getRequestUrl(), Stats.class);
            assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
            assertEquals(info.getResponse(), statsResponse.getBody());
        });
    }



    private void registerQueryResponse(ApiQuery query, Stats response) {
        when(johnsHopkinsApi.getStats(query)).thenReturn(response);
        when(covid19FastestUpdateApi.getStats(query)).thenReturn(response);
    }

}
