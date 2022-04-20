package tqs.assign.api;

import tqs.assign.data.Stats;

import java.time.LocalDate;

public interface ApiQuery {

    ApiQuery atCountry(String countryISO);

    ApiQuery after(LocalDate after);

    ApiQuery before(LocalDate before);

    ApiQuery atDate(LocalDate date);

    Stats fetch();

}
