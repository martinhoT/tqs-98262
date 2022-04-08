package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class YearFilter implements SearchFilter {

    private final LocalDateTime from;
    private final LocalDateTime to;

    @Override
    public boolean test(Book book) {
        return book.getPublished().isAfter(from) && book.getPublished().isBefore(to);
    }

}
