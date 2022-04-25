package tqs.assign.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.stream.Stream;

/**
 * Proxy bean that alternates between external APIs based on availability.
 */
@Service
@Slf4j
public class CovidApi implements Api {

    private final List<Api> supportedApis;
    @Getter private final Set<String> supportedCountries;
    private int chosenApiIdx;

    private final CovidCache covidCache;

    @Autowired
    public CovidApi(CovidCache covidCache,
                    JohnsHopkinsApi johnsHopkinsApi,
                    Covid19FastestUpdateApi covid19FastestUpdateApi) {

        this.covidCache = covidCache;

        supportedApis = Stream.of(
                johnsHopkinsApi,
                covid19FastestUpdateApi
        ).filter(Api::isEnabled).toList();
        chosenApiIdx = 0;

        log.info("Enabled external APIs: {}", supportedApis);

        supportedCountries = new HashSet<>();
        supportedApis.stream()
                .map(Api::getSupportedCountries)
                .forEach(supportedCountries::addAll);

        log.debug("Supported countries: {}", supportedCountries);

    }

    @Override
    public Stats getStats(ApiQuery query) {
        if (query.getAtCountry() != null)
            validateCountryIso(query.getAtCountry());

        return (Stats) covidCache.getOrStore(query, this::queryApis);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void fetchSupportedCountries() {
        supportedApis.forEach(Api::fetchSupportedCountries);
        supportedCountries.clear();
        supportedApis.stream()
                .map(Api::getSupportedCountries)
                .forEach(supportedCountries::addAll);
    }

    public CacheStats getCacheStats() {
        return covidCache.statsSnapshot();
    }



    private Stats queryApis(ApiQuery query) {
        Stats response = new NullStats();
        int initialApiIdx = chosenApiIdx;
        if (!supportedApis.isEmpty())
            do {
                Api chosenApi = supportedApis.get(chosenApiIdx);
                try {
                    response = chosenApi.getStats(query);
                } catch (UnavailableExternalApiException ex) {
                    log.debug("Api {} could not fulfill request: {}", chosenApi, ex.getMessage());
                    chosenApiIdx = (++chosenApiIdx) % supportedApis.size();
                }
            } while (response.isNull() && initialApiIdx != chosenApiIdx);

        if (response.isNull()) {
            log.error("No external API could fulfill the request: {}", query);
            throw new UnavailableApiException();
        }

        return response;
    }

    private void validateCountryIso(String countryISO) {
        if (!countryISO.matches("[A-Z]{1,2}"))
            throw new IncorrectlyFormattedCountryException(countryISO);
        if (!supportedCountries.contains(countryISO))
            throw new UnsupportedCountryISOException(countryISO);
    }

}
