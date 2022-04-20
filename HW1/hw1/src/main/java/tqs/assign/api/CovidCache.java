package tqs.assign.api;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tqs.assign.data.CacheStats;
import tqs.assign.data.ResponseData;

import java.util.List;

@Component
public class CovidCache {

    @Value("covid-cache.ttl")
    private long ttl;

    public void store(ResponseData response, String requestMethod, List<String> requestArgs) {
        // TODO
    }

    public ResponseData get(String requestMethod, List<String> requestArgs) {
        // TODO
        return null;
    }

    public boolean stale(String requestMethod, List<String> requestArgs) {
        // TODO
        return false;
    }

    public CacheStats statsSnapshot() {
        // TODO
        return null;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static class CovidCacheKey {
        private final String requestMethod;
        private final List<String> requestArgs;
    }

}
