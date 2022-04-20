package tqs.assign;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidCache;
import tqs.assign.api.external.OpenCovidApi;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CovidIntegrationTest {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CovidCache covidCache;

    @MockBean
    private VaccovidApi vaccovidApi;
    @MockBean
    private OpenCovidApi openCovidApi;

    private final Map<ApiQuery, ResponseData> queryResponses = Map.of(
            ApiQuery.builder().build(), TestUtils.randomStats(),
            ApiQuery.builder()
                    .atCountry("PT")
                    .build(), TestUtils.randomStats(),
            ApiQuery.builder()
                    .after(LocalDate.now().minusDays(6))
                    .build(), TestUtils.randomStats(),
            ApiQuery.builder()
                    .before(LocalDate.of(2022, 1, 1).plusDays(3))
                    .atCountry("PT")
                    .build(), TestUtils.randomStats(),
            ApiQuery.builder()
                    .atCountry("GB")
                    .atDate(LocalDate.of(2022, 4, 1))
                    .build(), TestUtils.randomStats()
    );



    @Test
    void whenGetCovidStatsRepeatedly_thenReturnCachedCovidStats() {
        ApiQuery apiQuery = ApiQuery.builder().build();

        ResponseEntity<Stats> statsResponse = restTemplate.getForEntity("/api/covid/stats", Stats.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        ResponseData actualResponse = queryResponses.get(apiQuery);
        assertEquals(actualResponse, statsResponse.getBody());

        statsResponse = restTemplate.getForEntity("/api/covid/stats", Stats.class);
        assertEquals(HttpStatus.OK, statsResponse.getStatusCode());
        actualResponse = queryResponses.get(apiQuery);
        assertEquals(actualResponse, statsResponse.getBody());

        ResponseEntity<CacheStats> cacheStatsResponse = restTemplate.getForEntity("/api/cache/stats", CacheStats.class);
        assertEquals(HttpStatus.OK, cacheStatsResponse.getStatusCode());
        actualResponse = new CacheStats(1, 1, 2, covidCache.getTtl());
        assertEquals(actualResponse, cacheStatsResponse.getBody());
    }



    private void registerQueryResponse(ApiQuery query, Stats response) {
        when(vaccovidApi.getStats(query)).thenReturn(response);
        when(openCovidApi.getStats(query)).thenReturn(response);
    }

}
