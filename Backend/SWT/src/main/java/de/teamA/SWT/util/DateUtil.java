package de.teamA.SWT.util;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.time.temporal.ChronoUnit.DAYS;

public class DateUtil {

    public static final ZoneId ZONE_ID = ZoneId.systemDefault();

    public static Date toDate(LocalDateTime localDateTimeToConvert) {
        return Date.from(localDateTimeToConvert.atZone(ZONE_ID).toInstant());
    }

    public static Date toDate(LocalDate localDateToConvert) {
        return Date.from(localDateToConvert.atStartOfDay().atZone(ZONE_ID).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime()).atZone(ZONE_ID).toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime()).atZone(ZONE_ID).toLocalDate();
    }

    public static long daysBetween(LocalDate dueDate, LocalDate borrowDate) {
        long days = DAYS.between(borrowDate, dueDate);

        if (days < 0) {
            throw new ValidationException("Due date must be later than the borrow date");
        }
        return days;
    }

}
