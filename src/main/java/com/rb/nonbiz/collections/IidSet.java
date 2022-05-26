package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.PrintsInstruments;
import com.rb.nonbiz.text.Strings;
import gnu.trove.set.hash.TLongHashSet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.newIidMap;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;
import static com.rb.nonbiz.collections.MutableIidMap.newMutableIidMapWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMapWithExpectedSize;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;

/**
 * A set of InstrumentId that can be indexed by InstrumentId.
 * It uses the instrument-id-specific optimized maps (using GNU Trove)
 *
 * @see HasLongSet
 * @see HasInstrumentIdSet
 * @see RBSet
 */
public class IidSet extends HasLongSet<InstrumentId> implements PrintsInstruments {

  protected IidSet(TLongHashSet rawSet) {
    super(rawSet);
  }

  protected IidSet(TLongHashSet rawSet, List<InstrumentId> sortedInstruments) {
    super(rawSet, sortedInstruments);
  }

  @Override
  protected InstrumentId instantiateItem(long asLong) {
    return instrumentId(asLong);
  }

  /**
   * This converts this set into an IidMap with the same instrument ID keys,
   * and whose corresponding values are some function of the key.
   *
   * See also #orderedToIidMap.
   */
  public <V> IidMap<V> toIidMap(Function<InstrumentId, V> valueGenerator) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(size());
    iterator().forEachRemaining(instrumentId ->
        mutableMap.put(instrumentId, valueGenerator.apply(instrumentId)));
    return newIidMap(mutableMap);
  }

  /**
   * In some cases, we still need to convert to an old-style, non-specialized RBMap.
   * Examples (Sep 2018) are cases where the code is more general and uses {@code <K extends Investable>} instead of
   * just an InstrumentId, such as {@code DoubleMap<K extends Investable>}.
   */
  public <V> RBMap<InstrumentId, V> toRBMap(Function<InstrumentId, V> valueGenerator) {
    MutableRBMap<InstrumentId, V> mutableMap = newMutableRBMapWithExpectedSize(size());
    iterator().forEachRemaining(instrumentId ->
        mutableMap.put(instrumentId, valueGenerator.apply(instrumentId)));
    return newRBMap(mutableMap);
  }

  /**
   * Converts this set into an IidMap whose instrument ids are a subset of the instrument ids in this set
   * and whose corresponding values are some function of the instrument id.
   * The valueGenerator passed must return Optional.empty() if an instrument id is not supposed to go into
   * a resulting map, and Optional.of(V) if it is.
   *
   * @see #orderedToIidMap
   */
  public <V> IidMap<V> toIidMapWithFilteredKeys(Function<InstrumentId, Optional<V>> optionalValueGenerator) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(size());
    forEach(instrumentId ->
        optionalValueGenerator.apply(instrumentId)
            .ifPresent(v -> mutableMap.put(instrumentId, v)));
    return newIidMap(mutableMap);
  }

  /**
   * This converts this set into an IidMap with the same instrument ids,
   * and whose corresponding values are some function of the instrument id.
   * However, the ordering that the valueGenerator is run on the instrument ids is fixed.
   *
   * This matters when valueGenerator has a side effect, such as increasing some internal counter.
   *
   * @see #toIidMap
   */
  public <V> IidMap<V> orderedToIidMap(Function<InstrumentId, V> valueGenerator) {
    MutableIidMap<V> mutableMap = newMutableIidMapWithExpectedSize(size());
    sortedStream().forEach(instrumentId ->
        mutableMap.put(instrumentId, valueGenerator.apply(instrumentId)));
    return newIidMap(mutableMap);
  }

  public IidSet filter(Predicate<InstrumentId> predicate) {
    return newIidSet(
        stream()
            .filter(predicate)
            .collect(Collectors.toSet()));
  }

  public void ifNonEmpty(Consumer<IidSet> iidSetConsumer) {
    if (isEmpty()) {
      return;
    }
    iidSetConsumer.accept(this);
  }

  @Override
  public String toString() {
    return isEmpty()
        ? "[IIS]"
        : Strings.format("[IIS %s IIS]",
        Joiner.on(" ; ").join(sortedStream().iterator()));
  }

  @Override
  public String toString(InstrumentMaster instrumentMaster, LocalDate date) {
    return isEmpty()
        ? "[IIS]"
        : Strings.format("[IIS %s IIS]", toSimpleString(instrumentMaster, date));
  }

  public String toSimpleString(InstrumentMaster instrumentMaster, LocalDate date) {
    return Joiner.on(" ; ").join(
        Iterators.transform(sortedStream().iterator(), instrumentId ->
            instrumentMaster.getLatestValidSymbolOrInstrumentIdAsSymbol(instrumentId, date)));
  }

  public List<InstrumentId> asSortedList() {
    return stream().sorted().collect(Collectors.toList());
  }

  @Override
  public int hashCode() {
    return getRawSetUnsafe().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IidSet iidSet = (IidSet) o;

    return getRawSetUnsafe().equals(iidSet.getRawSetUnsafe());
  }

}
