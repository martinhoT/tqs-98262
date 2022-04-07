package tqs.lab5.ex2;

import tqs.lab5.ex2.filters.SearchFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Library {
    private final List<Book> store = new ArrayList<>();

    public void addBook(final Book book) {
        store.add(book);
    }

    public List<Book> findBooks(List<SearchFilter> filters) {
        Stream<Book> stream = store.stream();
        for (SearchFilter filter : filters) {
            stream = stream.filter(filter);
        }
        return stream
                .sorted(Comparator.comparing(Book::getPublished).reversed())
                .collect(Collectors.toList());
    }
}