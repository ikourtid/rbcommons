package com.rb.biz.guice;

import com.google.inject.Singleton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Tells us what time it is.
 * We wrap all calls to 'what time is it?' so that we can replace 'wall clock' time (real) with
 * whatever time it happens to be during some multi-year simulation that we run.
 */
public interface RBClock {

  ZoneId EAST_COAST_ZONE_ID = ZoneId.of("America/New_York");

  LocalDateTime now();

  default LocalDate today() {
    return now().toLocalDate();
  }


  default ZonedDateTime nowOnEastCoast() {
    // All production machines should be on East Coast time, because that's kind of standard,
    // as NYSE is on the East Coast, and all exchanges follow by and large the same trading hours (9:30 - 4).
    return now().atZone(EAST_COAST_ZONE_ID);
  }

  /**
   * This is intentionally package-private, so that users of this method (which will be outside this package)
   * cannot call it. It is only meant to be called by RBClockModifier.
   * @see RBClockModifier
   */
  void overwriteCurrentTime(LocalDateTime newTime);

}
