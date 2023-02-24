package com.rb.nonbiz.date;

import com.rb.nonbiz.text.Strings;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Methods pertaining to calendar day calculations (vs. market days).
 */
public class CalendarDays {

  public static int countWeekdaysBetween(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException(smartFormat("Dates are reversed: %s -> %s", startDate, endDate));
    }
    // This could be more efficient but whatever
    int numWeekdays = 0;
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
        numWeekdays++;
      }
    }
    return numWeekdays;
  }

}
