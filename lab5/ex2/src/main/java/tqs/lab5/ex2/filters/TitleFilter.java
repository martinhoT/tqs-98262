package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

@Getter
@AllArgsConstructor
public class TitleFilter implements SearchFilter {

    private final String title;

    @Override
    public boolean test(Book book) {
        return book.getTitle().equals(title);
    }

}
