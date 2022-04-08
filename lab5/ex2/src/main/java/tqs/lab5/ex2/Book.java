package tqs.lab5.ex2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Book {

    private final String title;
    private final String author;
    private final LocalDateTime published;
    private final boolean booked;
    private final List<String> categories;

}