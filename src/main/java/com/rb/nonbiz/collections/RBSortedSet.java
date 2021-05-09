package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static com.rb.nonbiz.collections.MutableRBSortedSet.newMutableRBSortedSet;

/**
 * Similar to java.util.SortedSet. However, it is meant to be immutable.
 *
 * Always prefer RBSortedSet to a java.util.SortedSet, especially on an interface, but even inside a method's body, when possible.
 *
 * SortedSet implements the Set interface, which includes methods such as #add(), which we do not want.
 * However, RBSortedSet intentionally has NO methods to modify it. That offers compile-time safety.
 *
 * @see MutableRBSortedSet for a class that helps you initialize a SortedRBSet.
 */
public class RBSortedSet<T> implements Iterable<T> {

  private static final RBSortedSet EMPTY_INSTANCE = new RBSortedSet<>(new TreeSet<>());

  private final SortedSet<T> rawSortedSet;

  private RBSortedSet(SortedSet<T> rawSortedSet) {
    this.rawSortedSet = rawSortedSet;
  }

  public static <T> RBSortedSet<T> newRBSortedSet(MutableRBSortedSet<T> mutableRBSortedSet) {
    return mutableRBSortedSet.isEmpty()
        ? EMPTY_INSTANCE // small performance optimization
        : new RBSortedSet<>(mutableRBSortedSet.asSortedSet());
  }

  @SafeVarargs
  public static <T> RBSortedSet<T> newRBSortedSet(Comparator<T> comparator, T...items) {
    if (items.length == 0) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSortedSet<T> mutableSet = newMutableRBSortedSet(comparator);
    for (T item : items) {
      mutableSet.addAssumingAbsent(item);
    }
    return newRBSortedSet(mutableSet);
  }

  public static <T> RBSortedSet<T> newRBSortedSet(Comparator<T> comparator, Iterable<T> items) {
    return newRBSortedSet(comparator, items.iterator());
  }

  public static <T> RBSortedSet<T> newRBSortedSet(Comparator<T> comparator, Iterator<T> items) {
    if (!items.hasNext()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSortedSet<T> mutableSet = newMutableRBSortedSet(comparator);
    items.forEachRemaining(item -> mutableSet.addAssumingAbsent(item));
    return newRBSortedSet(mutableSet);
  }

  public static <T> RBSortedSet<T> newRBSortedSet(Comparator<T> comparator, Stream<T> items) {
    return newRBSortedSet(comparator, items.iterator());
  }

  public static <T> RBSortedSet<T> newRBSortedSetFromPossibleDuplicates(Comparator<T> comparator, T...items) {
    if (items.length == 0) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSortedSet<T> mutableSet = newMutableRBSortedSet(comparator);
    for (T item : items) {
      mutableSet.add(item);
    }
    return newRBSortedSet(mutableSet);
  }

  public static <T> RBSortedSet<T> newRBSortedSetFromPossibleDuplicates(Comparator<T> comparator, Collection<T> items) {
    return newRBSortedSetFromPossibleDuplicates(comparator, items.iterator());
  }

  public static <T> RBSortedSet<T> newRBSortedSetFromPossibleDuplicates(Comparator<T> comparator, Iterator<T> items) {
    if (!items.hasNext()) {
      return EMPTY_INSTANCE; // small performance optimization
    }
    MutableRBSortedSet<T> mutableSet = newMutableRBSortedSet(comparator);
    items.forEachRemaining(item -> mutableSet.add(item));
    return newRBSortedSet(mutableSet);
  }

  public static <T> RBSortedSet<T> newRBSortedSetFromPossibleDuplicates(Comparator<T> comparator, Stream<T> items) {
    return newRBSortedSetFromPossibleDuplicates(comparator, items.iterator());
  }

  /**
   * There is no 0-item override for rbSortedSetOf.
   * This is to force you to use emptyRBSortedSet, which is more explicit and makes reading tests easier.
   * Likewise for singletonRBSortedSet().
   *
   * Note that, since this class is immutable, we actually don't need a comparator for this empty object.
   */
  public static <T> RBSortedSet<T> emptyRBSortedSet() {
    return EMPTY_INSTANCE;
  }

  /**
   * There is no single-item override for rbSortedSetOf.
   * This is to force you to use singletonRBSortedSet, which is more explicit and makes reading tests easier.
   * Likewise for emptyRBSortedSet().
   */
  public static <T> RBSortedSet<T> singletonRBSortedSet(Comparator<T> comparator, T item) {
    return newRBSortedSet(comparator, item);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2) {
    return newRBSortedSet(comparator, t1, t2);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3) {
    return newRBSortedSet(comparator, t1, t2, t3);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4) {
    return newRBSortedSet(comparator, t1, t2, t3, t4);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5, T t6) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5, t6);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5, t6, t7);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5, t6, t7, t8);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5, t6, t7, t8, t9);
  }

  public static <T> RBSortedSet<T> rbSortedSetOf(Comparator<T> comparator, T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10) {
    return newRBSortedSet(comparator, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
  }

  public int size() {
    return rawSortedSet.size();
  }

  public boolean isEmpty() {
    return rawSortedSet.isEmpty();
  }

  public boolean contains(T t) {
    return rawSortedSet.contains(t);
  }

  public T first() {
    RBPreconditions.checkArgument(
        !rawSortedSet.isEmpty(),
        "You cannot request the first item from an empty RBSortedSet");
    return rawSortedSet.first();
  }

  public T last() {
    RBPreconditions.checkArgument(
        !rawSortedSet.isEmpty(),
        "You cannot request the last item from an empty RBSortedSet");
    return rawSortedSet.last();
  }

  @Override
  public Iterator<T> iterator() {
    return rawSortedSet.iterator();
  }

  public boolean containsAll(Collection<? extends T> c) {
    return rawSortedSet.containsAll(c);
  }

  public Stream<T> stream() {
    return rawSortedSet.stream();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RBSortedSet<?> rbSet = (RBSortedSet<?>) o;

    return rawSortedSet.equals(rbSet.rawSortedSet);
  }

  @Override
  public int hashCode() {
    return rawSortedSet.hashCode();
  }

  @Override
  public String toString() {
    return rawSortedSet.toString();
  }

}
