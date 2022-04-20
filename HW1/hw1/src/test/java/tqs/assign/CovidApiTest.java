package tqs.assign;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.api.CovidCache;
import tqs.assign.api.external.OpenCovidApi;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CovidApiTest {

    @MockBean
    private CovidCache covidCache;
    @MockBean
    private VaccovidApi vaccovidApi;
    @MockBean
    private OpenCovidApi openCovidApi;

    @InjectMocks
    private CovidApi covidApi;

    private final String globalRequestMethod = "getStats";
    private final String countryRequestMethod = "getStatsAtCountry";
    private final List<String> globalRequestArgs = Collections.emptyList();
    private final List<String> countryRequestArgs = List.of("PT");
    private final ResponseData globalResponse = new Stats(1, 2, 3, 4, 5, 6, 7, 0.01);
    private final ResponseData countryResponse = new Stats(9, 8, 7, 6, 5, 4, 3, 0);



    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUpEverything() {
        // Check that both API lists have the same order
        List<Api> supportedApis = (List<Api>) ReflectionTestUtils.getField(covidApi, "supportedApis");
        assertEquals(List.of(
                vaccovidApi,
                openCovidApi
        ), supportedApis, "The list of supported APIs is out of order in either the tests or the CovidApi class");
    }

    @Test
    void whenRequestCached_thenReturnCachedResponse() {
        when(covidCache.stale(globalRequestMethod, globalRequestArgs)).thenReturn(false);
        when(covidCache.stale(countryRequestMethod, countryRequestArgs)).thenReturn(false);
        when(covidCache.get(globalRequestMethod, globalRequestArgs)).thenReturn(globalResponse);
        when(covidCache.get(countryRequestMethod, countryRequestArgs)).thenReturn(countryResponse);

        assertEquals(globalResponse, covidApi.getStats(ApiQuery.builder().build()));
        assertEquals(globalResponse, covidApi.getGlobalStats());
        assertEquals(countryResponse, covidApi.getStats(ApiQuery.builder().atCountry(countryRequestArgs.get(0)).build()));
        assertEquals(countryResponse, covidApi.getCountryStats(countryRequestArgs.get(0)));
    }

    @Test
    void whenStaleCacheEntry_thenCallExternalAPI() {
        disableCovidCache();

        // Invalidate other APIs
        when(openCovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        covidApi.getGlobalStats();

        verify(covidCache, times(0)).get(any(), any());
        verify(vaccovidApi, times(1)).getGlobalStats();
        verify(vaccovidApi, times(1)).getStats(any());
    }

    @Test
    void whenUnavailableAPI_thenCallOtherAPI() {
        disableCovidCache();

        covidApi.getGlobalStats();
        verify(vaccovidApi, times(1)).getStats(any());
        verify(openCovidApi, times(0)).getStats(any());

        when(vaccovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        covidApi.getGlobalStats();
        verify(vaccovidApi, times(2)).getStats(any());
        verify(openCovidApi, times(1)).getStats(any());

        when(openCovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(vaccovidApi.getStats(any())).thenReturn(null);

        covidApi.getGlobalStats();
        verify(vaccovidApi, times(3)).getStats(any());
        verify(openCovidApi, times(2)).getStats(any());
    }

    @Test
    void whenUnavailableAPIs_thenThrowUnavailableApiException() {
        when(vaccovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);
        when(openCovidApi.getStats(any())).thenThrow(UnavailableExternalApiException.class);

        assertThrows(UnavailableExternalApiException.class, () -> covidApi.getGlobalStats());
    }

    private void disableCovidCache() {
        when(covidCache.stale(any(), any())).thenReturn(true);
    }

}
