package com.rb.biz.guice;

import com.rb.biz.guice.RBClockModifier.RBClockModifierToken;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Tells us what time it is.
 *
 * <p> We wrap all calls to 'what time is it?' to utilize a Guice-injected instance of this class.
 * This way, production instances can just use 'wall clock' time (real), but backtests can instead use
 * whatever time it happens to be during some multi-year simulation that we run. </p>
 */
public interface RBClock {

  ZoneId EAST_COAST_ZONE_ID = ZoneId.of("America/New_York");

  LocalDateTime now();

  default LocalDate today() {
    return now().toLocalDate();
  }


  default ZonedDateTime nowOnEastCoast() {
    // All production machines should be on East Coast time, because that's kind of standard,
    // as NYSE is on the East Coast, and all US exchanges follow by and large the same trading hours (9:30 - 4).
    // This is not a requirement, of course.
    return now().atZone(EAST_COAST_ZONE_ID);
  }

  /**
   * Provides a way to modify the current time.
   *
   * <p> Ideally, this would have been package-private, so that users of this method (which will be outside this package)
   * cannot call it. It is only meant to be called by RBClockModifier. However, there's no such thing as a
   * package-private interface method. Instead, we went for the following trick: the only way a
   * {@link RBClockModifierToken} can be constructed is from inside {@link RBClockModifier}. So no other class can
   * call this method. The only way to bypass this in other (non-{@link RBClockModifier}) calling code is to pass null,
   * since there's no way to instantiate a {@link RBClockModifierToken} elsewhere, plus it's a final class so
   * we can't derive from it and create an object from a subclass. To avoid that, the implementers of {@link RBClock}
   * must do a check that the {@link RBClockModifierToken} passed in is not null. </p>
   *
   * <p> For C++ developers, this is akin to a 'friend method'. </p>
   *
   * <p> This is a bit overly safe and convoluted, but it avoids the catastrophic effect of
   * some calling code changing the {@link RBClock} time by accident. Now, all modifications must go through
   * {@link RBClockModifier}. </p>
   */
  void overwriteCurrentTime(RBClockModifierToken rbClockModifierToken, LocalDateTime newTime);

}
