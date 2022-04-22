package tqs.assign.api.external.covid19;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Covid19Stats {

    private String id;
    private String country;
    private String countryCode;
    private int confirmed;
    private int deaths;
    private int recovered;
    private int active;
    private LocalDateTime date;

}
