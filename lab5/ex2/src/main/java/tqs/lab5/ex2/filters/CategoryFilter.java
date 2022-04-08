package tqs.lab5.ex2.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tqs.lab5.ex2.Book;

@Getter
@AllArgsConstructor
public class CategoryFilter implements SearchFilter {

    private final String category;

    @Override
    public boolean test(Book book) {
        return category.equals("Anything") || book.getCategories().contains(category);
    }

}
