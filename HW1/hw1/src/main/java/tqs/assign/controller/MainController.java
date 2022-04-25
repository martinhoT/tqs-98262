package tqs.assign.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.IncorrectlyFormattedParametersException;
import tqs.assign.exceptions.UnavailableApiException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@Controller
@Log4j2
public class MainController {

    private CovidController restCovidApi;
    private CovidCacheController restCacheApi;

    @Autowired
    public MainController(CovidController restCovidApi, CovidCacheController restCacheApi) {
        this.restCovidApi = restCovidApi;
        this.restCacheApi = restCacheApi;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("countryList",
                restCovidApi.getCountries().stream()
                        .map(iso2 -> {
                            Locale locale = new Locale("", iso2);
                            return new FormCountry(iso2, locale.getDisplayCountry());
                        })
                        .sorted((c1,c2) -> String.CASE_INSENSITIVE_ORDER.compare(c1.getIso2(), c2.getIso2()))
                        .toList()
        );
        model.addAttribute("covidStatsForm", new CovidStatsForm());
        return "index";
    }

    @GetMapping("/covid_stats")
    public String covidStats(@ModelAttribute CovidStatsForm covidStatsForm, Model model) {
        Optional<String> country = covidStatsForm.isWorld() ? Optional.empty() : Optional.of(covidStatsForm.getCountry());
        Optional<LocalDate> date = convertFormDateStringToParameter(covidStatsForm.getDateAt());
        Optional<LocalDate> before = convertFormDateStringToParameter(covidStatsForm.getDateBefore());
        Optional<LocalDate> after = convertFormDateStringToParameter(covidStatsForm.getDateAfter());

        Stats stats = restCovidApi.getStats(country, date, before, after);

        model.addAttribute("stats", stats);

        return "results-covid";
    }

    @GetMapping("/cache_stats")
    public String cacheStats(Model model) {
        model.addAttribute("stats", restCacheApi.getStats());

        return "results-cache";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleApiException(HttpServletRequest request, ResponseStatusException ex) {
        log.error("Request {} raised {}", request.getRequestURL(), ex);

        ModelAndView mav = new ModelAndView();
        mav.addObject("reason", ex.getReason());
        mav.setViewName("error-page");
        return mav;
    }



    @Data
    @AllArgsConstructor
    private static class FormCountry {
        private String iso2;
        private String name;
    }

    @Data
    private static class CovidStatsForm {
        private boolean world;
        private String country;
        private String dateBefore;
        private String dateAfter;
        private String dateAt;
    }

    private Optional<LocalDate> convertFormDateStringToParameter(String date) {
        return date.isEmpty() ? Optional.empty()
                : Optional.of(LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE));
    }

}
