package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

@Getter
@AllArgsConstructor
public class BookedFilter implements SearchFilter {

    private final boolean booked;


    @Override
    public boolean test(Book book) {
        return book.isBooked() == booked;
    }

}
