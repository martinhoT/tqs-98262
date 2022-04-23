package tqs.assign.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMapAdapter;
import tqs.assign.TestUtils;
import tqs.assign.api.external.Covid19Api;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;
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

    private final List<Api> supportedApis = List.of(
            vaccovidApi,
            covid19Api
    );



    @BeforeEach
    void setUpEverything() {
        ReflectionTestUtils.setField(covidApi, "supportedApis", supportedApis);
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

        disableApisBut(covid19Api, null);

        covidApi.getStats(globalQuery);
        verify(vaccovidApi, times(2)).getStats(globalQuery);
        verify(covid19Api, times(1)).getStats(globalQuery);

        disableApisBut(vaccovidApi, null);

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



    private void disableApisBut(Api onlyEnabledOne, Stats returnValue) {
        for (Api api : supportedApis)
            if (!api.equals(onlyEnabledOne))
                when(api.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(onlyEnabledOne.getStats(any())).thenReturn(returnValue);
    }

    private void disableCovidCache() {
        when(covidCache.stale(any())).thenReturn(true);
    }

}
