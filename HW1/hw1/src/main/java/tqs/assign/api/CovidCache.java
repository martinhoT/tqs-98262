package tqs.assign.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;

import java.util.function.Supplier;

@Component
public class CovidCache {

    @Getter
    @Value("${covid-cache.ttl}")
    private long ttl;

    public ResponseData store(ApiQuery apiQuery, ResponseData responseData) {
        // TODO
        return null;
    }

    public ResponseData get(ApiQuery apiQuery) {
        // TODO
        return null;
    }

    public boolean stale(ApiQuery apiQuery) {
        // TODO
        return false;
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
        // TODO
        return null;
    }

}
