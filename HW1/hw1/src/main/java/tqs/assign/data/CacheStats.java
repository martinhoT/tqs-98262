package tqs.assign.data;

public record CacheStats(
        int hits,
        int misses,
        int total,
        long ttl
) implements ResponseData {}
