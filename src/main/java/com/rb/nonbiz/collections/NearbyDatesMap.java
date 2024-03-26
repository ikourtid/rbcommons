package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;

import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBRanges.getMinMaxClosedRange;
import static com.rb.nonbiz.text.Strings.formatMapWhereValuesPrintInstruments;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * An {@link RBMap} keyed by date, with the extra construction-time precondition that the dates aren't too far apart from each other,
 * i.e. the earliest and latest date are no more than maxCalendarRangeAllowed from each other.
 *
 * <p> 'Nearby' is not a great name; the date range can be big, as in years. But the name makes it clear that the dates
 * can't be too far from each other. It just doesn't say how far. </p>
 */
public class NearbyDatesMap<V> {

  private final RBMap<LocalDate, V> rawDateMap;
  private final int maxCalendarDaysRangeAllowed;

  private NearbyDatesMap(RBMap<LocalDate, V> rawDateMap, int maxCalendarDaysRangeAllowed) {
    this.rawDateMap = rawDateMap;
    this.maxCalendarDaysRangeAllowed = maxCalendarDaysRangeAllowed;
  }

  public static <V> NearbyDatesMap<V> nearbyDatesMap(RBMap<LocalDate, V> rawDateMap, int maxCalendarDaysRangeAllowed) {
    RBPreconditions.checkArgument(
        maxCalendarDaysRangeAllowed >= 0,
        "maxCalendarDaysRangeAllowed can't be negative: %s %s",
        rawDateMap, maxCalendarDaysRangeAllowed);
    RBPreconditions.checkArgument(
        maxCalendarDaysRangeAllowed > 0,
        "If the min and max date must be the same, there's no point in using a map: %s %s",
        rawDateMap, maxCalendarDaysRangeAllowed);
    if (!rawDateMap.isEmpty()) {
      ClosedRange<LocalDate> dateRangeInclusive = getMinMaxClosedRange(rawDateMap.keySet());
      long calendarRangeInDays = DAYS.between(dateRangeInclusive.lowerEndpoint(), dateRangeInclusive.upperEndpoint());
      RBPreconditions.checkArgument(
          calendarRangeInDays <= maxCalendarDaysRangeAllowed,
          "The dates in the map must be within %s of each other, but date range %s has %s calendar days. Map was %s",
          maxCalendarDaysRangeAllowed, dateRangeInclusive, calendarRangeInDays, rawDateMap);
    }
    return new NearbyDatesMap<V>(rawDateMap, maxCalendarDaysRangeAllowed);
  }

  public static <V> NearbyDatesMap<V> emptyNearbyDatesMap(int maxCalendarRangeAllowed) {
    return nearbyDatesMap(emptyRBMap(), maxCalendarRangeAllowed);
  }

  public RBMap<LocalDate, V> getRawDateMap() {
    return rawDateMap;
  }

  public int getMaxCalendarDaysRangeAllowed() {
    return maxCalendarDaysRangeAllowed;
  }

  public boolean isEmpty() {
    return rawDateMap.isEmpty();
  }

  public boolean containsDate(LocalDate date) {
    return rawDateMap.containsKey(date);
  }

  @Override
  public String toString() {
    return Strings.format("[NDM maxCalendarDaysRangeAllowed= %s ; %s NDM]", maxCalendarDaysRangeAllowed, rawDateMap);
  }

  // Since the generic type V is not guaranteed to implement PrintsInstruments, which will let us selectively
  // do this for those cases.
  public static <V extends PrintsInstruments> String toString(
      NearbyDatesMap<V> nearbyDatesMap, InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[NDM maxCalendarDaysRangeAllowed= %s ; %s NDM]",
        nearbyDatesMap.getMaxCalendarDaysRangeAllowed(),
        formatMapWhereValuesPrintInstruments(nearbyDatesMap.getRawDateMap(), instrumentMaster, date));
  }

}
