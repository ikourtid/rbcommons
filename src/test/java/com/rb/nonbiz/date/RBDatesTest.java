package com.rb.nonbiz.date;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Optional;

import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.contiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMap.singletonContiguousDiscreteRangeMap;
import static com.rb.nonbiz.collections.ContiguousDiscreteRangeMapTest.contiguousDiscreteRangeMapEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.date.RBDates.*;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY0;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY1;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY2;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY3;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY4;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY5;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY6;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY7;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY8;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBDatesTest {

  @Test
  public void testMaxDate() {
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 3)));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 3), LocalDate.of(1974, 4, 4)));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 4)));
  }

  @Test
  public void testMaxDateWithOptional() {
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 4), Optional.of(LocalDate.of(1974, 4, 3))));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 3), Optional.of(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 4), Optional.of(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(LocalDate.of(1974, 4, 4), Optional.empty()));
  }

  @Test
  public void testMinWithList() {
    assertIllegalArgumentException( () -> minDate(emptyList()));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(singletonList(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(ImmutableList.of(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 5))));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(ImmutableList.of(
        LocalDate.of(1974, 4, 5),
        LocalDate.of(1974, 4, 4),
        LocalDate.of(1974, 4, 6))));
  }

  @Test
  public void testMaxWithList() {
    assertIllegalArgumentException( () -> maxDate(emptyList()));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(singletonList(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(ImmutableList.of(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 3))));
    assertEquals(LocalDate.of(1974, 4, 4), maxDate(ImmutableList.of(
        LocalDate.of(1974, 4, 2),
        LocalDate.of(1974, 4, 4),
        LocalDate.of(1974, 4, 3))));
  }

  @Test
  public void testMinDate() {
    assertEquals(LocalDate.of(1974, 4, 3), minDate(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 3)));
    assertEquals(LocalDate.of(1974, 4, 3), minDate(LocalDate.of(1974, 4, 3), LocalDate.of(1974, 4, 4)));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(LocalDate.of(1974, 4, 4), LocalDate.of(1974, 4, 4)));
  }

  @Test
  public void testMinDateWithOptional() {
    assertEquals(LocalDate.of(1974, 4, 3), minDate(LocalDate.of(1974, 4, 4), Optional.of(LocalDate.of(1974, 4, 3))));
    assertEquals(LocalDate.of(1974, 4, 3), minDate(LocalDate.of(1974, 4, 3), Optional.of(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(LocalDate.of(1974, 4, 4), Optional.of(LocalDate.of(1974, 4, 4))));
    assertEquals(LocalDate.of(1974, 4, 4), minDate(LocalDate.of(1974, 4, 4), Optional.empty()));
  }

  @Test
  public void testYyyyMmDd() {
    assertEquals("1974-04-03", yyyyMMdd(LocalDate.of(1974, 4, 3)));

    assertEquals(LocalDate.of(1974, 4, 3), dateFromYyyyMmDd("1974-04-03"));
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1974-04-99"));

    // April only has 30 days
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1974-04-31"));
    // need leading zero
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1974-04-1"));
    // no 13th month
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1974-13-01"));
    // no Feb 30th
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1974-02-30"));

    // 2000 was a leap year
    assertEquals(LocalDate.of(2000, 2, 29), dateFromYyyyMmDd("2000-02-29"));
    // 1904 was a leap year
    assertEquals(LocalDate.of(1904, 2, 29), dateFromYyyyMmDd("1904-02-29"));
    // but 1900 was NOT a leap year
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("1900-02-29"));
    // nor will 2100 be a leap year
    assertIllegalArgumentException( () -> dateFromYyyyMmDd("2100-02-29"));
  }

  @Test
  public void testNoHyphens() {
    assertEquals("191112-131415", yyMMdd_hhmmss_NoHyphens(LocalDateTime.of(2019, 11, 12, 13, 14, 15, 123_456_789)));
    assertEquals("090102-030405", yyMMdd_hhmmss_NoHyphens(LocalDateTime.of(2009, 1, 2, 3, 4, 5, 123_456_789)));

    assertEquals("19740403", yyyyMMddNoHyphens(LocalDate.of(1974, 4, 3)));
    assertEquals("740403", yyMMddNoHyphens(LocalDate.of(1974, 4, 3)));
  }

  @Test
  public void testFallsOnWeekend() {
    LocalDate thursday = LocalDate.of(1974, 4, 4);
    assertEquals(THURSDAY, thursday.getDayOfWeek());
    assertFalse(fallsOnWeekend(thursday.minusDays(6))); // Fri
    assertTrue( fallsOnWeekend(thursday.minusDays(5))); // Sat
    assertTrue( fallsOnWeekend(thursday.minusDays(4))); // Sun
    assertFalse(fallsOnWeekend(thursday.minusDays(3))); // Mon
    assertFalse(fallsOnWeekend(thursday.minusDays(2))); // Tue
    assertFalse(fallsOnWeekend(thursday.minusDays(1))); // Wed
    assertFalse(fallsOnWeekend(thursday));
    assertFalse(fallsOnWeekend(thursday.plusDays(1))); // Fri
    assertTrue( fallsOnWeekend(thursday.plusDays(2))); // Sat
    assertTrue( fallsOnWeekend(thursday.plusDays(3))); // Sun
    assertFalse(fallsOnWeekend(thursday.plusDays(4))); // Mon
  }

  @Test
  public void testFindNthDay() {
    LocalDate firstOfMonth = LocalDate.of(1974, 4, 1);
    assertEquals(MONDAY, firstOfMonth.getDayOfWeek());

    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), -999, MONDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), -1, MONDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), 0, MONDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 1),  findNthDay(YearMonth.of(1974, 4), 1, MONDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 8),  findNthDay(YearMonth.of(1974, 4), 2, MONDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 15), findNthDay(YearMonth.of(1974, 4), 3, MONDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 22), findNthDay(YearMonth.of(1974, 4), 4, MONDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 29), findNthDay(YearMonth.of(1974, 4), 5, MONDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), 6, MONDAY));

    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), -999, TUESDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), -1, TUESDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), 0, TUESDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 2),  findNthDay(YearMonth.of(1974, 4), 1, TUESDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 9),  findNthDay(YearMonth.of(1974, 4), 2, TUESDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 16), findNthDay(YearMonth.of(1974, 4), 3, TUESDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 23), findNthDay(YearMonth.of(1974, 4), 4, TUESDAY));
    assertOptionalEquals(LocalDate.of(1974, 4, 30), findNthDay(YearMonth.of(1974, 4), 5, TUESDAY));
    assertIllegalArgumentException( () -> findNthDay(YearMonth.of(1974, 4), 6, TUESDAY));

    // We know for sure there will never be a 6th Monday (or whatever) in a month.
    // However, sometimes there is a 5th day of a kind, and sometimes there isn't.
    assertOptionalEmpty(findNthDay(YearMonth.of(1974, 4), 5, WEDNESDAY)); // 5 days after the 1st Wednesday falls in the next month
    assertOptionalEmpty(findNthDay(YearMonth.of(1974, 4), 5, THURSDAY)); // same here, plus below
    assertOptionalEmpty(findNthDay(YearMonth.of(1974, 4), 5, FRIDAY));
    assertOptionalEmpty(findNthDay(YearMonth.of(1974, 4), 5, SATURDAY));
    assertOptionalEmpty(findNthDay(YearMonth.of(1974, 4), 5, SUNDAY));
  }

  @Test
  public void testGetNearestWeekday() {
    LocalDate thursday = LocalDate.of(1974, 4, 4);
    assertEquals(THURSDAY, thursday.getDayOfWeek());

    assertEquals(thursday.minusDays(6), getNearestWeekday(thursday.minusDays(6))); // Fri
    assertEquals(thursday.minusDays(6), getNearestWeekday(thursday.minusDays(5))); // Sat
    assertEquals(thursday.minusDays(3), getNearestWeekday(thursday.minusDays(4))); // Sun
    assertEquals(thursday.minusDays(3), getNearestWeekday(thursday.minusDays(3))); // Mon
    assertEquals(thursday.minusDays(2), getNearestWeekday(thursday.minusDays(2))); // Tue
    assertEquals(thursday.minusDays(1), getNearestWeekday(thursday.minusDays(1))); // Wed
    assertEquals(thursday,              getNearestWeekday(thursday)); // Thu
    assertEquals(thursday.plusDays(1),  getNearestWeekday(thursday.plusDays(1))); // Fri
    assertEquals(thursday.plusDays(1),  getNearestWeekday(thursday.plusDays(2))); // Sat
    assertEquals(thursday.plusDays(4),  getNearestWeekday(thursday.plusDays(3))); // Sun
    assertEquals(thursday.plusDays(4),  getNearestWeekday(thursday.plusDays(4))); // Mon
  }

  @Test
  public void testParseValidYear() {
    assertThrowsAnyException( () -> parseValidYear(""));
    assertThrowsAnyException( () -> parseValidYear("x"));
    assertThrowsAnyException( () -> parseValidYear("1_636"));
    assertThrowsAnyException( () -> parseValidYear("1636"));

    Year doesNotThrow;
    doesNotThrow = parseValidYear("1900");
    doesNotThrow = parseValidYear("2000");
    doesNotThrow = parseValidYear("2100");

    assertThrowsAnyException( () -> parseValidYear("2333"));
  }

  @Test
  public void testYearIsReasonable() {
    assertIllegalArgumentException( () -> checkYearIsReasonable(Year.of(1_636)));
    // The point here is that the following 3 do not throw
    checkYearIsReasonable(Year.of(1_900));
    checkYearIsReasonable(Year.of(2_000));
    checkYearIsReasonable(Year.of(2_100));
    assertIllegalArgumentException( () -> checkYearIsReasonable(Year.of(3_333)));
  }

  @Test
  public void testYearsAreConsecutive() {
    assertFalse(yearsAreConsecutive(Year.of(2014), Year.of(2012)));
    assertFalse(yearsAreConsecutive(Year.of(2014), Year.of(2013)));
    assertFalse(yearsAreConsecutive(Year.of(2014), Year.of(2014)));
    assertTrue( yearsAreConsecutive(Year.of(2014), Year.of(2015)));
    assertFalse(yearsAreConsecutive(Year.of(2014), Year.of(2016)));
    assertFalse(yearsAreConsecutive(Year.of(2014), Year.of(2017)));
  }

  @Test
  public void test_makeContiguousDateDiscreteRangeMap() {
    assertThat(
        "2 starting points",
        makeContiguousDateDiscreteRangeMap(
            rbMapOf(
                LocalDate.of(2020, 1, 20), "20 to 24",
                LocalDate.of(2020, 1, 25), "25 to 30"),
            LocalDate.of(2020, 1, 30)),
        contiguousDiscreteRangeMapEqualityMatcher(
            contiguousDiscreteRangeMap(
                ImmutableList.of(
                    Range.closed(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 24)),
                    Range.closed(LocalDate.of(2020, 1, 25), LocalDate.of(2020, 1, 30))),
                ImmutableList.of(
                    "20 to 24", "25 to 30"),
                date -> date.plusDays(1))));

    assertThat(
        "1 starting point",
        makeContiguousDateDiscreteRangeMap(
            singletonRBMap(
                LocalDate.of(2020, 1, 20), "20 to 30"),
            LocalDate.of(2020, 1, 30)),
        contiguousDiscreteRangeMapEqualityMatcher(
            singletonContiguousDiscreteRangeMap(
                Range.closed(LocalDate.of(2020, 1, 20), LocalDate.of(2020, 1, 30)),
                "20 to 30")));

    assertIllegalArgumentException( () -> makeContiguousDateDiscreteRangeMap(
        emptyRBMap(),
        LocalDate.of(2020, 1, 30)));
  }

  @Test
  public void testCannotBeConsecutiveMarketDays() {
    // If either day is not a market day, it doesn't matter what the other one is.
    LocalDate fri = LocalDate.of(2023, 6, 23);
    LocalDate sat = LocalDate.of(2023, 6, 24);
    LocalDate sun = LocalDate.of(2023, 6, 25);
    LocalDate mon = LocalDate.of(2023, 6, 26);

    assertTrue(cannotBeConsecutiveMarketDays(fri, sat));
    assertTrue(cannotBeConsecutiveMarketDays(fri, sun));
    assertTrue(cannotBeConsecutiveMarketDays(sat, sun));
    assertTrue(cannotBeConsecutiveMarketDays(sat, mon));

    assertFalse(cannotBeConsecutiveMarketDays(fri, mon));

    // All of DAY0, DAY1, etc. are market days.

    // None of these are after DAY2; invalid.
    rbSetOf(DAY0, DAY1, DAY2)      .forEach(date -> assertTrue( cannotBeConsecutiveMarketDays(DAY2, date)));
    // Less than 7 calendar days after DAY2; valid.
    rbSetOf(DAY3, DAY4, DAY5, DAY6).forEach(date -> assertFalse(cannotBeConsecutiveMarketDays(DAY2, date)));
    // 7 or more calendar days after DAY2; invalid.
    rbSetOf(DAY7, DAY8)            .forEach(date -> assertTrue( cannotBeConsecutiveMarketDays(DAY2, date)));
  }

}
