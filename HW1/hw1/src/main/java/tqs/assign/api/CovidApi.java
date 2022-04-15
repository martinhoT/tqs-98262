package tqs.assign.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.Stats;

import java.util.List;

/**
 * Proxy bean that alternates between 3 external APIs based on availability.
 */
@Service
public class CovidApi implements Api {

    private final List<Api> supportedApis;

    private final CovidCache covidCache;

    @Autowired
    public CovidApi(CovidCache covidCache,
            VaccovidApi vaccovidApi) {

        supportedApis = List.of(
                vaccovidApi
        );

        this.covidCache = covidCache;

    }

    @Override
    public Stats getGlobalStats() {
        // TODO
        return null;
    }

    @Override
    public Stats getStats(String country) {
        // TODO
        return null;
    }
}
