package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

@Getter
@AllArgsConstructor
public class AuthorFilter implements SearchFilter {

    private final String author;

    @Override
    public boolean test(Book book) {
        System.err.println(book.getAuthor());
        System.err.println(author);
        return book.getAuthor().equals(author);
    }

}
