package tqs.assign;

import com.google.gson.*;
import tqs.assign.data.Stats;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class TestUtils {

    private static final Random random = new Random();

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
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

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

}
