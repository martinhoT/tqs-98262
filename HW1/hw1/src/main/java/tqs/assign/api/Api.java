package tqs.assign.api;

import tqs.assign.data.Stats;

import java.io.IOException;

public interface Api {

    Stats getGlobalStats();

    Stats getStats(String countryISO);

}
