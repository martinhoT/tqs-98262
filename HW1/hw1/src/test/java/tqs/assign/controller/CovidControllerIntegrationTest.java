package tqs.assign.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import tqs.assign.exceptions.IncorrectlyFormattedCountryException;
import tqs.assign.exceptions.UnsupportedCountryISOException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This suite focuses on exception handling,
 * which requires multiple parts of the Spring Boot application run un-mocked (such as Exception Handlers)
 */
@SpringBootTest
@AutoConfigureMockMvc
class CovidControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;



    @Test
    @DisplayName("Bad Request when date arguments are not properly formatted")
    void whenDateArgumentsAreNotFormatted_thenExpectBadRequest() throws Exception {
        String baseMsg = "Date argument '%s' is not properly formatted (ISO Local Date format: yyyy-MM-dd)";
        Map<String, String> parameters = Map.of(
                "date", "2022-13-01",
                "before", "01-12-2022",
                "after", "11-22-2022"
        );

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            String param = parameter.getKey();
            String value = parameter.getValue();

            mvc.perform(get("/api/covid/stats")
                            .queryParam(param, value))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason(baseMsg.formatted(value)));
        }
    }

    @Test
    @DisplayName("Bad Request when country ISO is not properly formatted")
    void whenInvalidCountryISO_thenExpectBadRequest() throws Exception {
        String baseMsg = "Country argument '%s' is not properly formatted (ISO 3166-1 alpha code)";
        List<String> countryISOCodes = List.of("Portugal", "876", "United Kingdom", "O_O");

        for (String countryISOCode : countryISOCodes)
            mvc.perform(get("/api/covid/stats/{country}", countryISOCode))
                    .andExpect(status().isBadRequest())
                    .andExpect(status().reason(baseMsg.formatted(countryISOCode)))
                    .andExpect(result -> assertInstanceOf(IncorrectlyFormattedCountryException.class, result.getResolvedException()));
    }

    @Test
    @DisplayName("Not Found when country ISO doesn't exist (not supported)")
    void whenNonExistentCountryISO_thenExpectNotFound() throws Exception {
        String baseMsg = "The specified country ISO code '%s' does not exist in this platform";
        List<String> countryISOCodes = List.of("ABC", "ZZZ", "WOW");

        for (String countryISOCode : countryISOCodes)
            mvc.perform(get("/api/covid/stats/{country}", countryISOCode))
                    .andExpect(status().isNotFound())
                    .andExpect(status().reason(baseMsg.formatted(countryISOCode)))
                    .andExpect(result -> assertInstanceOf(UnsupportedCountryISOException.class, result.getResolvedException()));
    }

}
