package tqs.assign.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class CacheStats implements ResponseData {

    private final int hits;
    private final int misses;
    private final int stored;
    private final long ttl;

}
