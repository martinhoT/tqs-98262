package tqs.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.api.CovidCache;
import tqs.assign.api.external.covid19.Covid19Api;
import tqs.assign.api.external.vaccovid.VaccovidApi;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CovidApiTest {

    @MockBean
    private CovidCache covidCache;
    @MockBean
    private VaccovidApi vaccovidApi;
    @MockBean
    private Covid19Api covid19Api;

    @Autowired
    private CovidApi covidApi;

    private final Map<ApiQuery, ResponseData> queryResponses = Map.of(
            ApiQuery.builder().build(), TestUtils.randomStats(),
            ApiQuery.builder().atCountry("PT").build(), TestUtils.randomStats()
    );



    @BeforeEach
    void setUpEverything() {
        ReflectionTestUtils.setField(covidApi, "supportedApis", List.of(
                vaccovidApi,
                covid19Api
        ));
    }



    @Test
    void whenRequestCached_thenReturnCachedResponse() {
        queryResponses.forEach((query, response) -> {
            when(covidCache.stale(query)).thenReturn(false);
            when(covidCache.get(query)).thenReturn(response);

            assertEquals(response, covidApi.getStats(query));
        });
    }

    @Test
    void whenStaleCacheEntry_thenCallExternalAPI() {
        disableCovidCache();
        ApiQuery globalQuery = ApiQuery.builder().build();

        // Invalidate other APIs
        when(covid19Api.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        covidApi.getStats(globalQuery);

        verify(covidCache, times(0)).get(any());
        verify(vaccovidApi, times(1)).getStats(globalQuery);
    }

    @Test
    void whenRequestMade_thenResponseCached() {
        ApiQuery apiQuery = ApiQuery.builder()
                .atCountry("PT")
                .atDate(LocalDate.now())
                .build();
        Stats apiResponse = TestUtils.randomStats();

        when(vaccovidApi.getStats(apiQuery)).thenReturn(apiResponse);
        when(covidCache.stale(apiQuery)).thenReturn(true);

        covidApi.getStats(apiQuery);
        verify(vaccovidApi, times(1)).getStats(apiQuery);
        verify(covidCache, times(1)).store(apiQuery, apiResponse);

        when(covidCache.stale(apiQuery)).thenReturn(false);
        when(covidCache.get(apiQuery)).thenReturn(apiResponse);

        int numberOfExtraRequests = 5;
        IntStream.range(0, numberOfExtraRequests).forEach((i) -> covidApi.getStats(apiQuery));

        covidApi.getStats(apiQuery);
        verify(vaccovidApi, times(1)).getStats(apiQuery);
        verify(covidCache, times(1)).store(apiQuery, apiResponse);
        verify(covidCache, times(numberOfExtraRequests)).get(apiQuery);
        verify(covidCache, times(numberOfExtraRequests + 1)).get(apiQuery);
    }

    @Test
    void whenUnavailableAPI_thenCallOtherAPI() {
        disableCovidCache();
        ApiQuery globalQuery = ApiQuery.builder().build();

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(1)).getStats(globalQuery);
        verify(covid19Api, times(0)).getStats(globalQuery);

        when(vaccovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(2)).getStats(globalQuery);
        verify(covid19Api, times(1)).getStats(globalQuery);

        when(covid19Api.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(vaccovidApi.getStats(any())).thenReturn(null);

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(3)).getStats(globalQuery);
        verify(covid19Api, times(2)).getStats(globalQuery);
    }

    @Test
    void whenUnavailableAPIs_thenThrowUnavailableApiException() {
        when(vaccovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(covid19Api.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        assertThrows(UnavailableExternalApiException.class, () -> covidApi.getStats(ApiQuery.builder().build()));
    }



    private void disableCovidCache() {
        when(covidCache.stale(any())).thenReturn(true);
    }

}
