package tqs.assign.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@EqualsAndHashCode
public class ApiQuery {

    private final String atCountry;
    private final LocalDate after;
    private final LocalDate before;
    private final LocalDate atDate;

    @Builder
    public ApiQuery(String atCountry, LocalDate after, LocalDate before, LocalDate atDate) {
        this.atCountry = atCountry;
        this.after = after;
        this.before = before;
        this.atDate = atDate;
    }

    private String effectiveQueryStr;

    public String effectiveQuery() {
        if (effectiveQueryStr == null)
            effectiveQueryStr = "getStats" +
                    (atCountry != null ? "AtCountry" : "") +
                    (atDate != null ? "AtDate" : "") +
                    (after != null ? "After" : "") +
                    (before != null ? "Before" : "");
        return effectiveQueryStr;
    }

}
