package tqs.assign.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.Stats;

import java.time.LocalDateTime;
import java.util.Date;
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

    public static class CovidApiQuery implements ApiQuery {

        @Override
        public ApiQuery atCountry(String countryISO) {
            // TODO
            return this;
        }

        @Override
        public ApiQuery after(LocalDateTime after) {
            // TODO
            return this;
        }

        @Override
        public ApiQuery before(LocalDateTime before) {
            // TODO
            return this;
        }

        @Override
        public ApiQuery atDate(LocalDateTime date) {
            // TODO
            return this;
        }

        @Override
        public Stats fetch() {
            // TODO
            return null;
        }
    }

    @Override
    public Stats getGlobalStats() {
        // TODO
        return new CovidApiQuery().fetch();
    }

    @Override
    public Stats getCountryStats(String countryISO) {
        // TODO
        return new CovidApiQuery()
                .atCountry(countryISO)
                .fetch();
    }

    @Override
    public ApiQuery getStats() {
        return new CovidApiQuery();
    }
}
