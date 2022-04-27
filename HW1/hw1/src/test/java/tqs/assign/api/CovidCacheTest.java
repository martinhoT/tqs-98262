package tqs.assign.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tqs.assign.TestUtils;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.exceptions.NoCachedElementException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CovidCacheTest {

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



    @BeforeEach
    void setUp() {
        covidCache = new CovidCache(60L, 6000L);
    }



    @Test
    @DisplayName("Correct save functionality")
    void whenResponseStored_thenResponseSaved() {
        queryResponses.forEach((query, response) -> {
            covidCache.store(query, response);

            assertEquals(response, covidCache.get(query));
            assertFalse(covidCache.stale(query));
        });
    }

    @Test
    @DisplayName("Response is stale when TTL is exceeded")
    void whenResponseStoredLongerThanTTL_thenResponseIsStale() {
        Duration ttl = Duration.ofSeconds(5L);

        covidCache.setTtl(ttl.getSeconds());
        covidCache.store(testQuery, testResponse);
        assertFalse(covidCache.stale(testQuery));

        await().atLeast(ttl).and().atMost(ttl.plusSeconds(1L)).untilAsserted(
                () -> assertTrue(covidCache.stale(testQuery)));
    }

    @Test
    @DisplayName("NoCachedElementException when non existent response")
    void whenGetNonExistentResponse_thenThrowNoCachedElementException() {
        assertThrows(NoCachedElementException.class, () -> covidCache.get(testQuery));
    }

    @Test
    @DisplayName("Response is stale if non existent")
    void whenResponseIsNonExistent_thenItIsStale() {
        assertTrue(covidCache.stale(testQuery));
    }

    @Test
    @DisplayName("Correct cache stats")
    void whenGetCacheStats_thenCacheStatsObtained() {
        queryResponses.forEach((query, response) -> {
            assertEquals(response, covidCache.getOrStore(query, (q) -> response));
        });

        long ttl = covidCache.getTtl();

        covidCache.getOrStore(testQuery, (q) -> testResponse);

        assertEquals(new CacheStats(1, queryResponses.size(), queryResponses.size(), ttl), covidCache.statsSnapshot());

        Duration noTtl = Duration.ZERO;
        covidCache.setTtl(noTtl.getSeconds());
        await().atMost(noTtl.plusSeconds(2L)).untilAsserted(() -> assertTrue(covidCache.stale(testQuery)));
        covidCache.getOrStore(testQuery, (q) -> testResponse);
        assertEquals(new CacheStats(
                1,
                queryResponses.size()+1,
                queryResponses.size(),
                noTtl.getSeconds()
            ), covidCache.statsSnapshot());
    }

    @Test
    @DisplayName("Cache maximum size")
    void whenGetResponseBeyondMaxSize_thenRemoveOlderResponses() {
        covidCache = new CovidCache(Long.MAX_VALUE, 3L);
        covidCache.store(testQuery, testResponse);
        queryResponses.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals(testQuery))
                .forEach(entry -> covidCache.store(entry.getKey(), entry.getValue()));

        assertEquals(3, covidCache.statsSnapshot().getStored());
        assertTrue(covidCache.stale(testQuery));
    }

}
