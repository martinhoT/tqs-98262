package tqs.assign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tqs.assign.api.CovidApi;
import tqs.assign.data.Stats;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/covid")
public class CovidController {

    private final CovidApi covidApi;

    @Autowired
    public CovidController(CovidApi covidApi) {
        this.covidApi = covidApi;
    }

    @GetMapping({ "/stats", "/stats/{countryISO}" })
    public Stats getStats(
            @PathVariable(name="countryISO", required=false) Optional<String> countryISO,
            @RequestParam(name="date", required=false) LocalDateTime date,
            @RequestParam(name="after", required=false) LocalDateTime after,
            @RequestParam(name="before", required=false) LocalDateTime before) {
        if (countryISO.isPresent())
            return covidApi.getCountryStats(countryISO.get());
        return covidApi.getGlobalStats();
    }

}
