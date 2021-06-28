package com.rb.nonbiz.date;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.time.temporal.TemporalAdjusters.firstInMonth;

public class RBDates {

  // The plain toString method in classes that implement PrintsInstruments
  // needs to pass in some date (which will not get used) to toString(InstrumentMaster, LocalDate).
  // We use UNUSED_DATE, instead of LocalDate.MIN, for more explicit semantics.
  public static final LocalDate UNUSED_DATE = LocalDate.MIN;

  public static LocalDate maxDate(LocalDate date1, LocalDate date2) {
    return date1.isBefore(date2) ? date2 : date1;
  }

  public static LocalDate maxDate(LocalDate date1, Optional<LocalDate> date2) {
    if (!date2.isPresent()) {
      return date1;
    }
    return date1.isBefore(date2.get()) ? date2.get() : date1;
  }

  public static LocalDate minDate(Collection<LocalDate> dates) {
    return minDate(dates.iterator());
  }

  public static LocalDate maxDate(Collection<LocalDate> dates) {
    return maxDate(dates.iterator());
  }

  public static LocalDate maxDate(Stream<LocalDate> stream) {
    return maxDate(stream.iterator());
  }

  public static LocalDate maxDate(Iterator<LocalDate> iterator) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot call maxDate with no dates");
    LocalDate max = iterator.next();
    while (iterator.hasNext()) {
      LocalDate thisDate = iterator.next();
      if (thisDate.isAfter(max)) {
        max = thisDate;
      }
    }
    return max;
  }

  public static LocalDate minDate(LocalDate date1, LocalDate date2) {
    return date2.isBefore(date1) ? date2 : date1;
  }

  public static LocalDate minDate(LocalDate date1, Optional<LocalDate> date2) {
    if (!date2.isPresent()) {
      return date1;
    }
    return date2.get().isBefore(date1) ? date2.get() : date1;
  }

  public static LocalDate minDate(Stream<LocalDate> stream) {
    return minDate(stream.iterator());
  }

  public static LocalDate minDate(Iterator<LocalDate> iterator) {
    RBPreconditions.checkArgument(
        iterator.hasNext(),
        "You cannot call minDate with no dates");
    LocalDate min = iterator.next();
    while (iterator.hasNext()) {
      LocalDate thisDate = iterator.next();
      if (thisDate.isBefore(min)) {
        min = thisDate;
      }
    }
    return min;
  }

  public static String yyMMdd_hhmmss_NoHyphens(LocalDateTime localDateTime) {
    // We will need to hire Y2.1K consultants for this at some point
    LocalDate date = localDateTime.toLocalDate();
    return String.format("%02d%02d%02d-%02d%02d%02d",
        date.getYear() % 100, date.getMonthValue(), date.getDayOfMonth(),
        localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
  }

  public static String yyMMddNoHyphens(LocalDate date) {
    // We will need to hire Y2.1K consultants for this at some point
    return String.format("%02d%02d%02d", date.getYear() % 100, date.getMonthValue(), date.getDayOfMonth());
  }

  public static String yyyyMMddNoHyphens(LocalDate date) {
    return String.format("%04d%02d%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  public static String yyyyMMdd(LocalDate date) {
    // ISO_LOCAL_DATE means YYYY-MM-DD
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  public static LocalDate dateFromYyyyMmDd(String yyyyMMdd) {
    try {
      // ISO_LOCAL_DATE means YYYY-MM-DD, with strict enforcement of valid dates. E.g. can't have 2016-01-32 or 2016-13-01
      return LocalDate.parse(yyyyMMdd, DateTimeFormatter.ISO_LOCAL_DATE);
    } catch (DateTimeParseException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  public static boolean fallsOnWeekend(LocalDate date) {
    return
        date.getDayOfWeek().equals(DayOfWeek.SATURDAY) ||
            date.getDayOfWeek().equals(DayOfWeek.SUNDAY);
  }

  /**
   * Helps to find e.g. the 3rd Monday in February 2019.
   * n = 1 means first e.g. Monday in YearMonth - so n starts at 1 instead of 0
   */
  public static Optional<LocalDate> findNthDay(YearMonth yearMonth, int n, DayOfWeek dayOfWeek) {
    RBPreconditions.checkArgument(
        1 <= n && n <= 5,
        "You can ask for anywhere between the 1st or 5th %s in a year/month %s , but not %s",
        dayOfWeek, yearMonth, n);
    // OK, get to the 1st 'dayOfWeek' for this yearMonth.
    LocalDate date = yearMonth.atDay(1).with(firstInMonth(dayOfWeek));
    n--; // adjust it to be in 0 .. n-1 (more

    for (int i = 1; i <= n; i++) {
      date = date.plusWeeks(1);
    }
    return date.getMonth().equals(yearMonth.getMonth())
        ? Optional.of(date)
        : Optional.empty();
  }

  /**
   * If supplied date is on a weekend, return the nearest weekday (Fri if Sat was supplied, Mon if Sun was supplied).
   * Otherwise, return the same date.
   */
  public static LocalDate getNearestWeekday(LocalDate date) {
    switch (date.getDayOfWeek()) {
      case SATURDAY: return date.minusDays(1);
      case SUNDAY:   return date.plusDays(1);
      default:       return date;
    }
  }

  public static Year parseValidYear(String asYear) {
    try {
      return checkYearIsReasonable(Year.of(Integer.parseInt(asYear)));
    } catch (NumberFormatException e) {
      throw new NumberFormatException(
          Strings.format("String '%s' is not a valid year: %s", asYear, e));
    }
  }

  public static Year checkYearIsReasonable(Year year) {
    RBPreconditions.checkArgument(
        1_800 <= year.getValue() && year.getValue() <= 2_200,
        "%s is a valid year in history, but it's too far in the past or future; you probably have an error somewhere",
        year);
    return year;
  }

  public static boolean yearsAreConsecutive(Year year1, Year year2) {
    return year2.getValue() == year1.getValue() + 1;
  }

  // FIXME IAK DRIFT test this
  public static boolean doubleIsRound(double value, double epsilon) {
    RBPreconditions.checkArgument(
        epsilon >= 0,
        "epsilon to check % cannot be negative: %s",
        value, epsilon);
    return Math.abs(value - (int) value) <= epsilon;
  }

}
