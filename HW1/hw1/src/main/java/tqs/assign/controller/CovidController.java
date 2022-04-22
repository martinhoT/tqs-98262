package tqs.assign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/covid")
public class CovidController {

    private final CovidApi covidApi;

    @Autowired
    public CovidController(CovidApi covidApi) { this.covidApi = covidApi; }

    @GetMapping({ "/stats", "/stats/{country}" })
    public Stats getStats(
            @PathVariable(name="country", required=false) Optional<String> countryISO,
            @RequestParam(name="date", required=false) Optional<LocalDate> date,
            @RequestParam(name="after", required=false) Optional<LocalDate> after,
            @RequestParam(name="before", required=false) Optional<LocalDate> before) {

        ApiQuery.ApiQueryBuilder apiQueryBuilder = ApiQuery.builder();
        countryISO.ifPresent(apiQueryBuilder::atCountry);
        date.ifPresent(apiQueryBuilder::atDate);
        after.ifPresent(apiQueryBuilder::after);
        before.ifPresent(apiQueryBuilder::before);

        return covidApi.getStats(apiQueryBuilder.build());
    }

}
