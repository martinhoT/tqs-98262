package tqs.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.controller.CovidController;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.IncorrectlyFormattedCountryException;
import tqs.assign.exceptions.IncorrectlyFormattedDateException;
import tqs.assign.exceptions.UnavailableApiException;
import tqs.assign.exceptions.UnsupportedCountryISOException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tqs.assign.TestUtils.gson;

@WebMvcTest(CovidController.class)
class CovidControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CovidApi covidApi;



    private Map<String, Stats> countryStats;
    private Stats globalStats;

    @BeforeEach
    void setUp() {
        countryStats = Map.of(
                "PT", new Stats(
                        3719485,
                        0,
                        21993,
                        0,
                        12345,
                        0,
                        5839,
                        0.001),
                "GB", new Stats(
                        68325673,
                        12,
                        5215632,
                        326932,
                        153264,
                        68943,
                        2515,
                        0.002
                )
        );

        globalStats = new Stats(
                547398674,
                2356,
                473953,
                233,
                86759757,
                7483,
                4839636,
                0.05
        );
    }



    @Test
    @DisplayName("Obtain global stats")
    void whenGetGlobalStats_thenReturnGlobalStats() throws Exception {
        when(covidApi.getStats(ApiQuery.builder().build())).thenReturn(globalStats);

        MvcResult result = mvc.perform(get("/api/covid/stats"))
                .andExpect(status().isOk())
                .andReturn();

        Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);

        assertEquals(globalStats, resultStats);
    }

    @Test
    @DisplayName("Country filtering")
    void whenGetCountryStats_thenReturnCountryStats() throws Exception {
        Set<String> countryISOs = countryStats.keySet();

        for (String countryISO : countryISOs)
            when(covidApi.getStats(ApiQuery.builder().atCountry(countryISO).build()))
                    .thenReturn(countryStats.get(countryISO));

        for (String countryISO : countryISOs) {
            MvcResult result = mvc.perform(get("/api/covid/stats/{country}", countryISO))
                    .andExpect(status().isOk())
                    .andReturn();

            Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);

            assertEquals(countryStats.get(countryISO), resultStats);
        }
    }

    @Test
    @DisplayName("After date query parameter")
    void whenGetStatsAfterDate_thenReturnStatsAfterDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApi.getStats(ApiQuery.builder().after(date).build())).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApi.getStats(ApiQuery.builder().atCountry(countryISO).after(date).build()))
                    .thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "after", List.of(date.toString())
        )));
    }

    @Test
    @DisplayName("Before date query parameter")
    void whenGetStatsBeforeDate_thenReturnBeforeDateStats() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApi.getStats(ApiQuery.builder().before(date).build())).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApi.getStats(ApiQuery.builder().atCountry(countryISO).before(date).build()))
                    .thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "before", List.of(date.toString())
        )));
    }

    @Test
    @DisplayName("Date query parameter")
    void whenGetStatsAtDate_thenReturnStatsAtDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApi.getStats(ApiQuery.builder().atDate(date).build())).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApi.getStats(ApiQuery.builder().atCountry(countryISO).atDate(date).build())).thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "date", List.of(date.toString())
        )));
    }

    @Test
    @DisplayName("Priority to specific date argument in detriment to range date arguments")
    void whenGetStatsAtDateAndRange_thenPrioritizeAtDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate dateAt = LocalDate.of(2022, 1, 1);
        LocalDate dateBefore = LocalDate.of(2022, 2, 2);
        LocalDate dateAfter = LocalDate.of(2021, 1, 1);

        when(covidApi.getStats(ApiQuery.builder()
                .atDate(dateAt)
                .before(dateBefore)
                .after(dateAfter)
                .build()))
            .thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApi.getStats(ApiQuery.builder()
                    .atCountry(countryISO)
                    .atDate(dateAt)
                    .before(dateBefore)
                    .after(dateAfter)
                    .build()))
                .thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "date", List.of(dateAt.toString()),
                "after", List.of(dateAfter.toString()),
                "before", List.of(dateBefore.toString())
        )));
    }

    @Test
    @DisplayName("Bad Request when date arguments are not properly formatted")
    void whenDateArgumentsAreNotFormatted_thenExpectBadRequest() throws Exception {
        String baseMsg = "Date argument '%s' is not properly formatted (ISO Local Date format: yyyy-MM-dd)";
        Map<String, String> parameters = Map.of(
                "date", "2022-13-01",
                "before", "01-12-2022",
                "after", "11-22-2022"
        );

        for (Map.Entry<String, String> parameter : parameters.entrySet())
            mvc.perform(get("/api/covid/stats")
                    .queryParam(parameter.getKey(), parameter.getValue()))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(baseMsg.formatted(parameter.getKey())))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IncorrectlyFormattedDateException));
    }

    @Test
    @DisplayName("Bad Request when country ISO is not properly formatted")
    void whenInvalidCountryISO_thenExpectBadRequest() throws Exception {
        String baseMsg = "Country argument '%s' is not properly formatted (ISO 3166-1 alpha code)";
        List<String> countryISOCodes = List.of("Portugal", "876", "United Kingdom", "O_O");

        for (String countryISOCode : countryISOCodes) {
            mvc.perform(get("/api/covid/stats/{country}", countryISOCode))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(baseMsg.formatted(countryISOCode)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IncorrectlyFormattedCountryException));
        }
    }

    @Test
    @DisplayName("Not Found when country ISO doesn't exist (not supported)")
    void whenNonExistentCountryISO_thenExpectNotFound() throws Exception {
        String baseMsg = "The specified country ISO code '%s' does not exist in this platform";
        List<String> countryISOCodes = List.of("ABC", "ZZZ", "WOAH");

        for (String countryISOCode : countryISOCodes)
            mvc.perform(get("/api/covid/stats/{country}", countryISOCode))
                .andExpect(status().isNotFound())
                .andExpect(status().reason(baseMsg.formatted(countryISOCode)))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnsupportedCountryISOException));
    }

    @Test
    @DisplayName("UnavailableApiException when all external APIs are unreachable")
    void whenExternalAPIsUnreachable_thenThrowUnavailableApiException() throws Exception {
        when(covidApi.getStats(any())).thenThrow(new UnavailableApiException());

        String msg = "The specified resource is unavailable, since no external data providers can fulfill the request at the moment";

        mvc.perform(get("/api/covid/stats"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(status().reason(msg))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UnavailableApiException));
    }



    private void assertCorrectCountryAndGlobalStatsForRequest(MultiValueMap<String, String> queryParams) throws Exception {
        MvcResult result = mvc.perform(get("/api/covid/stats")
                        .queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();
        Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);
        assertEquals(globalStats, resultStats);

        for (String countryISO : countryStats.keySet()) {
            result = mvc.perform(get("/api/covid/stats/{country}", countryISO)
                            .queryParams(queryParams))
                    .andExpect(status().isOk())
                    .andReturn();
            resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);
            assertEquals(countryStats.get(countryISO), resultStats);
        }
    }

}
