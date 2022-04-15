package tqs.assign.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tqs.assign.api.CovidApi;
import tqs.assign.data.Stats;

@RestController()
@RequestMapping("/api/covid")
public class CovidController {

    private CovidApi covidApi;

    @GetMapping("/stats")
    public Stats getStats(@RequestParam(name="country", required=false) String country) {
        if (country.isEmpty())
            return covidApi.getGlobalStats();
        return covidApi.getStats(country);
    }

}
