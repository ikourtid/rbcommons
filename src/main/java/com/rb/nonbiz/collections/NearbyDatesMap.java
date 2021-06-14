package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBRanges.getMinMaxClosedRange;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * A RBMap keyed by date, with the extra precondition that the dates aren't too far apart from each other,
 * i.e. the earliest and latest date are no more than maxCalendarRangeAllowed from each other.
 *
 * 'Nearby' is not a great name; the date range can be big, as in years. But the name makes it clear that the dates
 * can't be too far from each other. It just doesn't say how far.
 */
public class NearbyDatesMap<V> {

  private final RBMap<LocalDate, V> rawDateMap;
  private final int maxCalendarRangeAllowed;

  private NearbyDatesMap(RBMap<LocalDate, V> rawDateMap, int maxCalendarRangeAllowed) {
    this.rawDateMap = rawDateMap;
    this.maxCalendarRangeAllowed = maxCalendarRangeAllowed;
  }

  public static <V> NearbyDatesMap<V> nearbyDatesMap(RBMap<LocalDate, V> rawDateMap, int maxCalendarRangeAllowed) {
    RBPreconditions.checkArgument(
        maxCalendarRangeAllowed >= 0,
        "maxCalendarRangeAllowed can't be negative: %s %s",
        rawDateMap, maxCalendarRangeAllowed);
    RBPreconditions.checkArgument(
        maxCalendarRangeAllowed > 0,
        "If the min and max date must be the same, there's no point in using a map: %s %s",
        rawDateMap, maxCalendarRangeAllowed);
    if (!rawDateMap.isEmpty()) {
      ClosedRange<LocalDate> dateRangeInclusive = getMinMaxClosedRange(rawDateMap.keySet());
      long calendarRangeInDays = DAYS.between(dateRangeInclusive.lowerEndpoint(), dateRangeInclusive.upperEndpoint());
      RBPreconditions.checkArgument(
          calendarRangeInDays <= maxCalendarRangeAllowed,
          "The dates in the map must be within %s of each other, but date range %s has %s calendar days. Map was %s",
          maxCalendarRangeAllowed, dateRangeInclusive, calendarRangeInDays, rawDateMap);
    }
    return new NearbyDatesMap<V>(rawDateMap, maxCalendarRangeAllowed);
  }

  public static <V extends Comparable<? super V>> NearbyDatesMap<V> emptyNearbyDatesMap(int maxCalendarRangeAllowed) {
    return nearbyDatesMap(emptyRBMap(), maxCalendarRangeAllowed);
  }

  public RBMap<LocalDate, V> getRawDateMap() {
    return rawDateMap;
  }

  public int getMaxCalendarRangeAllowed() {
    return maxCalendarRangeAllowed;
  }

  @Override
  public String toString() {
    return Strings.format("[NDM maxCalendarRangeAllowed= %s ; %s NDM]", maxCalendarRangeAllowed, rawDateMap);
  }

}
