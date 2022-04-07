package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

import java.util.List;

@Getter
@AllArgsConstructor
public class CategoryFilter implements SearchFilter {

    private final List<String> categories;

    @Override
    public boolean test(Book book) {
        return book.getCategories().containsAll(categories);
    }

}
