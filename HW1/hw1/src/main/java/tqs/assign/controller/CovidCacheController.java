package tqs.assign.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.assign.api.CovidApi;
import tqs.assign.data.CacheStats;

@RestController
@RequestMapping("/api/cache")
public class CovidCacheController {

    private CovidApi covidApi;

    @GetMapping("/stats")
    public CacheStats getStats() {
        // TODO
        return null;
    }

}
