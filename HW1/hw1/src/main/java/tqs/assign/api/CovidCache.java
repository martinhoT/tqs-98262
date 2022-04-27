package tqs.assign.api;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;
import tqs.assign.exceptions.NoCachedElementException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class CovidCache {

    @Getter @Setter private long ttl;

    private int hits = 0;
    private int misses = 0;

    private final CovidCacheMap cache;



    public CovidCache(
            @Value("${covid-cache.ttl}") final Long ttl,
            @Value("${covid-cache.max-size}") final Long maxSize) {
        this.ttl = ttl;

        cache = new CovidCacheMap(maxSize);
    }

    private record CacheValue(ResponseData response, Long timestamp) {}



    public void store(ApiQuery apiQuery, ResponseData responseData) {
        cache.put(apiQuery, new CacheValue(responseData, Instant.now().getEpochSecond()));
    }

    public ResponseData get(ApiQuery apiQuery) {
        if (!cache.containsKey(apiQuery))
            throw new NoCachedElementException();
        return cache.get(apiQuery).response();
    }

    public boolean stale(ApiQuery apiQuery) {
        return !cache.containsKey(apiQuery) || Instant.now().getEpochSecond() - cache.get(apiQuery).timestamp() > ttl;
    }

    /**
     * Convenience method that uses the 3 main methods.
     * This allows for obtaining a cached result if it's present, or to populate it if it's not.
     * <br>
     * This is the method that should be called for normal cache use.
     *
     * @param apiQuery the API query
     * @param responseDataProvider the supplier that will produce the updated response in case the one for the specified
     *                             API query is stale
     * @return the response that is cached for this API query
     */
    public ResponseData getOrStore(ApiQuery apiQuery, Function<ApiQuery, ResponseData> responseDataProvider) {
        if (stale(apiQuery)) {
            misses++;
            ResponseData response = responseDataProvider.apply(apiQuery);
            store(apiQuery, response);
            return response;
        }
        hits++;
        return get(apiQuery);
    }

    public CacheStats statsSnapshot() {
        return new CacheStats(hits, misses, cache.size(), ttl);
    }

    public void clear() {
        cache.clear();
    }

    public void resetStats() {
        hits = 0;
        misses = 0;
    }



    @EqualsAndHashCode(callSuper = true)
    private static class CovidCacheMap extends LinkedHashMap<ApiQuery, CacheValue> {

        private final long maxSize;

        public CovidCacheMap(long maxSize) { this.maxSize = maxSize; }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }

    }

}
