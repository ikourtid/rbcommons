package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.util.RBOrderingPreconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.MutableIidSet.newMutableIidSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;

/**
 * Constructors for IidSets based on lists, Collections, iterators.
 */
public class IidSetSimpleConstructors {

  private static final boolean ALLOW_DUPLICATES = true;
  private static final boolean DISALLOW_DUPLICATES = false;

  private static final boolean IN_INCREASING_INSTRUMENT_ID = true;
  private static final boolean NOT_IN_INCREASING_INSTRUMENT_ID = false;

  /**
   * Use this when you already had the instruments sorted previously.
   */
  public static IidSet newIidSetInOrder(List<InstrumentId> sortedInstruments) {
    RBOrderingPreconditions.checkIncreasing(sortedInstruments);
    return newIidSetHelper(IN_INCREASING_INSTRUMENT_ID, DISALLOW_DUPLICATES, sortedInstruments);
  }

  public static IidSet newIidSet(RBSet<InstrumentId> instrumentIds) {
    return newIidSet(instrumentIds.asSet());
  }

  public static IidSet newIidSet(Collection<InstrumentId> instrumentIds) {
    return newIidSetHelper(NOT_IN_INCREASING_INSTRUMENT_ID, DISALLOW_DUPLICATES, instrumentIds);
  }

  public static IidSet newIidSet(Iterator<InstrumentId> instrumentIds, int sizeHint) {
    return newIidSetHelper(NOT_IN_INCREASING_INSTRUMENT_ID, DISALLOW_DUPLICATES, instrumentIds, sizeHint);
  }

  public static IidSet newIidSet(InstrumentId[] instrumentIds) {
    return newIidSet(Iterators.forArray(instrumentIds), instrumentIds.length);
  }

  public static IidSet newIidSet(InstrumentId first, InstrumentId ... rest) {
    return newIidSet(concatenateFirstAndRest(first, rest).iterator(), 1 + rest.length);
  }

  public static IidSet newIidSetFromPossibleDuplicates(Collection<InstrumentId> instrumentIds) {
    return newIidSetHelper(NOT_IN_INCREASING_INSTRUMENT_ID, ALLOW_DUPLICATES, instrumentIds);
  }

  public static IidSet newIidSetFromPossibleDuplicates(Iterator<InstrumentId> instrumentIds, int sizeHint) {
    return newIidSetHelper(NOT_IN_INCREASING_INSTRUMENT_ID, ALLOW_DUPLICATES, instrumentIds, sizeHint);
  }

  public static IidSet newIidSetFromPossibleDuplicates(InstrumentId...instrumentIds) {
    return newIidSetHelper(ALLOW_DUPLICATES, instrumentIds);
  }

  private static IidSet newIidSetHelper(
      boolean inIncreasingInstrumentIdOrder,
      boolean allowDuplicates,
      Collection<InstrumentId> instrumentIds) {
    return newIidSetHelper(
        inIncreasingInstrumentIdOrder, allowDuplicates, instrumentIds.iterator(), instrumentIds.size());
  }

  private static IidSet newIidSetHelper(
      boolean inIncreasingInstrumentIdOrder,
      boolean allowDuplicates,
      Iterator<InstrumentId> instrumentIds,
      int sizeHint) {
    MutableIidSet mutableSet = newMutableIidSetWithExpectedSize(sizeHint);
    // This is a bit clunky with the various if statements, but I'm trying to be efficient here,
    // and only instantiate & populate the list of sorted instrument IDs if needed.
    if (inIncreasingInstrumentIdOrder) {
      List<InstrumentId> list = newArrayListWithExpectedSize(sizeHint);
      if (allowDuplicates) {
        instrumentIds.forEachRemaining(instrumentId -> {
          list.add(instrumentId);
          mutableSet.add(instrumentId);
        });
      } else {
        instrumentIds.forEachRemaining(instrumentId -> {
          list.add(instrumentId);
          mutableSet.addAssumingAbsent(instrumentId);
        });
      }
      return new IidSet(mutableSet.getRawSet(), list);

    } else {
      if (allowDuplicates) {
        instrumentIds.forEachRemaining(instrumentId -> mutableSet.add(instrumentId));
      } else {
        instrumentIds.forEachRemaining(instrumentId -> mutableSet.addAssumingAbsent(instrumentId));
      }
      return newIidSet(mutableSet);
    }
  }

  private static IidSet newIidSetHelper(boolean allowDuplicates, InstrumentId...instrumentIds) {
    if (instrumentIds.length == 0) {
      return emptyIidSet(); // small performance optimization
    }
    MutableIidSet mutableSet = newMutableIidSetWithExpectedSize(instrumentIds.length);
    if (allowDuplicates) {
      for (InstrumentId instrumentId : instrumentIds) {
        mutableSet.add(instrumentId);
      }
    } else {
      for (InstrumentId instrumentId : instrumentIds) {
        mutableSet.addAssumingAbsent(instrumentId);
      }
    }
    return newIidSet(mutableSet);
  }

  /**
   * Unlike ImmutableSet#of, there is no 0-pair override for iidSetOf.
   * This is to force you to use emptyIidSet, which is more explicit and makes reading tests easier.
   * Likewise for singletonIidSet().
   */
  public static IidSet emptyIidSet() {
    // in theory this should be 0, but the underlying 3rd party classes break if we do that.
    return newIidSet(newMutableIidSetWithExpectedSize(1));
  }

  /**
   * Unlike ImmutableSet#of, there is no single-item override for iidSetOf.
   * This is to force you to use singletoniidSet, which is more explicit and makes reading tests easier.
   * Likewise for emptyiidSet().
   */
  public static IidSet singletonIidSet(InstrumentId item) {
    MutableIidSet mutableSet = newMutableIidSetWithExpectedSize(1);
    mutableSet.add(item);
    return newIidSet(mutableSet);
  }

  public static IidSet iidSetOf(InstrumentId first, InstrumentId second, InstrumentId ... rest) {
    MutableHasLongSet<InstrumentId > set = newMutableIidSetWithExpectedSize(rest.length + 2); // + 2 for first and rest
    set.add(first); // first one can't already exist in the set that starts out empty
    set.addAssumingAbsent(second);
    for (InstrumentId item : rest) {
      set.addAssumingAbsent(item);
    }
    return newIidSet(set);
  }

  public static IidSet newIidSet(MutableHasLongSet<InstrumentId> mutableSet) {
    return new IidSet(mutableSet.getRawSet());
  }

}
