package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSetWithExpectedSize;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.collections.RBStreams.concatenateFirstSecondAndRest;

/**
 * Static methods pertaining to RBSet objects.
 * @see RBSet
 */
public class RBSets {

  /**
   * Useful when you are iterating over 2 sets, and want to cleanly separate out the behavior when an item
   * appears only on the left, only on the right, or on both sets.
   *
   * You could always just do this with a bunch of ifs, but this is cleaner and more functional-looking.
   */
  public interface TwoRBSetsVisitor<T> {

    void visitItemInLeftSetOnly(T itemInLeftSetOnly);
    void visitItemInRightSetOnly(T itemInRightSetOnly);
    void visitItemInBothSets(T itemInBothSets);

  }

  public static <T> RBSet<T> union(RBSet<T> set1, RBSet<T> set2) {
    return smartSetUnion(set1.asSet(), set2.asSet());
  }

  public static <T> RBSet<T> union(Set<T> set1, RBSet<T> set2) {
    return smartSetUnion(set1, set2.asSet());
  }

  public static <T> RBSet<T> union(RBSet<T> set1, Set<T> set2) {
    return smartSetUnion(set1.asSet(), set2);
  }

  public static <T> RBSet<T> union(RBSet<T> rbSet, T item) {
    return union(rbSet, singletonRBSet(item));
  }

  private static <T> RBSet<T> smartSetUnion(Set<T> set1, Set<T> set2) {
    if (set1.isEmpty()) {
      return rbSet(set2);
    } else if (set2.isEmpty()) {
      return rbSet(set1);
    }
    return rbSet(Sets.union(set1, set2));
  }

  /**
   * Returns empty if the items in the list are not unique, otherwise it returns them as an RBSet.
   */
  public static <T> Optional<RBSet<T>> toRBSetIfUnique(List<T> items) {
    return toRBSetIfUnique(items.iterator());
  }

  /**
   * Returns empty if the items are not unique, otherwise it returns them as an RBSet.
   */
  public static <T> Optional<RBSet<T>> toRBSetIfUnique(Iterator<T> items) {
    MutableRBSet<T> mutableSet = newMutableRBSet();
    while (items.hasNext()) {
      T item = items.next();
      if (mutableSet.contains(item)) {
        return Optional.empty();
      }
      mutableSet.add(item);
    }
    return Optional.of(newRBSet(mutableSet));
  }

  /**
   * If you have N sets where N is big, this should be more efficient than doing N-1 set unions of pairs of sets.
   */
  public static <T> RBSet<T> union(Iterator<RBSet<T>> sets) {
    Set<T> union = Sets.newHashSet();
    while (sets.hasNext()) {
      union.addAll(sets.next().asSet());
    }
    return newRBSet(union);
  }

  @SafeVarargs
  public static <T> RBSet<T> unionOfRBSets(RBSet<T> first, RBSet<T> second, RBSet<T> ... rest) {
    return union(concatenateFirstSecondAndRest(first, second, rest).iterator());
  }

  /**
   * If you have N sets where N is big, this should be more efficient than doing N-1 set unions of pairs of sets.
   */
  public static <T> RBSet<T> unionOfPlainSets(Iterator<Set<T>> sets) {
    Set<T> union = Sets.newHashSet();
    while (sets.hasNext()) {
      union.addAll(sets.next());
    }
    return newRBSet(union);
  }

  @SafeVarargs
  public static <T> RBSet<T> unionOfPlainSets(Set<T> first, Set<T> second, Set<T> ... rest) {
    return unionOfPlainSets(concatenateFirstSecondAndRest(first, second, rest).iterator());
  }

  public static <T> RBSet<T> unionOfIncludedSets(Iterator<? extends HasRbSet<T>> sets) {
    Set<T> union = Sets.newHashSet();
    while (sets.hasNext()) {
      union.addAll(sets.next().getRbSet().asSet());
    }
    return newRBSet(union);
  }

  public static <T> RBSet<T> intersection(RBSet<T> set1, RBSet<T> set2) {
    return rbSet(Sets.intersection(set1.asSet(), set2.asSet()));
  }

  public static <T> RBSet<T> intersection(Set<T> set1, RBSet<T> set2) {
    return rbSet(Sets.intersection(set1, set2.asSet()));
  }

  public static <T> RBSet<T> intersection(RBSet<T> set1, Set<T> set2) {
    return rbSet(Sets.intersection(set1.asSet(), set2));
  }

  public static <T> boolean noSharedItems(RBSet<T> set1, RBSet<T> set2) {
    return noSharedItems(set1.asSet(), set2.asSet());
  }

  public static <T> boolean noSharedItems(Set<T> set1, RBSet<T> set2) {
    return noSharedItems(set1, set2.asSet());
  }

  public static <T> boolean noSharedItems(RBSet<T> set1, Set<T> set2) {
    return noSharedItems(set1.asSet(), set2);
  }

  /** This has a 2-Set arg signature because java Sets does not include such a method. */
  public static <T> boolean noSharedItems(Set<T> set1, Set<T> set2) {
    if (set1.isEmpty() || set2.isEmpty()) {
      return true; // small performance optimization
    }
    return set1
        .stream()
        .noneMatch(item1 -> set2.contains(item1));
  }

  public static <T> boolean noSharedItemsInCollection(Collection<RBSet<T>> rbSets) {
    int totalItems = rbSets
        .stream()
        .mapToInt(set -> set.size())
        .sum();
    MutableRBSet<T> allItems = newMutableRBSetWithExpectedSize(totalItems);
    // If any items are shared, they will be collapsed into a single one in allItems, so its size will be smaller.
    for (RBSet<T> set : rbSets) {
      for (T item : set) {
        boolean wasAbsent = allItems.add(item);
        if (!wasAbsent) {
          return false;
        }
      }
    }
    return true;
  }

  @SafeVarargs
  public static <T> boolean noSharedItems(RBSet<T> ... rbSets) {
    return noSharedItemsInCollection(Arrays.asList(rbSets));
  }

  public static <T> RBSet<T> difference(RBSet<T> set1, RBSet<T> set2) {
    return rbSet(Sets.difference(set1.asSet(), set2.asSet()));
  }

  public static <T> RBSet<T> difference(Set<T> set1, RBSet<T> set2) {
    return rbSet(Sets.difference(set1, set2.asSet()));
  }

  public static <T> RBSet<T> difference(RBSet<T> set1, Set<T> set2) {
    return rbSet(Sets.difference(set1.asSet(), set2));
  }

  public static <T> boolean isSubsetOf(RBSet<T> subset, RBSet<T> superset) {
    return isSubsetOf(subset.asSet(), superset.asSet());
  }

  public static <T> boolean isSubsetOf(RBSet<T> subset, Set<T> superset) {
    return isSubsetOf(subset.asSet(), superset);
  }

  public static <T> boolean isSubsetOf(Set<T> subset, RBSet<T> superset) {
    return isSubsetOf(subset, superset.asSet());
  }

  public static <T> boolean isSubsetOf(Set<T> subset, Set<T> superset) {
    return subset
        .stream()
        .allMatch(item1 -> superset.contains(item1));
  }

  public static <T> boolean equalAsSets(List<T> list, Set<T> set) {
    return list.size() == set.size()
        && list.stream().allMatch(listItem -> set.contains(listItem));
  }

  public static <T> boolean equalAsSets(List<T> list, RBSet<T> set) {
    return list.size() == set.size()
        && list.stream().allMatch(listItem -> set.contains(listItem));
  }

  /**
   * When you have 2 sets, this lets you perform 3 different actions for items that are only in the left set,
   * only in the right, or in both sets.
   *
   * It lets you specify a bit more cleanly (via the TwoRBSetsVisitor) what to do in those 3 different cases.
   */
  public static <T> void visitItemsOfTwoSets(RBSet<T> leftSet, RBSet<T> rightSet, TwoRBSetsVisitor<T> visitor) {
    for (T key1 : leftSet) {
      if (rightSet.contains(key1)) {
        visitor.visitItemInBothSets(key1);
      } else {
        visitor.visitItemInLeftSetOnly(key1);
      }
    }
    for (T key2 : rightSet) {
      if (!leftSet.contains(key2)) {
        visitor.visitItemInRightSetOnly(key2);
      }
    }
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> RBSet<K> setUnionOfFirstAndRest(K first, K...rest) {
    return newRBSet(RBStreams.concatenateFirstAndRest(first, rest)
        .collect(Collectors.toSet()));
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> RBSet<K> setUnionOfFirstSecondAndRest(K first, K second, K...rest) {
    return newRBSet(concatenateFirstSecondAndRest(first, second, rest)
        .collect(Collectors.toSet()));
  }

  /**
   * For a base class B and a subclass S, Java will always let you use an S instead of a B,
   * but it will not let you use an {@code RBSet<S>} instead of an {@code RBSet<B>}. This will make that conversion.
   * It's easy enough to call this code inlined - this doesn't save that much boilerplate code -
   * but the name of this method makes the intent clearer.
   */
  @SuppressWarnings("unchecked")
  public static <S, B> RBSet<B> castRBSet(RBSet<S> originalSet) {
    return originalSet.transform(v -> (B) v);
  }

}
