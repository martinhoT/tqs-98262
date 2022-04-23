package tqs.assign.api;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.assign.api.external.Covid19Api;
import tqs.assign.api.external.VaccovidApi;
import tqs.assign.data.CacheStats;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.IncorrectlyFormattedCountryException;
import tqs.assign.exceptions.UnavailableApiException;
import tqs.assign.exceptions.UnavailableExternalApiException;
import tqs.assign.exceptions.UnsupportedCountryISOException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Proxy bean that alternates between 2 external APIs based on availability.
 */
@Service
public class CovidApi implements Api {

    private final List<Api> supportedApis;
    @Getter private final Set<String> supportedCountries;
    private int chosenApiIdx;

    private final CovidCache covidCache;

    @Autowired
    public CovidApi(CovidCache covidCache,
                    VaccovidApi vaccovidApi,
                    Covid19Api covid19Api) {

        supportedApis = List.of(
                vaccovidApi,
                covid19Api
        );
        chosenApiIdx = 0;

        Set<String> firstSupportedCountries = supportedApis.get(0).getSupportedCountries();
        if (firstSupportedCountries != null)
            supportedCountries = supportedApis.stream()
                    .skip(1L)
                    .map(Api::getSupportedCountries)
                    .collect(() -> new HashSet<>(firstSupportedCountries), Set::retainAll, Set::retainAll);
        else
            supportedCountries = new HashSet<>();

        this.covidCache = covidCache;

    }

    @Override
    public Stats getStats(ApiQuery query) {
        if (query.getAtCountry() != null)
            validateCountryIso(query.getAtCountry());

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

    private void validateCountryIso(String countryISO) {
        if (!countryISO.matches("[A-Z]{1,3}"))
            throw new IncorrectlyFormattedCountryException(countryISO);
        if (!supportedCountries.contains(countryISO))
            throw new UnsupportedCountryISOException(countryISO);
    }

}
