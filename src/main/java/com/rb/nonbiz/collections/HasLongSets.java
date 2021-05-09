package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.types.HasLongRepresentation;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableHasLongSet.newMutableHasLongSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstAndRest;

public class HasLongSets {

  /**
   * Useful when you are iterating over 2 sets, and want to cleanly separate out the behavior when an hasLong
   * appears only on the left, only on the right, or on both sets.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   */
  public interface TwoHasLongSetsVisitor<T> {

    void visitHasLongInLeftSetOnly(T hasLongInLeftSetOnly);
    void visitHasLongInRightSetOnly(T hasLongInRightSetOnly);
    void visitHasLongInBothSets(T hasLongInBothSets);

  }

  public static <T extends HasLongRepresentation> Iterator<Long> toLongIterator(Iterator<T> iter) {
    return Iterators.transform(iter, v -> v.asLong());
  }

  @SafeVarargs
  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableUnionOfHasLongSets(
      HasLongSet<T> first,
      HasLongSet<T>...rest) {
    int sizeHint = first.size() + Arrays.stream(rest).mapToInt(v -> v.size()).sum();
    return mutableUnionOfHasLongSetsHelper(
        sizeHint,
        concatenateFirstAndRest(first, rest).map(v -> v.iterator()));
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableUnionOfHasLongSets(
      Set<T> set1,
      HasLongSet<T> set2) {
    return mutableUnionOfHasLongSetsHelper(set1.size() + set2.size(), Stream.of(set1.iterator(), set2.iterator()));
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableUnionOfHasLongSets(
      HasLongSet<T> set1,
      Set<T> set2) {
    return mutableUnionOfHasLongSetsHelper(set1.size() + set2.size(), Stream.of(set1.iterator(), set2.iterator()));
  }

  private static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableUnionOfHasLongSetsHelper(
      int sizeHint,
      Stream<Iterator<T>> iterators) { // this is just a hint for the size, not the actual size.
    MutableHasLongSet<T> mutableSet = newMutableHasLongSetWithExpectedSize(sizeHint);
    iterators.forEach(iterator ->
        iterator.forEachRemaining(hasLong -> mutableSet.add(hasLong)));
    return mutableSet;
  }

  /**
   * Returns empty if the items in the list are not unique, otherwise it returns them as a HasLongSet.
   */
  protected static <T extends HasLongRepresentation> Optional<MutableHasLongSet<T>> toMutableHasLongSetIfUnique(
      List<T> items) {
    return toMutableHasLongSetIfUnique(items.iterator(), items.size());
  }

  /**
   * Returns empty if the items are not unique, otherwise it returns them as a HasLongSet.
   */
  protected static <T extends HasLongRepresentation> Optional<MutableHasLongSet<T>> toMutableHasLongSetIfUnique(
      Iterator<T> items,
      int sizeHint) {
    MutableHasLongSet<T> mutableSet = newMutableHasLongSetWithExpectedSize(sizeHint);
    while (items.hasNext()) {
      T item = items.next();
      if (mutableSet.contains(item)) {
        return Optional.empty();
      }
      mutableSet.add(item);
    }
    return Optional.of(mutableSet);
  }

  /**
   * If you have N sets where N is big, this should be more efficient than doing N-1 set unions of pairs of sets.
   */
  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableUnionOfHasLongSets(
      Iterator<? extends HasLongSet<T>> sets,
      int sizeHint) {
    MutableHasLongSet<T> mutableSet = newMutableHasLongSetWithExpectedSize(sizeHint);
    while (sets.hasNext()) {
      HasLongSet<T> thisSet = sets.next();
      thisSet.forEach(hasLong -> mutableSet.add(hasLong));
    }
    return mutableSet;
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableIntersectionOfHasLongSets(
      HasLongSet<T> set1,
      HasLongSet<T> set2) {
    MutableHasLongSet<T> mutableSet = newMutableHasLongSetWithExpectedSize(set1.size() + set2.size());
    set1.forEach(hasLong1 -> {
      if (set2.contains(hasLong1)) {
        mutableSet.add(hasLong1);
      }
    });
    return mutableSet;
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableIntersectionOfHasLongSets(
      Set<T> set1,
      HasLongSet<T> set2) {
    return mutableIntersectionOfHasLongSets(set2, set1);
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableIntersectionOfHasLongSets(
      HasLongSet<T> set1,
      Set<T> set2) {
    MutableHasLongSet<T> mutableSet = newMutableHasLongSetWithExpectedSize(set1.size() + set2.size());
    set1.forEach(hasLong1 -> {
      if (set2.contains(hasLong1)) {
        mutableSet.add(hasLong1);
      }
    });
    return mutableSet;
  }

  protected static <T extends HasLongRepresentation> boolean noSharedHasLongs(HasLongSet<T> set1, HasLongSet<T> set2) {
    TLongIterator iter1 = set1.rawTroveIterator();
    while (iter1.hasNext()) {
      long long1 = iter1.next();
      if (set2.getRawSetUnsafe().contains(long1)) {
        return false;
      }
    }
    // set2 may have extra items beyond set1, but if any were shared with set1, then we would have caught it above.
    return true;
  }

  protected static <T extends HasLongRepresentation> boolean noSharedHasLongs(Set<T> set1, HasLongSet<T> set2) {
    return noSharedHasLongs(set2, set1);
  }

  protected static <T extends HasLongRepresentation> boolean noSharedHasLongs(HasLongSet<T> set1, Set<T> set2) {
    TLongIterator iter1 = set1.rawTroveIterator();
    return set2
        .stream()
        .noneMatch(item -> set1.contains(item));
  }

  protected static <T extends HasLongRepresentation> boolean noSharedHasLongsInCollection(
      Collection<? extends HasLongSet<T>> hasLongSets) {
    int totalHasLongs = hasLongSets
        .stream()
        .mapToInt(set -> set.size())
        .sum();
    TLongSet allLongs = new TLongHashSet(totalHasLongs);
    // If any hasLongs are shared, they will be collapsed into a single one in allHasLongs, so its size will be smaller.
    for (HasLongSet<T> set : hasLongSets) {
      TLongIterator iter = set.rawTroveIterator();
      while (iter.hasNext()) {
        long asLong = iter.next();
        boolean wasAbsent = allLongs.add(asLong);
        if (!wasAbsent) {
          return false;
        }
      }
    }
    return true;
  }

  protected static <T extends HasLongRepresentation> boolean noSharedHasLongs(HasLongSet<T>... hasLongSets) {
    return noSharedHasLongsInCollection(Arrays.asList(hasLongSets));
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableDifference(
      HasLongSet<T> set1, HasLongSet<T> set2) {
    MutableHasLongSet<T> mutableHasLongSet = newMutableHasLongSetWithExpectedSize(set1.size()); // size hint is an upper bound
    set1.forEach(item1 -> {
      if (!set2.contains(item1)) {
        mutableHasLongSet.add(item1);
      }
    });
    return mutableHasLongSet;
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableDifference(
      Set<T> set1, HasLongSet<T> set2) {
    MutableHasLongSet<T> mutableHasLongSet = newMutableHasLongSetWithExpectedSize(set1.size()); // size hint is an upper bound
    set1.forEach(item1 -> {
      if (!set2.contains(item1)) {
        mutableHasLongSet.add(item1);
      }
    });
    return mutableHasLongSet;
  }

  protected static <T extends HasLongRepresentation> MutableHasLongSet<T> mutableDifference(
      HasLongSet<T> set1, Set<T> set2) {
    MutableHasLongSet<T> mutableHasLongSet = newMutableHasLongSetWithExpectedSize(set1.size()); // size hint is an upper bound
    set1.forEach(item1 -> {
      if (!set2.contains(item1)) {
        mutableHasLongSet.add(item1);
      }
    });
    return mutableHasLongSet;
  }

  protected static <T extends HasLongRepresentation> boolean hasLongSetIsSubsetOfHasLongSet(
      HasLongSet<T> subset, HasLongSet<T> superset) {
    TLongIterator subsetIter = subset.rawTroveIterator();
    while (subsetIter.hasNext()) {
      long asLong = subsetIter.next();
      if (!superset.getRawSetUnsafe().contains(asLong)) {
        // this can't be a subset if it contains something that's not in the superset
        return false;
      }
    }
    return true;
  }

  // Since Set<T> does not index its items by long value, we need to do a little trick here.
  // We count the number of items in superset that appear in subset. If fewer than the subset size,
  // then that's not really a subset.
  protected static <T extends HasLongRepresentation> boolean hasLongSetIsSubsetOf(
      HasLongSet<T> subset, Set<T> superset) {
    long appearingInSubset = superset
        .stream()
        .filter(item -> subset.contains(item))
        .count();
    return appearingInSubset == subset.size();
  }

  protected static <T extends HasLongRepresentation> boolean isSubsetOfHasLongSet(
      Set<T> subset, HasLongSet<T> superset) {
    for (T next : subset) {
      if (!superset.getRawSetUnsafe().contains(next.asLong())) {
        return false;
      }
    }
    return true;
  }

  public static <T extends HasLongRepresentation> boolean equalAsHasLongSets(List<T> list, HasLongSet<T> set) {
    return list.size() == set.size()
        && list.stream().allMatch(listHasLong -> set.contains(listHasLong));
  }

  /**
   * When you have 2 sets, this lets you perform 3 different actions for hasLongs that are only in the left set,
   * only in the right, or in both sets.
   *
   * It lets you specify a bit more cleanly (via the TwoHasLongSetsVisitor) what to do in those 3 different cases.
   */
  public static <T extends HasLongRepresentation> void visitHasLongsOfTwoSets(
      HasLongSet<T> leftSet, HasLongSet<T> rightSet, TwoHasLongSetsVisitor<T> visitor) {
    for (T key1 : leftSet) {
      if (rightSet.contains(key1)) {
        visitor.visitHasLongInBothSets(key1);
      } else {
        visitor.visitHasLongInLeftSetOnly(key1);
      }
    }
    for (T key2 : rightSet) {
      if (!leftSet.contains(key2)) {
        visitor.visitHasLongInRightSetOnly(key2);
      }
    }
  }
  
}
