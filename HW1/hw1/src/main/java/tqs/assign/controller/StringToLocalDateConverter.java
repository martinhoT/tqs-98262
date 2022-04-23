package tqs.assign.controller;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tqs.assign.exceptions.IncorrectlyFormattedDateException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    @Override
    public LocalDate convert(String date) {
        try {
            return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IncorrectlyFormattedDateException(date);
        }
    }

}
