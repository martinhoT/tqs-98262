package tqs.assign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tqs.assign.api.CovidApi;
import tqs.assign.data.Stats;

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
            @RequestParam(name="startPoint", required=false) int startPoint,
            @RequestParam(name="endPoint", required=false) int endPoint) {
        if (countryISO.isPresent())
            return covidApi.getStats(countryISO.get());
        return covidApi.getGlobalStats();
    }

}
