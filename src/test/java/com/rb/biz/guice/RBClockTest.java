package com.rb.biz.guice;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static junit.framework.TestCase.assertEquals;

public class RBClockTest {

  /**
   * Use this if you want an RBClock to have its today() method return a specific date, but when you also want
   * to confirm that nobody is calling the other methods such as #now(). You could accomplish this with mocks,
   * but this is easier to use in tests.
   */
  public static RBClock rbClockWithDateOnly(LocalDate today) {
    return new RBClock() {
      @Override
      public LocalDate today() {
        return today;
      }

      @Override
      public LocalDateTime now() {
        throw new IllegalArgumentException("You should not be calling this when you use RBClockTest#rbClockWithDateOnly");
      }

      @Override
      public ZonedDateTime nowOnEastCoast() {
        throw new IllegalArgumentException("You should not be calling this when you use RBClockTest#rbClockWithDateOnly");
      }

      @Override
      public void overwriteCurrentTime(LocalDateTime newTime) {
        throw new IllegalArgumentException("You should not be calling this when you use RBClockTest#rbClockWithDateOnly");
      }
    };
  }

//  /**
//   * Use this if you want an RBClock to have its today() method return a specific date, but when you also want
//   * to confirm that nobody is calling the other methods such as #now(). You could accomplish this with mocks,
//   * but this is easier to use in tests.
//   */
//  public static RBClock rbClockWithTimeOnly(LocalDateTime now) {
//    return new RBClock(now) {
//      @Override
//      public LocalDate today() {
//        throw new IllegalArgumentException("You should not be calling this when you use RBClockTest#rbClockWithTimeOnly");
//      }
//    };
//  }
//
//  @Test
//  public void testGetterForToday() {
//    RBClock rbClock = new RBClock(LocalDateTime.of(2014, 4, 4, 4, 4, 4));
//    assertEquals(LocalDate.of(2014, 4, 4), rbClock.today());
//  }

}
