package tqs.assign;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tqs.assign.data.Stats;

import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    public static final Gson gson = new GsonBuilder()
            .create();

    public static Stats randomStats() {
        return new Stats(
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextInt(1000),
                random.nextInt(100000),
                random.nextDouble(1.0)
        );
    }
}
