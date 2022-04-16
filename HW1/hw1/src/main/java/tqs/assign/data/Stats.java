package tqs.assign.data;

public record Stats(
    int confirmed,
    int newConfirmed,
    int deaths,
    int newDeaths,
    int recovered,
    int newRecovered,
    int active,
    double fatalityRate
) implements ResponseData {}
