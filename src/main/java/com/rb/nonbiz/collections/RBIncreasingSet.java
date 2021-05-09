package com.rb.nonbiz.collections;


import com.rb.nonbiz.util.RBOrderingPreconditions;

import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static java.util.Collections.emptyList;

/**
 * Like an RBSet, except that you can iterate in increasing item order.
 * This is a bit inefficient because it stores items twice, but it allows us to retrieve in O(1) expected time
 * (since the RBSet uses an underlying HashSet), and also iterate in O(1) time.
 */
public class RBIncreasingSet<T extends Comparable<? super T>> implements Iterable<T> {

  private final RBSet<T> asSet;
  private final List<T> asList;

  private RBIncreasingSet(RBSet<T> asSet, List<T> asList) {
    this.asSet = asSet;
    this.asList = asList;
  }

  @SafeVarargs
  public static <T extends Comparable<? super T>> RBIncreasingSet<T> newRBIncreasingSet(T...items) {
    if (items.length == 0) {
      return emptyRBIncreasingSet(); // small performance optimization
    }
    List<T> asList = newArrayList(items);
    RBOrderingPreconditions.checkIncreasing(
        asList,
        "Even though we can sort them here, you should pass items in increasing order: items were %s",
        asList);
    return new RBIncreasingSet<>(newRBSet(items), asList);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> newRBIncreasingSet(List<T> items) {
    if (items.size() == 0) {
      return emptyRBIncreasingSet(); // small performance optimization
    }
    RBOrderingPreconditions.checkIncreasing(
        items,
        "Even though we can sort them here, you should pass items in increasing order: items were %s",
        items);
    return new RBIncreasingSet<>(newRBSet(items), items);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> emptyRBIncreasingSet() {
    return new RBIncreasingSet<T>(emptyRBSet(), emptyList());
  }

  // The following rbIncreasingSetOf methods are to satisfy our convention; newRBIncreasingSet already does what's needed.

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> singletonRBIncreasingSet(T t) {
    return newRBIncreasingSet(t);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2) {
    return newRBIncreasingSet(t1, t2);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3) {
    return newRBIncreasingSet(t1, t2, t3);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4) {
    return newRBIncreasingSet(t1, t2, t3, t4);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5, T t6) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5, t6);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5, t6, t7);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5, t6, t7, t8);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5, t6, t7, t8, t9);
  }

  public static <T extends Comparable<? super T>> RBIncreasingSet<T> rbIncreasingSetOf(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10) {
    return newRBIncreasingSet(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
  }

  public boolean contains(T key) {
    return asSet.contains(key);
  }

  public RBSet<T> asSet() {
    return asSet;
  }

  public List<T> asList() {
    return asList;
  }

  @Override
  public Iterator<T> iterator() {
    return asList.iterator();
  }

  @Override
  public String toString() {
    return asList.toString();
  }

}
