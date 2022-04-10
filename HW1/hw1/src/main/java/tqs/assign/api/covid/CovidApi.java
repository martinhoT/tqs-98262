package tqs.assign.api.covid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tqs.assign.api.covid.external.VaccovidApi;

import java.util.List;

/**
 * Proxy bean that alternates between 3 external APIs based on availability.
 */
@Component
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

}
