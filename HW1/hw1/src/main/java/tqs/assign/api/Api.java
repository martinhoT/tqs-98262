package tqs.assign.api;

import tqs.assign.data.Stats;

public interface Api {

    // Convenience methods
    Stats getGlobalStats();
    Stats getCountryStats(String countryISO);

    ApiQuery getStats();

}
