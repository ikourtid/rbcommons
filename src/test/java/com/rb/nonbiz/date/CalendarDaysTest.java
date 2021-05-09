package com.rb.nonbiz.date;

import org.junit.Test;

import java.time.LocalDate;

import static com.rb.nonbiz.date.CalendarDays.countWeekdaysBetween;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class CalendarDaysTest {

  @Test
  public void datesReversed_throws() {
    assertIllegalArgumentException( () ->
        countWeekdaysBetween(LocalDate.of(2016, 10, 25), LocalDate.of(2016, 10, 24)));
  }

  @Test
  public void dateSame_isNotWeekday_returns0() {
    assertEquals(1, countWeekdaysBetween(LocalDate.of(2016, 10, 25), LocalDate.of(2016, 10, 25)));
  }

  @Test
  public void dateSame_isWeekday_returns1() {
    assertEquals(1, countWeekdaysBetween(LocalDate.of(2016, 10, 25), LocalDate.of(2016, 10, 25)));
  }

  @Test
  public void marketHolidayExists_doesNotCount() {
    assertEquals(1, countWeekdaysBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2016, 1, 1)));
  }

  @Test
  public void fridayToNextMonday_returns2() {
    assertEquals(2, countWeekdaysBetween(LocalDate.of(2016, 10, 21), LocalDate.of(2016, 10, 24)));
  }

  @Test
  public void saturdayToSunday_returns0() {
    assertEquals(0, countWeekdaysBetween(LocalDate.of(2016, 10, 22), LocalDate.of(2016, 10, 23)));
  }

  @Test
  public void sundayToMonday_returns1() {
    assertEquals(1, countWeekdaysBetween(LocalDate.of(2016, 10, 23), LocalDate.of(2016, 10, 24)));
  }

}
