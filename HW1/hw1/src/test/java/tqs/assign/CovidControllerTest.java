package tqs.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.controller.CovidController;
import tqs.assign.data.Stats;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        Map<String, Stats> countryStats = Map.of(
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

        Stats globalStats = new Stats(
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

        Stats resultStats = gson.fromJson( result.getResponse().getContentAsString(), Stats.class );

        assertEquals(globalStats, resultStats);
    }

    @Test
    void whenGetCountryStats_thenReturnCountryStats() throws Exception {
        Set<String> countryISOs = countryStats.keySet();

        for (String countryISO : countryISOs)
            when(covidApi.getCountryStats(countryISO)).thenReturn(countryStats.get(countryISO));

        for (String countryISO : countryISOs) {
            MvcResult result = mvc.perform(get("{/api/covid/stats/{countryISO}")
                            .param("countryISO", countryISO))
                    .andExpect(status().isOk())
                    .andReturn();

            Stats resultStats = gson.fromJson(result.getResponse().getContentAsString(), Stats.class);

            assertEquals(countryStats.get(countryISO), resultStats);
        }
    }

    @Test
    void whenGetStatsAfterDate_thenReturnAfterDateStats() throws Exception {
        LocalDateTime date = LocalDateTime.of(2022, 1, 1, 12, 30);

        when(covidApiQuery.after(date).fetch()).thenReturn(globalStats);

        assertEquals(globalStats, covidApi.getStats().after(date).fetch());
    }

    // TODO: test for bad input

}
