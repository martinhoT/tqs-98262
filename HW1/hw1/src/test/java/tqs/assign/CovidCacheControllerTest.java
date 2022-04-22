package tqs.assign;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tqs.assign.api.CovidCache;
import tqs.assign.controller.CovidCacheController;
import tqs.assign.data.CacheStats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tqs.assign.TestUtils.gson;

@WebMvcTest(CovidCacheController.class)
class CovidCacheControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CovidCache covidCache;

    private final CacheStats cacheStats = new CacheStats(1, 4, 5, 4,60L);



    @BeforeEach
    void setUp() {
        when(covidCache.statsSnapshot()).thenReturn(cacheStats);
    }



    @Test
    void whenGetStats_thenReturnStats() throws Exception {
        MvcResult result = mvc.perform(get("/api/cache/stats"))
                .andExpect(status().isOk())
                .andReturn();

        CacheStats resultCacheStats = gson.fromJson(result.getResponse().getContentAsString(), CacheStats.class);

        assertEquals(cacheStats, resultCacheStats);
    }

}
