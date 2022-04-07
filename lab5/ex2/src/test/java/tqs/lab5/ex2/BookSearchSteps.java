package tqs.lab5.ex2;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import tqs.lab5.ex2.filters.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookSearchSteps {

    Library library = new Library();
    List<Book> result = new ArrayList<>();

    @Given(" book with the title '{string}', written by '{string}', published in {iso8601date}")
    public void addNewBook(String title, String author, LocalDateTime date) {

    }

    @When("the customer searches for {bookStatus} books{filters}")
    public void searchBook(String bookStatus, List<SearchFilter> filters) {
        if (bookStatus.length() > 0)
            filters.add(new BookedFilter(bookStatus.equals("booked")));
        library.findBooks(filters);
    }

    @ParameterType("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    public LocalDateTime iso8601Date(String year, String month, String day){
        return LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day),0, 0);
    }

    @ParameterType("unbooked|booked|")
    public String bookStatus(String bookStatus) {
        return bookStatus;
    }

    @ParameterType(" (?:(of .+)|(with the title '.+')|(published between \\d+ and \\d+)|(written by '.+'))")
    public List<SearchFilter> filters(String... matches) {
        List<String> filterRegexes = List.of(
                "of (.+ (?: and (\\s)+)", //???
                "with the title '(.+)'",
                "published between (\\d+) and (\\d+)",
                "written by '(.+)'"
        );
        Arrays.stream(matches).map(
                s -> {
                    String filterRegex = filterRegexes.stream().filter(s::matches).findFirst().orElse(null);
                    if (filterRegex == null)
                        return null;
                    Pattern filterPattern = Pattern.compile(filterRegex);
                    Matcher matcher = filterPattern.matcher(s);
                    switch (filterRegexes.indexOf(filterRegex)) {
                        case 0:
                            return new CategoryFilter(matcher.group(0));
                            break;
                        case 1:
                            return new TitleFilter(matcher.group(0));
                            break;
                        case 2:
                            return new YearFilter(, Integer.parseInt(matcher.group(1)));
                            break;
                        case 3:
                            return new AuthorFilter(matcher.group(0));
                    }
                }
        );


    }

}
