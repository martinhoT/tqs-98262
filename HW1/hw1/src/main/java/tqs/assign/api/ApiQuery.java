package tqs.assign.api;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
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

}
