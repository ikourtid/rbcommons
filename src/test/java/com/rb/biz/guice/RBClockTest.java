package com.rb.biz.guice;

import com.rb.biz.guice.RBClockModifier.RBClockModifierToken;
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
      public void overwriteCurrentTime(RBClockModifierToken rbClockModifierToken, LocalDateTime newTime) {
        throw new IllegalArgumentException("You should not be calling this when you use RBClockTest#rbClockWithDateOnly");
      }
    };
  }

}
