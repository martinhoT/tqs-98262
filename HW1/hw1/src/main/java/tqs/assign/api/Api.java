package tqs.assign.api;

import tqs.assign.data.Stats;

import java.util.Set;

public interface Api {

    Stats getStats(ApiQuery query);

    Set<String> getSupportedCountries();

    boolean isEnabled();

}
