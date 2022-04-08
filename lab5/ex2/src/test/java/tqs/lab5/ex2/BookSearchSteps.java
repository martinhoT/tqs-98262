package tqs.lab5.ex2;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.lab5.ex2.filters.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BookSearchSteps {

    Library library = new Library();
    List<Book> result = new ArrayList<>();

    @Given("a/another book with the title {string}, written by {string}, published in {iso8601Date}")
    public void addNewBook(String title, String author, LocalDateTime date) {
        library.addBook(new Book(title, author, date, false, new ArrayList<>()));
    }

    @Given("a/another book with the title {string}, written by {string}, published in {iso8601Date}, from {categories}")
    public void addNewBookWithCategories(String title, String author, LocalDateTime date, List<String> categories) {
        library.addBook(new Book(title, author, date, false, categories));
    }

    @When("the customer searches for{bookStatus} books{filters}")
    public void searchBook(String bookStatus, List<SearchFilter> filters) {
        if (!bookStatus.isEmpty())
            filters.add(new BookedFilter(bookStatus.equals("booked")));
        result = library.findBooks(filters);
    }

    @Then("{int} book(s) should have been found")
    public void booksShouldHaveBeenFound(final int booksFound) {
        assertEquals(result.size(), booksFound);
    }

    @Then("Book {int} should have the title {string}")
    public void bookShouldHaveTheTitleSomeOtherBook(final int position, final String title) {
        assertEquals(result.get(position - 1).getTitle(), title);
    }

    @Then("all books should not be booked")
    public void booksShouldBeUnbooked() {
        assertTrue(result.stream().noneMatch(Book::isBooked));
    }

    @Given("the books catalogue is initialized with the following books")
    public void theBooksCatalogueIsInitializedWithTheFollowingBooks(DataTable table) {
        List<Map<String, String>> rows = table.asMaps();

        for (Map<String, String> row : rows) {
            String title = row.get("title");
            String author = row.get("author");
            String[] dateStr = row.get("published").split("-");
            LocalDateTime published = iso8601Date(dateStr[0], dateStr[1], dateStr[2]);
            boolean booked = row.get("booked").equals("true");

            Book book = new Book(title, author, published, booked, new ArrayList<>());
            library.addBook(book);
        }
    }

    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    public LocalDateTime iso8601Date(String year, String month, String day){
        return LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),0, 0);
    }

    @ParameterType(" unbooked| booked|")
    public String bookStatus(String bookStatus) {
        return bookStatus;
    }

    @ParameterType("'(\\w+)'(?: and '(\\w+)')?")
    public List<String> categories(String... matches) {
        return Arrays.stream(matches).collect(Collectors.toList());
    }

    @ParameterType("(?: (of \\w+)| (with the title '[^\\']+')| (published between \\d+ and \\d+)| (written by '[^\\']++'))*")
    public List<SearchFilter> filters(String... matches) {
        List<String> filterRegexes = List.of(
                "of (\\w+)",
                "with the title '(.+)'",
                "published between (\\d+) and (\\d+)",
                "written by '(.+)'"
        );

        return Arrays.stream(matches).filter(Objects::nonNull).map(
                s -> {
                    String filterRegex = filterRegexes.stream().filter(s::matches).findFirst().orElse(null);
                    if (filterRegex == null)
                        return null;
                    Pattern filterPattern = Pattern.compile(filterRegex);
                    Matcher matcher = filterPattern.matcher(s);
                    if (matcher.find())
                        switch (filterRegexes.indexOf(filterRegex)) {
                            case 0: return new CategoryFilter(matcher.group(1));
                            case 1: return new TitleFilter(matcher.group(1));
                            case 2: return new YearFilter(
                                    LocalDateTime.of(Integer.parseInt(matcher.group(1)), 1, 1, 0, 0),
                                    LocalDateTime.of(Integer.parseInt(matcher.group(2))+1, 1, 1, 0, 0)
                            );
                            case 3: return new AuthorFilter(matcher.group(1));
                        }
                    return null;
                }
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
