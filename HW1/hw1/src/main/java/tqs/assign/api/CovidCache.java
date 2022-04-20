package tqs.assign.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;

@Component
public class CovidCache {

    @Value("covid-cache.ttl")
    @Getter
    private long ttl;

    public void store(ApiQuery apiQuery, ResponseData responseData) {
        // TODO
    }

    public ResponseData get(ApiQuery apiQuery) {
        // TODO
        return null;
    }

    public boolean stale(ApiQuery apiQuery) {
        // TODO
        return false;
    }

    public CacheStats statsSnapshot() {
        // TODO
        return null;
    }

}
