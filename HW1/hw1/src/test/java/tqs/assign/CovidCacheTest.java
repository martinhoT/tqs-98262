package tqs.assign;

import org.awaitility.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidCache;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.exceptions.NoCachedElementException;

import java.time.LocalDate;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CovidCacheTest {

    @Autowired
    private CovidCache covidCache;

    // Provided for convenience
    private final ApiQuery testQuery = ApiQuery.builder().build();
    private final ResponseData testResponse = TestUtils.randomStats();

    private final Map<ApiQuery, ResponseData> queryResponses = Map.of(
        testQuery, testResponse,
        ApiQuery.builder().atCountry("USA").build(), TestUtils.randomStats(),
        ApiQuery.builder().after(LocalDate.now()).build(), TestUtils.randomStats(),
        ApiQuery.builder().atCountry("SUS").after(LocalDate.of(2012, 7, 20)).build(), TestUtils.randomStats()
    );



    @Test
    void whenResponseStored_thenResponseSaved() {
        queryResponses.forEach((query, response) -> {
            covidCache.store(query, response);

            assertEquals(testResponse, covidCache.get(query));
            assertFalse(covidCache.stale(query));
        });
    }

    @Test
    void whenResponseStoredLongerThanTTL_thenResponseIsStale() {
        Duration ttl = Duration.FIVE_SECONDS;

        ReflectionTestUtils.setField(covidCache, "ttl", ttl.getValue());
        covidCache.store(testQuery, testResponse);
        assertFalse(covidCache.stale(testQuery));

        await().atLeast(ttl).and().atMost(ttl.plus(1L)).untilAsserted(
                () -> assertTrue(covidCache.stale(testQuery)));
    }

    @Test
    void whenGetNonExistentResponse_thenThrowNoCachedElementException() {
        assertThrows(NoCachedElementException.class, () -> covidCache.get(testQuery));
    }

    @Test
    void whenResponseIsNonExistent_thenItIsStale() {
        assertTrue(covidCache.stale(testQuery));
    }

    @Test
    @DisplayName("Check that the cache presents the correct stats. The count of hits and misses must be obtained from checking entry staleness")
    void whenGetCacheStats_thenCacheStatsObtained() {
        queryResponses.forEach((query, response) -> {
            covidCache.getOrStore(query, () -> response);
        });

        long ttl = covidCache.getTtl();

        covidCache.getOrStore(testQuery, () -> testResponse);

        assertEquals(new CacheStats(1, queryResponses.size(), queryResponses.size()+1, 4, ttl), covidCache.statsSnapshot());
    }

}
