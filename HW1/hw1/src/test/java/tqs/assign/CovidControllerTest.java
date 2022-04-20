package tqs.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import tqs.assign.api.CovidApi;
import tqs.assign.controller.CovidController;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tqs.assign.JsonUtils.gson;
import static tqs.assign.api.CovidApi.CovidApiQuery;

@WebMvcTest(CovidController.class)
class CovidControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CovidApi covidApi;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CovidApiQuery covidApiQuery;



    private Map<String, Stats> countryStats;
    private Stats globalStats;

    @BeforeEach
    void setUp() {
        when(covidApi.getStats()).thenReturn(covidApiQuery);

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
    void whenGetGlobalStats_thenReturnGlobalStats() throws Exception {
        when(covidApi.getGlobalStats()).thenReturn(globalStats);

        MvcResult result = mvc.perform(get("/api/covid/stats"))
                .andExpect(status().isOk())
                .andReturn();

        Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);

        assertEquals(globalStats, resultStats);
    }

    @Test
    void whenGetCountryStats_thenReturnCountryStats() throws Exception {
        Set<String> countryISOs = countryStats.keySet();

        for (String countryISO : countryISOs)
            when(covidApi.getCountryStats(countryISO)).thenReturn(countryStats.get(countryISO));

        for (String countryISO : countryISOs) {
            MvcResult result = mvc.perform(get("{/api/covid/stats/{country}")
                            .param("country", countryISO))
                    .andExpect(status().isOk())
                    .andReturn();

            Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);

            assertEquals(countryStats.get(countryISO), resultStats);
        }
    }

    @Test
    void whenGetStatsAfterDate_thenReturnStatsAfterDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApiQuery.after(date).fetch()).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApiQuery.atCountry(countryISO).after(date).fetch()).thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "after", List.of(date.toString())
        )));
    }

    @Test
    void whenGetStatsBeforeDate_thenReturnBeforeDateStats() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApiQuery.before(date).fetch()).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApiQuery.atCountry(countryISO).before(date).fetch()).thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "before", List.of(date.toString())
        )));
    }

    @Test
    void whenGetStatsAtDate_thenReturnStatsAtDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate date = LocalDate.of(2022, 1, 1);

        when(covidApiQuery.atDate(date).fetch()).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApiQuery.atCountry(countryISO).atDate(date).fetch()).thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "date", List.of(date.toString())
        )));
    }

    @Test
    @DisplayName("If both a particular date and a range of dates were specified as parameters in the call, then ignore the range parameter")
    void whenGetStatsAtDateAndRange_thenPrioritizeAtDate() throws Exception {
        Set<String> countryISOs = countryStats.keySet();
        LocalDate dateAt = LocalDate.of(2022, 1, 1);
        LocalDate dateBefore = LocalDate.of(2022, 2, 2);
        LocalDate dateAfter = LocalDate.of(2021, 1, 1);

        when(covidApiQuery.atDate(dateAt).before(dateBefore).after(dateAfter).fetch()).thenReturn(globalStats);
        for (String countryISO : countryISOs)
            when(covidApiQuery.atCountry(countryISO).atDate(dateAt).before(dateBefore).after(dateAfter).fetch())
                    .thenReturn(countryStats.get(countryISO));

        assertCorrectCountryAndGlobalStatsForRequest(new MultiValueMapAdapter<>(Map.of(
                "date", List.of(dateAt.toString()),
                "after", List.of(dateAfter.toString()),
                "before", List.of(dateBefore.toString())
        )));
    }

    @Test
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
                .andExpect(jsonPath("$.message").value(baseMsg.formatted(parameter.getKey())));
    }

    @Test
    void whenInvalidCountryISO_thenExpectBadRequest() throws Exception {
        String baseMsg = "Country argument '%s' is not properly formatted (ISO 3166-1 alpha code)";
        List<String> countryISOCodes = List.of("Portugal", "876", "United Kingdom", "O_O");

        for (String countryISOCode : countryISOCodes) {
            mvc.perform(get("/api/covid/stats/{country}")
                    .param("country", countryISOCode))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(baseMsg.formatted(countryISOCode)));
        }
    }

    @Test
    void whenNonExistentCountryISO_thenExpectNotFound() throws Exception {
        String baseMsg = "The specified country ISO code '%s' does not exist in this platform";
        List<String> countryISOCodes = List.of("ABC", "ZZZ", "WOAH");

        for (String countryISOCode : countryISOCodes)
            mvc.perform(get("/api/covid/stats/{country}")
                    .param("country", countryISOCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(baseMsg.formatted(countryISOCode)));
    }

    private void assertCorrectCountryAndGlobalStatsForRequest(MultiValueMap<String, String> queryParams) throws Exception {
        MvcResult result = mvc.perform(get("/api/covid/stats")
                        .queryParams(queryParams))
                .andExpect(status().isOk())
                .andReturn();
        Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);
        assertEquals(globalStats, resultStats);

        for (String countryISO : countryStats.keySet()) {
            result = mvc.perform(get("/api/covid/stats/{country}")
                            .param("country", countryISO)
                            .queryParams(queryParams))
                    .andExpect(status().isOk())
                    .andReturn();
            resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);
            assertEquals(countryStats.get(countryISO), resultStats);
        }
    }

}
