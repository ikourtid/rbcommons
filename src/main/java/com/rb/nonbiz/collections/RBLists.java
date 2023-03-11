package com.rb.nonbiz.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBComparators.composeComparators;
import static com.rb.nonbiz.collections.RBIterators.getFirstNonUniqueIteratorItem;
import static com.rb.nonbiz.collections.RBIterators.iteratorItemsAreUnique;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBStreams.pasteIntoStream;
import static java.util.Comparator.comparing;

/**
 * Various static methods pertaining to List objects.
 */
public class RBLists {

  /**
   * Concatenates a bunch of lists into a single one.
   * Sounds simple, but I can't find some library that already does this.
   */
  @SafeVarargs
  public static <K> List<K> listConcatenation(List<K>...lists) {
    return Arrays.stream(lists)
        .flatMap(list -> list.stream())
        .collect(Collectors.toList());
  }

  /**
   * Concatenates a bunch of lists into a single one.
   * Sounds simple, but I can't find some library that already does this.
   */
  public static <K> List<K> listConcatenation(Iterator<List<K>> listIterator) {
    List<K> toReturn = newArrayList();
    listIterator.forEachRemaining(list -> toReturn.addAll(list));
    return toReturn;
  }

  /**
   * Concatenates a bunch of lists into a single one, but eliminates duplicates.
   * This assumes that K implements a nontrivial equals/hashCode, which our classes typically do not,
   * so be careful.
   */
  @SafeVarargs
  public static <K> List<K> listConcatenationWithoutDuplicates(List<K>...lists) {
    return Arrays.stream(lists)
        .flatMap(list -> list.stream())
        .distinct()
        .collect(Collectors.toList());
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> List<K> concatenateFirstAndRest(K first, K...rest) {
    return RBStreams.concatenateFirstAndRest(first, rest)
        .collect(Collectors.toList());
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  public static List<Double> concatenateFirstAndRestDoubles(double first, double...rest) {
    return RBStreams.concatenateFirstAndRestDoubles(first, rest)
        .boxed()
        .collect(Collectors.toList());
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  public static List<Double> concatenateFirstSecondAndRestDoubles(double first, double second, double...rest) {
    return RBStreams.concatenateFirstSecondAndRestDoubles(first, second, rest)
        .boxed()
        .collect(Collectors.toList());
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> List<K> concatenateFirstSecondAndRest(K first, K second, K...rest) {
    return RBStreams.concatenateFirstSecondAndRest(first, second, rest)
        .collect(Collectors.toList());
  }

  /**
   * This is for a common case for specialized static constructors (which usually live in test) that want to force you to
   * supply at least one item. But it can be used anywhere.
   */
  @SafeVarargs
  public static <K> List<K> concatenateFirstSecondThirdAndRest(K first, K second, K third, K...rest) {
    return RBStreams.concatenateFirstSecondThirdAndRest(first, second, third, rest)
        .collect(Collectors.toList());
  }

  /**
   * Unlike Sets.intersection or RBSets.intersection, this will preserve the order in the 1st list.
   * Contrast this with sets, where the intersection (which itself is returned as a set) can be in any order.
   */
  public static <K> List<K> intersection(List<K> list1, List<K> list2) {
    RBSet<K> itemsIn2 = newRBSet(list2);
    return list1
        .stream()
        .filter(item -> itemsIn2.contains(item))
        .collect(Collectors.toList());
  }

  /**
   * deterministic implies 'it's a fixed order, but not guaranteed'
   * ordered means we don't reorder items within the lists.
   * Without loss of generality, if you assume K to be a char,
   * then these will sort like in a dictionary: 'ant' comes before 'bee',
   * and 'ant' comes before 'anti'.
   */
  public static <K extends Comparable<K>> Comparator<List<K>> deterministicOrderedListComparator() {
    return (list1, list2) -> {
      Iterator<K> iter1 = list1.iterator();
      Iterator<K> iter2 = list2.iterator();

      while (iter1.hasNext() && iter2.hasNext()) {
        K item1 = iter1.next();
        K item2 = iter2.next();
        int comparisonResult = item1.compareTo(item2);
        if (comparisonResult != 0) {
          return comparisonResult;
        }
      }
      return iter1.hasNext() ? 1 : iter2.hasNext() ? -1 : 0;
    };
  }

  public static <T> boolean listItemsAreUnique(List<T> items) {
    return iteratorItemsAreUnique(items.iterator());
  }

  public static <T> Optional<T> getFirstNonUniqueListItem(List<T> list) {
    return getFirstNonUniqueIteratorItem(list.iterator());
  }

  /**
   * This is useful for cases where the items in the iterator can be given some sort of score (of type C)
   * that's cheap to do comparisons on, but expensive to calculate. Example: some double score that gets calculated
   * using a complex calculation.
   * We also allow for secondary, tertiary, etc. comparators. For example, when we sort tax lots
   * by loss per share, if that's the same, we want to have some deterministic sorting on lot open date and quantity.
   *
   * All of this is done to avoid calling 'scoringFunction' O(n * logn)
   * times under the hood in the process of sorting, which would be bad, e.g. with the following code
   * (ignoring secondary etc. sorts for clarity):
   *
   * <pre>
   * {@code
   *     return newArrayList(iterator)
   *        .sorted(comparing(v -> scoringFunction.apply(v)))
   *        .collect(Collectors.toList());}
   * </pre>
   */
  @SafeVarargs
  public static <T, C extends Comparable<C>> List<T> sortEfficientlyToList(
      Stream<T> stream, Function<T, C> primaryScoringFunction, Comparator<T>...cheaperSubsequentComparators) {
    return stream
        .map(v -> pair(primaryScoringFunction.apply(v), v))
        .sorted(composeComparators(
            // primary comparison is on the score (of type C) which was calculated once per item
            comparing(pair1 -> pair1.getLeft()),
            Arrays.stream(cheaperSubsequentComparators)
                .map(itemComparator -> (Comparator<Pair<C, T>>)
                    (pair11, pair2) -> itemComparator.compare(pair11.getRight(), pair2.getRight()))
                .collect(Collectors.toList())))
        .map(pair -> pair.getRight())
        .collect(Collectors.toList());
  }

  /**
   * For every item in the collection, we will use the supplied function to create
   * another list of items of type Y. Then, we will concatenate everything into a single {@code List<Y>}.
   * This can be done with a bunch of inline streams and flatMap operations, but calling this method is easier to read.
   */
  public static <X, Y> List<Y> listConcatenationFromEach(Collection<X> items, Function<X, Stream<Y>> transformer) {
    return items.stream()
        .flatMap(x -> transformer.apply(x))
        .collect(Collectors.toList());
  }

  /**
   * For every combination of items in the first and second collection, we will use the supplied function to create
   * another list of items of type Y. Then, we will concatenate everything into a single {@code List<Y>}.
   * This can be done with a bunch of inline streams and flatMap operations, but calling this method is easier to read.
   */
  public static <X1, X2, Y> List<Y> listConcatenationFromEachPair(
      Collection<X1> listX1, Collection<X2> listX2, BiFunction<X1, X2, List<Y>> transformer) {
    return listX1.stream()
        .flatMap(x1 -> listX2.stream().flatMap(x2 -> transformer.apply(x1, x2).stream()))
        .collect(Collectors.toList());
  }

  public static <T> List<T> newRBListWithExpectedSize(int size, Iterator<T> iterator) {
    List<T> list = newArrayListWithExpectedSize(size);
    iterator.forEachRemaining(v -> list.add(v));
    return list;
  }

  /**
   * Create a list that has the same size as the two input lists (which are expected to have the same size),
   * each element of which is the function of the corresponding items (for the same list index) from the two
   * input lists.
   */
  public static <T, T1, T2> List<T> pasteLists(List<T1> list1, List<T2> list2, BiFunction<T1, T2, T> pasteTransformer) {
    return pasteIntoStream(list1, list2, pasteTransformer).collect(Collectors.toList());
  }

  /**
   * For a list of 2+ items, find the first index i such that items i and i+1 match a predicate.
   */
  public static <T> OptionalInt findIndexOfFirstConsecutivePair(
      List<T> list,
      BiPredicate<T, T> consecutivePairPredicate) {
    if (list.size() <= 1) {
      // If there isn't any room to have any consecutive pairs starting at startingIndex, there's no way the predicate
      // would ever be true, since there are no consecutive pairs to begin with!
      return OptionalInt.empty();
    }
    for (int i = 0; i < list.size() - 1; i++) {
      if (consecutivePairPredicate.test(list.get(i), list.get(i + 1))) {
        return OptionalInt.of(i);
      }
    }
    return OptionalInt.empty();
  }

  /**
   * The 'reduce' part of map/reduce is about creating a single item from a collection. This handles the case where
   * only some consecutive items can be reduced.
   */
  public static <T> List<T> possiblyReduceConsecutiveItems(
      List<T> list,
      BiPredicate<T, T> mustReduceItems,
      BinaryOperator<T> reducer) {
    OptionalInt indexOfFirstReduction = findIndexOfFirstConsecutivePair(list, mustReduceItems);
    if (!indexOfFirstReduction.isPresent()) {
      // performance optimization; if no consecutive items need to be reduced, just return the original list.
      // This saves us from having to generate a new list, which would just be a copy of the original one in this case.
      return list;
    }

    // Even if we could run the predicate on every consecutive pair of items, we still wouldn't know how big the
    // final list would be. Say e.g. the 4th, 5th, and 6th items are called A, B, C. If A can be reduced with B,
    // then we'd still need to know if the reduced (merged) result AB can itself be merged with C.
    // Therefore, we could just create a list with no size hint. However, given that the initial usage of this method
    // (March 2023) is to merge tax lots that only differ in their size, it's very likely that the final list size
    // will be only slightly smaller than the original one. So let's just use a list of the same size.
    List<T> reducedList = newArrayListWithExpectedSize(list.size());

    // Copy everything until the point of the first reduction; this is a one-off operation so that we can
    // utilize (and not waste) the result of findIndexOfFirstConsecutivePair()
    for (int i = 0; i < indexOfFirstReduction.getAsInt() - 1; i++) {
      reducedList.add(list.get(i));
    }
    T reducedItem = reducer.apply(
        list.get(indexOfFirstReduction.getAsInt()),
        list.get(indexOfFirstReduction.getAsInt() + 1));
    int lastIndex = list.size() - 1;

    // Special case; it's a bit uglier this way, but we avoid wasting the findIndexOfFirstConsecutivePair() call.
    if (indexOfFirstReduction.getAsInt() + 1 == lastIndex) {
      reducedList.add(reducedItem);
      return reducedList;
    }

    // currentIndex will be the index in 'list' of the *right* element in a consecutive pair of items.
    for (int currentIndex = indexOfFirstReduction.getAsInt() + 2;
         currentIndex < list.size();
         currentIndex++) {
      T previousItem = list.get(currentIndex - 1);
      T thisItem = list.get(currentIndex);
      if (mustReduceItems.test(previousItem, thisItem)) {
        reducedItem = reducer.apply(previousItem, thisItem);
        if (currentIndex == lastIndex) {
          reducedList.add(reducedItem);
          return reducedList;
        }
      } else {
        // We stopped reducing items, so let's store the item that was being reduced...
        reducedList.add(reducedItem);
        // ... and also store the item that we know won't be reduced.
        reducedList.add(previousItem);
      }
    }

    return reducedList;
  }

}
