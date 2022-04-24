package tqs.assign.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.TestUtils;
import tqs.assign.api.external.Covid19Api;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableApiException;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class CovidApiIntegrationTest {

    @SpyBean
    private CovidCache covidCache;
    @MockBean
    private VaccovidApi vaccovidApi;
    @MockBean
    private Covid19Api covid19Api;

    @Autowired
    private CovidApi covidApi;

    private final Map<ApiQuery, Stats> queryResponses = Map.of(
            ApiQuery.builder().build(), TestUtils.randomStats(),
            ApiQuery.builder().atCountry("PT").build(), TestUtils.randomStats()
    );

    private List<Api> supportedApis;



    @BeforeEach
    void setUp() {
        supportedApis = List.of(
                vaccovidApi,
                covid19Api
        );

        ReflectionTestUtils.setField(covidApi, "supportedApis", supportedApis);
        ReflectionTestUtils.setField(covidApi, "supportedCountries", Set.of("PT"));

        queryResponses.forEach(this::registerQueryResponse);
    }

    @AfterEach
    void tearDown() {
        covidCache.clear();
        covidCache.resetStats();
    }



    @Test
    @DisplayName("Return from cache")
    void whenRequestCached_thenReturnCachedResponse() {
        disableApis();

        queryResponses.forEach((query, response) -> {
            covidCache.store(query, response);

            assertEquals(response, covidApi.getStats(query));
        });

        verify(covidCache, times(queryResponses.size())).get(any());
    }

    @Test
    @DisplayName("Call external API when cache entry is stale")
    void whenStaleCacheEntry_thenCallExternalAPI() {
        ApiQuery globalQuery = ApiQuery.builder().build();

        disableApisBut(vaccovidApi);

        covidApi.getStats(globalQuery);

        verify(covidCache, times(0)).get(globalQuery);
        verify(vaccovidApi, times(1)).getStats(globalQuery);
    }

    @Test
    @DisplayName("Store on cache")
    void whenRequestMade_thenResponseCached() {
        disableApisBut(vaccovidApi);

        ApiQuery apiQuery = ApiQuery.builder()
                .atCountry("PT")
                .atDate(LocalDate.now())
                .build();
        Stats apiResponse = TestUtils.randomStats();

        when(vaccovidApi.getStats(apiQuery)).thenReturn(apiResponse);

        covidApi.getStats(apiQuery);
        verify(vaccovidApi, times(1)).getStats(apiQuery);
        verify(covidCache, times(1)).store(apiQuery, apiResponse);

        int numberOfExtraRequests = 5;
        IntStream.range(0, numberOfExtraRequests).forEach((i) -> covidApi.getStats(apiQuery));

        verify(vaccovidApi, times(1)).getStats(apiQuery);
        verify(covidCache, times(1)).store(apiQuery, apiResponse);
        verify(covidCache, times(numberOfExtraRequests)).get(apiQuery);
    }

    @Test
    @DisplayName("Switch between APIs depending on availability")
    void whenUnavailableAPI_thenCallOtherAPI() {
        disableCovidCache();
        ApiQuery globalQuery = ApiQuery.builder().build();

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(1)).getStats(globalQuery);
        verify(covid19Api, times(0)).getStats(globalQuery);

        disableApisBut(covid19Api);

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(2)).getStats(globalQuery);
        verify(covid19Api, times(1)).getStats(globalQuery);

        disableApisBut(vaccovidApi);

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(3)).getStats(globalQuery);
        verify(covid19Api, times(2)).getStats(globalQuery);
    }

    @Test
    @DisplayName("UnavailableApiException when no external APIs can fulfill the request")
    void whenUnavailableAPIs_thenThrowUnavailableApiException() {
        when(vaccovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(covid19Api.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        assertThrows(UnavailableApiException.class, () -> covidApi.getStats(ApiQuery.builder().build()));
    }



    private void disableApisBut(Api onlyEnabledOne) {
        queryResponses.forEach(this::registerQueryResponse);

        for (Api api : supportedApis)
            if (!api.equals(onlyEnabledOne))
                doThrow(UnavailableExternalApiException.class).when(api).getStats(any());
    }

    private void disableApis() {
        for (Api api : supportedApis)
            doThrow(UnavailableExternalApiException.class).when(api).getStats(any());
    }

    private void disableCovidCache() {
        when(covidCache.stale(any())).thenReturn(true);
    }

    private void registerQueryResponse(ApiQuery query, Stats response) {
        doReturn(response).when(vaccovidApi).getStats(query);
        doReturn(response).when(covid19Api).getStats(query);
    }

}
