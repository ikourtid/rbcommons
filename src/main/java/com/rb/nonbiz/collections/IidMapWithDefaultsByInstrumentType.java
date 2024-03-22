package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.biz.types.asset.InstrumentType;
import com.rb.biz.types.asset.InstrumentTypeMap;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.biz.types.asset.InstrumentTypeMap.instrumentTypeMapWithSharedDefaults;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * Similar to an {@link IidMap}, but it also stores a default value per instrument type,
 * with the semantics that the default value of the respective instrument type applies
 * when a value for a given {@link InstrumentId} is missing.
 *
 * <p> This means that the {@link InstrumentType} must be known when attempting to retrieve a value from this map,
 * so that the corresponding default value can be used if the {@link InstrumentId} key is missing. </p>
 *
 * @see IidMap
 * @see InstrumentTypeMap
 */
public class IidMapWithDefaultsByInstrumentType<V> implements PrintsInstruments {

  private final InstrumentTypeMap<V> defaultValues;
  private final IidMap<V> rawIidMap;

  private IidMapWithDefaultsByInstrumentType(InstrumentTypeMap<V> defaultValues, IidMap<V> rawIidMap) {
    this.defaultValues = defaultValues;
    this.rawIidMap = rawIidMap;
  }

  public static <V> IidMapWithDefaultsByInstrumentType<V> iidMapWithDefaultsByInstrumentType(
      InstrumentTypeMap<V> defaultValues, IidMap<V> rawIidMap) {
    return new IidMapWithDefaultsByInstrumentType<>(defaultValues, rawIidMap);
  }

  public static <V> IidMapWithDefaultsByInstrumentType<V> iidMapWithOnlyDefaultsByInstrumentType(
      InstrumentTypeMap<V> defaultValues) {
    return new IidMapWithDefaultsByInstrumentType<>(defaultValues, emptyIidMap());
  }

  public static <V> IidMapWithDefaultsByInstrumentType<V> emptyIidMapByInstrumentTypeWithSharedDefaults(V defaultValue) {
    return new IidMapWithDefaultsByInstrumentType<>(instrumentTypeMapWithSharedDefaults(defaultValue), emptyIidMap());
  }

  public InstrumentTypeMap<V> getDefaultValues() {
    return defaultValues;
  }

  public IidMap<V> getRawIidMap() {
    return rawIidMap;
  }

  public int getMapSize() {
    return rawIidMap.size();
  }

  public V getOrDefault(InstrumentId instrumentId, InstrumentType instrumentType) {
    return rawIidMap.getOptional(instrumentId).orElse(defaultValues.get(instrumentType));
  }

  public boolean allMatch(Predicate<V> predicate) {
    return Stream.concat(
        defaultValues.stream(),
        rawIidMap.values().stream())
        .allMatch(predicate);
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IMWD default= %s ; %s IMWD]",
        defaultValues, rawIidMap.toString(instrumentMaster, date));
  }

}
