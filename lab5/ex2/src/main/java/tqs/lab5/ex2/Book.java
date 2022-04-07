package tqs.lab5.ex2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class Book {

    private final String title;
    private final String author;
    private final Date published;

}