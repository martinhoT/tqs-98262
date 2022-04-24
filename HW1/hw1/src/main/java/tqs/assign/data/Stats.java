package tqs.assign.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Stats implements ResponseData {

    public static final int UNSUPPORTED_FIELD = -1;

    private final int confirmed;
    private final int newConfirmed;
    private final int deaths;
    private final int newDeaths;
    private final int recovered;
    private final int newRecovered;
    private final int active;
    private final int newActive;
    private final double fatalityRate;

    @Override
    public boolean isNull() { return false; }

}