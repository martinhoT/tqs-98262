package tqs.assign.api;

import tqs.assign.data.Stats;

import java.time.LocalDateTime;

public interface ApiQuery {

    ApiQuery atCountry(String countryISO);

    ApiQuery after(LocalDateTime after);

    ApiQuery before(LocalDateTime before);

    ApiQuery atDate(LocalDateTime date);

    Stats fetch();

}
