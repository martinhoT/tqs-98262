package tqs.lab5.ex2;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookSearchSteps {

    Library library = new Library();
    List<Book> result = new ArrayList<>();

    @Given(" book with the title '{string}', written by '{string}', published in {iso8601date}")
    public void addNewBook(String title, String author, LocalDateTime date) {

    }

    @ParameterType()

}
