package tqs.lab5.ex2.filters;

import lombok.Getter;
import tqs.lab5.ex2.Book;

import java.util.Calendar;
import java.util.Date;

@Getter
public class YearFilter implements SearchFilter {

    private final Date from;
    private final Date to;

    private final Calendar end;

    public YearFilter(Date from, Date to) {
        this.from = from;
        this.to = to;

        end = Calendar.getInstance();
        end.setTime(to);
        end.roll(Calendar.YEAR, 1);
    }

    @Override
    public boolean test(Book book) {
        return from.before(book.getPublished()) && end.getTime()
                .after(book.getPublished());
    }

}
