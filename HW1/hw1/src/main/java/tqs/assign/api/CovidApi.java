package tqs.assign.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.assign.api.external.OpenCovidApi;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.CacheStats;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableApiException;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.util.List;

/**
 * Proxy bean that alternates between 2 external APIs based on availability.
 */
@Service
public class CovidApi implements Api {

    private final List<Api> supportedApis;
    private int chosenApiIdx;

    private final CovidCache covidCache;

    @Autowired
    public CovidApi(CovidCache covidCache,
                    VaccovidApi vaccovidApi,
                    OpenCovidApi openCovidApi) {

        supportedApis = List.of(
                vaccovidApi,
                openCovidApi
        );
        chosenApiIdx = 0;

        this.covidCache = covidCache;

    }

    @Override
    public Stats getGlobalStats() {
        return getStats(ApiQuery.builder().build());
    }

    @Override
    public Stats getCountryStats(String countryISO) {
        return getStats(ApiQuery.builder()
                .atCountry(countryISO)
                .build());
    }

    @Override
    public Stats getStats(ApiQuery query) {
        Stats response = null;
        int initialApiIdx = chosenApiIdx;
        do {
            Api chosenApi = supportedApis.get(chosenApiIdx);
            try {
                response = chosenApi.getStats(query);
            } catch (UnavailableExternalApiException ex) {
                chosenApiIdx = (++chosenApiIdx) % supportedApis.size();
            }
        } while (response == null && initialApiIdx != chosenApiIdx);

        if (response == null)
            throw new UnavailableApiException();

        return response;
    }

    public CacheStats getCacheStats() {
        return covidCache.statsSnapshot();
    }

}
