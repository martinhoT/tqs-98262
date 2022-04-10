package tqs.assign.data;

import lombok.Data;

@Data
public class Stats {

    private int confirmed;
    private int newConfirmed;
    private int deaths;
    private int newDeaths;
    private int recovered;
    private int newRecovered;
    private int active;
    private double fatalityRate;

}
