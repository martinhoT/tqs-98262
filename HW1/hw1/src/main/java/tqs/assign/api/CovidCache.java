package tqs.assign.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.exceptions.NoCachedElementException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class CovidCache {

    @Getter
    @Value("${covid-cache.ttl}")
    private long ttl;

    private int hits = 0;
    private int misses = 0;

    private final Map<ApiQuery, ResponseData> cache = new HashMap<>();
    private final Map<ApiQuery, Long> timestamps = new HashMap<>();



    public ResponseData store(ApiQuery apiQuery, ResponseData responseData) {
        cache.put(apiQuery, responseData);
        timestamps.put(apiQuery, Instant.now().getEpochSecond());
        return responseData;
    }

    public ResponseData get(ApiQuery apiQuery) {
        if (!cache.containsKey(apiQuery))
            throw new NoCachedElementException();
        return cache.get(apiQuery);
    }

    public boolean stale(ApiQuery apiQuery) {
        boolean stale = !timestamps.containsKey(apiQuery) || Instant.now().getEpochSecond() - timestamps.get(apiQuery) > ttl;
        if (stale) misses++;
        else hits++;
        return stale;
    }

    /**
     * Convenience method that uses the 3 main methods.
     * This allows for obtaining a cached result if it's present, or to populate it if it's not.
     *
     * @param apiQuery the API query
     * @param responseDataSupplier the supplier that will produce the updated response in case the one for the specified
     *                             API query is stale
     * @return the response that is cached for this API query
     */
    public ResponseData getOrStore(ApiQuery apiQuery, Supplier<ResponseData> responseDataSupplier) {
        return stale(apiQuery)
                ? store(apiQuery, responseDataSupplier.get())
                : get(apiQuery);
    }

    public CacheStats statsSnapshot() {
        return new CacheStats(hits, misses, cache.size(), ttl);
    }

}
