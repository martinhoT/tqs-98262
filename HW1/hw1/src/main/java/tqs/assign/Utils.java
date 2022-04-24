package tqs.assign;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    private Utils() {}



    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.value( localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) );
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            return LocalDateTime.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

    }

    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {

        @Override
        public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
            if (localDate == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.value( localDate.format(DateTimeFormatter.ISO_LOCAL_DATE) );
        }

        @Override
        public LocalDate read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            return LocalDate.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE);
        }

    }

}
