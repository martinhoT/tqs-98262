package tqs.assign.api;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tqs.assign.api.external.Covid19FastestUpdateApi;
import tqs.assign.api.external.JohnsHopkinsApi;
import tqs.assign.data.CacheStats;
import tqs.assign.data.NullStats;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.IncorrectlyFormattedCountryException;
import tqs.assign.exceptions.UnavailableApiException;
import tqs.assign.exceptions.UnavailableExternalApiException;
import tqs.assign.exceptions.UnsupportedCountryISOException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Proxy bean that alternates between external APIs based on availability.
 */
@Service
@Log4j2
public class CovidApi implements Api {

    private final List<Api> supportedApis;
    @Getter private final Set<String> supportedCountries;
    private int chosenApiIdx;

    private final CovidCache covidCache;

    @Autowired
    public CovidApi(CovidCache covidCache,
                    JohnsHopkinsApi johnsHopkinsApi,
                    Covid19FastestUpdateApi covid19FastestUpdateApi) {

        supportedApis = List.of(
                johnsHopkinsApi,
                covid19FastestUpdateApi
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

        return (Stats) covidCache.getOrStore(query, this::queryApis);
    }

    public CacheStats getCacheStats() {
        return covidCache.statsSnapshot();
    }



    private Stats queryApis(ApiQuery query) throws UnavailableApiException {
        Stats response = new NullStats();
        int initialApiIdx = chosenApiIdx;
        do {
            Api chosenApi = supportedApis.get(chosenApiIdx);
            try {
                response = chosenApi.getStats(query);
            } catch (UnavailableExternalApiException ex) {
                chosenApiIdx = (++chosenApiIdx) % supportedApis.size();
            }
        } while (response.isNull() && initialApiIdx != chosenApiIdx);

        if (response.isNull())
            throw new UnavailableApiException();

        return response;
    }

    private void validateCountryIso(String countryISO) {
        if (!countryISO.matches("[A-Z]{1,3}"))
            throw new IncorrectlyFormattedCountryException(countryISO);
        if (!supportedCountries.contains(countryISO))
            throw new UnsupportedCountryISOException(countryISO);
    }

}
