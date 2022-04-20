package tqs.assign.api.external;

import org.springframework.stereotype.Component;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;

@Component
public class OpenCovidApi implements Api {

    @Override
    public Stats getGlobalStats() {
        // TODO
        return null;
    }

    @Override
    public Stats getCountryStats(String countryISO) {
        // TODO
        return null;
    }

    @Override
    public Stats getStats(ApiQuery query) {
        // TODO
        return null;
    }

}
