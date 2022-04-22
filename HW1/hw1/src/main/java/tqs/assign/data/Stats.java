package tqs.assign.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Stats implements ResponseData {

    private final int confirmed;
    private final int newConfirmed;
    private final int deaths;
    private final int newDeaths;
    private final int recovered;
    private final int newRecovered;
    private final int active;
    private final double fatalityRate;

}