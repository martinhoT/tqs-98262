package tqs.assign.data;

public record CacheStats(
        int hits,
        int misses,
        int total,
        int stored,
        long ttl
) implements ResponseData {}
