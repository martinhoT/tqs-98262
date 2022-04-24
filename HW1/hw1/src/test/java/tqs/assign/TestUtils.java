package tqs.assign;

import tqs.assign.data.Stats;

import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    private TestUtils() {}

    public static Stats randomStats() {
        return new Stats(
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextDouble(1.0)
        );
    }

}
