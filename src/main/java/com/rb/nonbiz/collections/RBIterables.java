package com.rb.nonbiz.collections;

import com.rb.nonbiz.text.Strings;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.RBIterators.consecutiveNonOverlappingPairsIterator;
import static com.rb.nonbiz.collections.RBIterators.consecutivePairsIterator;

/**
 * Various static methods pertaining to Iterable objects.
 */
public class RBIterables {

  /**
   * This allows you to walk over an iterable 2 consecutive items at a time and not have to worry about
   * saving the 1st item as 'previous' and remembering to update it.
   * E.g. if 'inner' is A, B, C, D, then the list will have (A, B), (B, C), and (C, D).
   *
   * Since we're building a list, we might as well return a list instead of an Iterable,
   * since the list has extra conveniences.
   */
  public static <T> List<PairOfSameType<T>> consecutivePairs(Iterable<T> inner) {
    return newArrayList(consecutivePairsIterator(inner.iterator()));
  }

  /**
   * This allows you to walk over an iterable 2 consecutive items at a time (with no overlap) and not have to worry about
   * saving the 1st item as 'previous' and remembering to update it.
   * E.g. if 'inner' is A, B, C, D, E, F, then the list will have (A, B), (C, D), (E, F).
   *
   * Since we're building a list, we might as well return a list instead of an Iterable,
   * since the list has extra conveniences.
   */
  public static <T> List<PairOfSameType<T>> consecutiveNonOverlappingPairs(Iterable<T> inner) {
    return newArrayList(consecutiveNonOverlappingPairsIterator(inner.iterator()));
  }

  public static <T> void applyToConsecutivePairs(Iterator<T> inner, BiConsumer<T, T> biConsumer) {
    Iterator<PairOfSameType<T>> pairsIterator = consecutivePairsIterator(inner);
    while (pairsIterator.hasNext()) {
      PairOfSameType<T> pairOfSameType = pairsIterator.next();
      biConsumer.accept(pairOfSameType.getLeft(), pairOfSameType.getRight());
    }
  }

  public static double sumDoubles(Collection<Double> doublesList) {
    return doublesList.stream()
        .mapToDouble(Double::doubleValue)
        .sum();
  }

  public static double weightedAverage(List<Double> values, List<Double> weights) {
    double sumOfWeights = 0;
    if (values.size() != weights.size()) {
      throw new IllegalArgumentException(smartFormat(
          "There are %s values but %s weights in the weighted average. Values= %s ; weights= %s",
          values.size(), weights.size(), values, weights));
    }
    if (values.isEmpty()) {
      return 0;
    }
    double sumOfWeightedTerms = 0;
    for (int i = 0; i < values.size(); i++) {
      double weight = weights.get(i);
      if (weight < 0) {
        throw new IllegalArgumentException(smartFormat(
            "Only non-negative weights are allowed; not %s ; values= %s ; weights= %s",
            weight, values, weights));
      }
      sumOfWeightedTerms += weight * values.get(i);
      sumOfWeights += weight;
    }
    if (sumOfWeights <= 0) {
      throw new IllegalArgumentException(smartFormat(
          "Sum of weights must be >= 0. Values= %s ; weights= %s",
          values, weights));
    }
    return sumOfWeightedTerms / sumOfWeights;
  }

  /**
   * Returns
   */
  public static double dotProduct(List<Double> vector1, List<Double> vector2) {
    return RBIterators.dotProduct(vector1.iterator(), vector2.iterator());
  }

  /**
   * Executes some code for each consecutive pair of values.
   */
  public static <T> void consecutivePairsForEach(Iterable<T> iterable, BiConsumer<T, T> biConsumer) {
    consecutivePairs(iterable)
        .forEach(pair -> biConsumer.accept(pair.getLeft(), pair.getRight()));
  }

  /**
   * Executes some code for each consecutive pair of values.
   */
  public static <T> void consecutiveTuplesForEach(int tupleSize, Iterable<T> iterable, Consumer<List<T>> tupleConsumer) {
    RBIterators.consecutiveTuplesForEach(tupleSize, iterable.iterator(), tupleConsumer);
  }

  /**
   * Executes some code for each pair of values in 2 iterables which must be of the same size.
   */
  public static <T1, T2> void forEachPair(Iterable<T1> iter1, Iterable<T2> iter2, BiConsumer<T1, T2> biConsumer) {
    RBIterators.forEachPair(iter1.iterator(), iter2.iterator(), biConsumer);
  }

  /**
   * Returns true if each pair of values in 2 iterables returns true for the supplied {@link BiPredicate}.
   *
   * <p> It will also have a precondition that they have the same number of items. </p>
   *
   * <p> This is useful for situations where we want a partial equality (i.e. not compare every member),
   * or where there's no equality operation that's well-defined enough to add as a method in the data class,
   * which is something we usually avoid to do. One rare example is if the data class stores verb classes in it,
   * which cannot really be compared, except for their {@link Class} object.</p>
   *
   * <p> The name parallels {@link java.util.stream.Stream#allMatch(Predicate)}. That is, this is unrelated to the
   * test hamcrest matchers. </p>
   */
  public static <T1, T2> boolean allPairsMatch(Iterable<T1> iter1, Iterable<T2> iter2, BiPredicate<T1, T2> biPredicate) {
    return RBIterators.allPairsMatch(iter1.iterator(), iter2.iterator(), biPredicate);
  }

  /**
   * Finds the first item in an iterator that satisfies a condition,
   * and returns its position in the iterator (0, 1, etc.)
   * Throws if condition applies to 0, or more than 1 item.
   */
  public static <T> int getOnlyIndexWhere(Iterable<T> iterable, Predicate<T> predicate) {
    return RBIterators.getOnlyIndexWhere(iterable.iterator(), predicate);
  }

  /**
   * E.g. if you pass in keys A, B, C, D, this will operate on AB, AC, AD, BC, BD, CD.
   * It expressly avoids looking at any pair (X, X), hence 'unique' (though that's not a great adjective here).
   */
  public static <K> void forEachUnequalPairInList(List<K> list, BiConsumer<K, K> biConsumer) {
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        biConsumer.accept(list.get(i), list.get(j));
      }
    }
  }

  /**
   * E.g. if you pass in keys A, B, C, D, this will operate on AB/BA, AC/CA, AD/DA, BC/CB, BD/DB, CD/DC.
   * It expressly avoids looking at any pair (X, X), hence 'unique' (though that's not a great adjective here).
   */
  public static <K> void forEachUniquePair(Iterable<K> keys, BiConsumer<K, K> biConsumer) {
    for (K key1 : keys) {
      for (K key2 : keys) {
       if (!key1.equals(key2)) {
         biConsumer.accept(key1, key2);
       }
      }
    }
  }

}
