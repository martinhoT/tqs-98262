package tqs.assign.data;

public record CacheStats(
        int hits,
        int misses,
        int stored,
        long ttl
) implements ResponseData {}
