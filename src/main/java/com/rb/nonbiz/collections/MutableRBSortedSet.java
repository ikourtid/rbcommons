package com.rb.nonbiz.collections;

import com.google.common.collect.Sets;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * A mutable version of an {@link RBSortedSet}.
 *
 * <p> This is one of the rare mutable data classes in the codebase.
 * Typically, we will build a {@link MutableRBSortedSet} and then 'lock' its values into an {@link RBSortedSet}
 * in the same method, and then return it.
 * We should (almost) never pass around a {@link RBSortedSet}. </p>
 */
public class MutableRBSortedSet<T> {

  private final SortedSet<T> rawSet;

  private MutableRBSortedSet(SortedSet<T> rawSet) {
    this.rawSet = rawSet;
  }

  // Unfortunately we cannot have newMutableRBSortedSetWithExpectedSize; the underlying TreeSet does not support it.
  public static <T> MutableRBSortedSet<T> newMutableRBSortedSet(Comparator<T> comparator) {
    return new MutableRBSortedSet<>(Sets.newTreeSet(comparator));
  }

  // A MutableRBSortedSet does not have to include items that implement Comparable;
  // in general, we prefer passing in a Comparator, since it gives us the flexibility to use different Comparators
  // in different contexts.
  // When that's the case, we could have this constructor shorten things a bit:
  //
  //  public static <C extends Comparable<C>> MutableRBSortedSet<C> newMutableRBSortedSet() {
  //    return new MutableRBSortedSet<>(Sets.newTreeSet());
  //  }
  //
  // However, to avoid confusion, we will require passing in a comparator explicitly.

  public SortedSet<T> asSortedSet() {
    return rawSet;
  }

  public int size() {
    return rawSet.size();
  }

  public boolean isEmpty() {
    return rawSet.isEmpty();
  }

  public boolean contains(T value) {
    return rawSet.contains(value);
  }

  public Iterator<T> iterator() {
    return rawSet.iterator();
  }

  public Stream<T> stream() {
    return rawSet.stream();
  }

  /**
   * Adds an item to the set.
   * It's OK if the item is already in the set.
   *
   * Consider using #addAssumingAbsent if adding the same item twice is not valid behavior,
   * and you want the extra safety check.
   */
  public boolean add(T value) {
    return rawSet.add(value);
  }

  /**
   * Adds an item to the set.
   * Throws if it's already there.
   *
   * If adding the same item twice is not valid behavior, use this instead of plain #add for extra safety.
   */
  public boolean addAssumingAbsent(T value) {
    RBPreconditions.checkArgument(
        !rawSet.contains(value),
        "RBSortedSet already contains %s ; contents are %s",
        value, rawSet);
    return add(value);
  }

  public boolean remove(T value) {
    return rawSet.remove(value);
  }

  public void removeAssumingPresent(T value) {
    boolean wasRemoved = rawSet.remove(value);
    RBPreconditions.checkArgument(
        wasRemoved,
        "removeAssumingPresent for %s did not find a value to remove in MutableRBSortedSet= %s",
        value, rawSet);
  }

  public T first() {
    RBPreconditions.checkArgument(
        !rawSet.isEmpty(),
        "You cannot request the first item from an empty MutableRBSortedSet");
    return rawSet.first();
  }

  public T last() {
    RBPreconditions.checkArgument(
        !rawSet.isEmpty(),
        "You cannot request the last item from an empty MutableRBSortedSet");
    return rawSet.last();
  }

  public boolean containsAll(Collection<? extends T> c) {
    return rawSet.containsAll(c);
  }

  public boolean addAll(Collection<? extends T> c) {
    return rawSet.addAll(c);
  }

  public boolean retainAll(Collection<? extends T> c) {
    return rawSet.retainAll(c);
  }

  public boolean removeAll(Collection<? extends T> c) {
    return rawSet.removeAll(c);
  }

  public void clear() {
    rawSet.clear();
  }

}
