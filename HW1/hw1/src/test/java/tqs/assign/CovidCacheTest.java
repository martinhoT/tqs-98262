package tqs.assign;

import org.awaitility.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.api.CovidCache;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.NoCachedElementException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CovidCacheTest {

    @Autowired
    private CovidCache covidCache;

    // Provided for convenience
    private final ResponseData testResponse = new Stats(
            1, 2, 3, 4, 5, 6, 7, 0.1
    );
    private final String testRequestMethod = "getStats";
    private final List<String> testRequestArgs = Collections.emptyList();

    private final List<ResponseData> testResponses = List.of(
            testResponse,
            new Stats(2, 3, 4, 5, 6, 7, 8, 0.5),
            new Stats(10, 10, 10,0, 25, 5, 1, 0)
    );
    private final Map<String, List<String>> testRequests = Map.of(
            testRequestMethod, testRequestArgs,
            "getStatsAtCountry", List.of("arg"),
            "getStatsAfter", List.of("a", "b"),
            "getStatsAtCountryAfter", List.of("a")
    );



    @Test
    void whenResponseStored_thenResponseSaved() {
        covidCache.store(testResponse, testRequestMethod, testRequestArgs);

        assertEquals(testResponse, covidCache.get(testRequestMethod, testRequestArgs));
        assertFalse(covidCache.stale(testRequestMethod, testRequestArgs));
    }

    @Test
    void whenResponseStoredLongerThanTTL_thenResponseIsStale() {
        Duration ttl = Duration.FIVE_SECONDS;

        ReflectionTestUtils.setField(covidCache, "ttl", ttl.getValue());
        covidCache.store(testResponse, testRequestMethod, testRequestArgs);
        assertFalse(covidCache.stale(testRequestMethod, testRequestArgs));

        await().atLeast(ttl).and().atMost(ttl.plus(1L)).untilAsserted(
                () -> assertTrue(covidCache.stale(testRequestMethod, testRequestArgs)));
    }

    @Test
    void whenGetNonExistentResponse_thenThrowNoCachedElementException() {
        assertThrows(NoCachedElementException.class, () -> covidCache.get(testRequestMethod, testRequestArgs));
    }

    @Test
    void whenResponseIsNonExistent_thenItIsStale() {
        assertTrue(covidCache.stale(testRequestMethod, testRequestArgs));
    }

    @Test
    @DisplayName("Check that the cache presents the correct stats. The count of hits and misses must be obtained from checking entry staleness")
    void whenGetCacheStats_thenCacheStatsObtained() {
        for (Map.Entry<String, List<String>> testRequest : testRequests.entrySet()) {
            covidCache.stale(testRequest.getKey(), testRequest.getValue());
            covidCache.store(testResponse, testRequest.getKey(), testRequest.getValue());
        }

        Long ttl = (Long) ReflectionTestUtils.getField(covidCache, "ttl");
        assertNotNull(ttl);

        covidCache.stale(testRequestMethod, testRequestArgs);

        assertEquals(new CacheStats(1, testRequests.size(), testRequests.size()+1, ttl), covidCache.statsSnapshot());
    }

}
