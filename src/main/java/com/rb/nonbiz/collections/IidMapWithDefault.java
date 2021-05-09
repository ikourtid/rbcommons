package com.rb.nonbiz.collections;

import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.rb.biz.marketdata.instrumentmaster.NullInstrumentMaster.NULL_INSTRUMENT_MASTER;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;

/**
 * Basically just an IidMap, but it also stores a default value, with the semantics that the default value applies
 * when a value for a given InstrumentId is missing.
 *
 * @see IidMap
 */
public class IidMapWithDefault<V> implements PrintsInstruments {

  private final V defaultValue;
  private final IidMap<V> rawIidMap;

  private IidMapWithDefault(V defaultValue, IidMap<V> rawIidMap) {
    this.defaultValue = defaultValue;
    this.rawIidMap = rawIidMap;
  }

  public static <V> IidMapWithDefault<V> iidMapWithDefault(V defaultValue, IidMap<V> rawIidMap) {
    return new IidMapWithDefault<>(defaultValue, rawIidMap);
  }

  public static <V> IidMapWithDefault<V> emptyIidMapWithDefault(V defaultValue) {
    return new IidMapWithDefault<>(defaultValue, emptyIidMap());
  }

  public V getDefaultValue() {
    return defaultValue;
  }

  public IidMap<V> getRawIidMap() {
    return rawIidMap;
  }

  public V getOrDefault(InstrumentId instrumentId) {
    return rawIidMap.getOptional(instrumentId).orElse(defaultValue);
  }

  public IidMapWithDefault<V> copyWithReplacedDefaultValue(V newDefaultValue) {
    return iidMapWithDefault(newDefaultValue, rawIidMap);
  }

  public <V2> IidMapWithDefault<V2> transformDefaultAndOverrides(Function<V, V2> transformer) {
    return iidMapWithDefault(
        transformer.apply(defaultValue),
        rawIidMap.transformValuesCopy(transformer));
  }

  public Stream<V> streamOfDefaultValuePlusOverrides() {
    return Stream.concat(
        Stream.of(defaultValue),
        rawIidMap.values().stream());
  }

  public int size() {
    return rawIidMap.size();
  }

  @Override
  public String toString() {
    return toString(NULL_INSTRUMENT_MASTER, UNUSED_DATE);
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Strings.format("[IMWD default= %s ; %s IMWD]",
        defaultValue, rawIidMap.toString(instrumentMaster, date));
  }

}
