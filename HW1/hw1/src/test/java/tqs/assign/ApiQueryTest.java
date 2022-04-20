package tqs.assign;

import org.junit.jupiter.api.Test;
import tqs.assign.api.ApiQuery;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiQueryTest {

    private ApiQuery apiQuery;



    @Test
    void whenBuilt_thenReturnCorrectEffectiveQuery() {
        String atCountry = "PT";
        LocalDate atDate = LocalDate.of(2022, 1, 1);
        LocalDate before = LocalDate.of(2022, 2, 2);
        LocalDate after = LocalDate.of(2021, 1, 1);
        String effectiveQuery;

        effectiveQuery = "getStats";
        assertEquals(effectiveQuery, ApiQuery.builder().build().effectiveQuery());

        effectiveQuery = "getStatsAtCountryAfter";
        assertEquals(effectiveQuery,
            ApiQuery.builder()
                .after(after)
                .atCountry(atCountry)
                .build().effectiveQuery());
        assertEquals(effectiveQuery,
            ApiQuery.builder()
                .atCountry(atCountry)
                .after(after)
                .build().effectiveQuery());
        assertEquals(effectiveQuery,
            ApiQuery.builder()
                .after(before)
                .atCountry(atCountry)
                .after(after)
                .build().effectiveQuery());

        effectiveQuery = "getStatsAtCountryAtDateAfterBefore";
        assertEquals(effectiveQuery,
            ApiQuery.builder()
                .after(after)
                .atCountry(atCountry)
                .before(before)
                .atDate(atDate)
                .build().effectiveQuery());
        assertEquals(effectiveQuery,
                ApiQuery.builder()
                        .atDate(atDate)
                        .before(before)
                        .atCountry(atCountry)
                        .after(after)
                        .build().effectiveQuery());
        assertEquals(effectiveQuery,
                ApiQuery.builder()
                        .atDate(after)
                        .before(before)
                        .atDate(atDate)
                        .atCountry("GB")
                        .atCountry(atCountry)
                        .after(after)
                        .before(before)
                        .build().effectiveQuery());
    }

}
