package tqs.assign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.assign.api.CovidApi;
import tqs.assign.data.CacheStats;

@RestController
@RequestMapping("/api/cache")
public class CovidCacheController {

    private final CovidApi covidApi;

    @Autowired
    public CovidCacheController(CovidApi covidApi) { this.covidApi = covidApi; }

    @GetMapping("/stats")
    public CacheStats getStats() {
        return covidApi.getCacheStats();
    }

}
