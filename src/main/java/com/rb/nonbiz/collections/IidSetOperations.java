package com.rb.nonbiz.collections;

import com.rb.biz.types.asset.InstrumentId;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.HasLongSets.hasLongSetIsSubsetOf;
import static com.rb.nonbiz.collections.HasLongSets.hasLongSetIsSubsetOfHasLongSet;
import static com.rb.nonbiz.collections.HasLongSets.isSubsetOfHasLongSet;
import static com.rb.nonbiz.collections.HasLongSets.mutableDifference;
import static com.rb.nonbiz.collections.HasLongSets.mutableIntersectionOfHasLongSets;
import static com.rb.nonbiz.collections.HasLongSets.mutableUnionOfHasLongSets;
import static com.rb.nonbiz.collections.IidSetSimpleConstructors.newIidSet;

/**
 * {@link IidSet} constructors based on unions / intersections / differences of {@link IidSet}s and / or
 * plain Java {@link Set}s.
 */
public class IidSetOperations {

  public static IidSet unionOfIidSets(IidSet first, IidSet...rest) {
    return newIidSet(mutableUnionOfHasLongSets(first, rest));
  }

  public static IidSet unionOfIidSets(Set<InstrumentId> set1, IidSet set2) {
    return newIidSet(mutableUnionOfHasLongSets(set1, set2));
  }

  public static IidSet unionOfIidSets(IidSet set1, Set<InstrumentId> set2) {
    return newIidSet(mutableUnionOfHasLongSets(set1, set2));
  }

  /**
   * If you have N sets where N is big, this should be more efficient than doing N-1 set unions of pairs of sets.
   */
  public static IidSet unionOfIidSets(Collection<IidSet> sets) {
    int sizeHint = sets
        .stream()
        .mapToInt(set -> set.size())
        .sum();
    return newIidSet(mutableUnionOfHasLongSets(sets.iterator(), sizeHint));
  }

  /**
   * If you have N sets where N is big, this should be more efficient than doing N-1 set unions of pairs of sets.
   */
  public static IidSet unionOfIidSets(Iterator<IidSet> setsIterator, int sizeHint) {
    return newIidSet(mutableUnionOfHasLongSets(setsIterator, sizeHint));
  }

  public static IidSet intersectionOfIidSets(IidSet set1, IidSet set2) {
    return newIidSet(mutableIntersectionOfHasLongSets(set1, set2));
  }

  public static IidSet intersectionOfIidSets(Set<InstrumentId> set1, IidSet set2) {
    return newIidSet(mutableIntersectionOfHasLongSets(set1, set2));
  }

  public static IidSet intersectionOfIidSets(IidSet set1, Set<InstrumentId> set2) {
    return newIidSet(mutableIntersectionOfHasLongSets(set1, set2));
  }

  // find the instruments that are in set 1 but not in set2
  public static IidSet differenceOfIidSets(IidSet set1, IidSet set2) {
    return newIidSet(mutableDifference(set1, set2));
  }

  public static IidSet differenceOfIidSets(Set<InstrumentId> set1, IidSet set2) {
    return newIidSet(mutableDifference(set1, set2));
  }

  public static IidSet differenceOfIidSets(IidSet set1, Set<InstrumentId> set2) {
    return newIidSet(mutableDifference(set1, set2));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  public static IidSet setUnionOfFirstAndRestInstrumentIds(InstrumentId first, InstrumentId...rest) {
    return newIidSet(RBStreams.concatenateFirstAndRest(first, rest)
        .collect(Collectors.toSet()));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  public static IidSet setUnionOfFirstSecondAndRestInstrumentIds(
      InstrumentId first, InstrumentId second, InstrumentId...rest) {
    return newIidSet(RBStreams.concatenateFirstSecondAndRest(first, second, rest)
        .collect(Collectors.toSet()));
  }

  /**
   * Returns if the first argument is a subset of the second argument.
   */
  public static boolean isSubsetOf(IidSet subset, IidSet superset) {
    return hasLongSetIsSubsetOfHasLongSet(subset, superset);
  }

  /**
   * Returns if the first argument is a subset of the second argument.
   */
  public static boolean isSubsetOf(Set<InstrumentId> subset, IidSet superset) {
    return isSubsetOfHasLongSet(subset, superset);
  }

  /**
   * Returns if the first argument is a subset of the second argument.
   */
  public static boolean isSubsetOf(IidSet subset, Set<InstrumentId> superset) {
    return hasLongSetIsSubsetOf(subset, superset);
  }

  /**
   * Returns if the first argument is a subset of the second argument.
   *
   * <p> This is a bit more performant, for the cases where we don't want to actually build an IidSet. </p>
   */
  public static boolean isSubsetOf(Stream<InstrumentId> subset, IidSet superset) {
    return subset.allMatch(v -> superset.contains(v));
  }

  /**
   * Returns if the first argument is a subset of the second argument.
   *
   * <p> This is a bit more performant, for the cases where we don't want to actually build an IidSet. </p>
   *
   * <p> However, you need to ensure that the stream does not have duplicate items in it. But by the time
   * you call .sorted() and .distinct(), maybe you lose all the performance benefits anyway. It depends on
   * your use case. </p>
   */
  public static boolean isSubsetOf(IidSet subset, Stream<InstrumentId> superset) {
    long subsetItemsInSuperset = superset.filter(v -> subset.contains(v)).count();
    return subset.size() == subsetItemsInSuperset;
  }

}
