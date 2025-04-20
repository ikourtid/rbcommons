package com.rb.nonbiz.collections;

import com.rb.nonbiz.util.RBPreconditions;

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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBComparators.composeComparators;
import static com.rb.nonbiz.collections.RBIterators.getFirstNonUniqueIteratorItem;
import static com.rb.nonbiz.collections.RBIterators.iteratorItemsAreUnique;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBStreams.pasteIntoStream;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Map.Entry.comparingByKey;

/**
 * Various static methods pertaining to {@link List} objects.
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
    // We need the -1 so that i can be all valid indices of a consecutive pair in the list.
    for (int i = 0; i < list.size() - 1; i++) {
      if (consecutivePairPredicate.test(list.get(i), list.get(i + 1))) {
        return OptionalInt.of(i);
      }
    }
    return OptionalInt.empty();
  }

  /**
   * Returns a copy of the initial list where consecutive items may be 'reduced' (as in map/reduce).
   *
   * <p> This is different than 'reducing' a list, e.g. adding a bunch of numbers, which results in a single number.
   * Instead, this returns a list (not a scalar), where certain sublists inside it may be reduced into a single value.
   * The tests may explain this better. </p>
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
    // Therefore, we could just create a list with no size hint, and expect newArrayList to auto-reallocate (and copy,
    // which is expensive). However, given that the initial usage of this method
    // (March 2023) is to merge tax lots that only differ in their size, it's very likely that the final list size
    // will be only slightly smaller than the original one. So let's just use a list of the same size.
    // We'll do -1 because we know for sure there will be at least one reduction.
    List<T> reducedList = newArrayListWithExpectedSize(list.size() - 1);

    // Copy everything until the point of the first reduction; this is a one-off operation so that we can
    // utilize (and not waste) the result of findIndexOfFirstConsecutivePair()
    for (int i = 0; i < indexOfFirstReduction.getAsInt(); i++) {
      reducedList.add(list.get(i));
    }
    // Then add the result of the first reduction (even if this may be further reduced later)
    reducedList.add(reducer.apply(
        list.get(indexOfFirstReduction.getAsInt()),
        list.get(indexOfFirstReduction.getAsInt() + 1)));

    for (int i = indexOfFirstReduction.getAsInt() + 2; i < list.size(); i++) {
      int indexOfLast = reducedList.size() - 1;
      T previousItem = reducedList.get(indexOfLast);
      T thisItem = list.get(i);
      if (mustReduceItems.test(previousItem, thisItem)) {
        // Just modify the last item in the reducedList
        reducedList.set(indexOfLast, reducer.apply(previousItem, thisItem));
      } else {
        // We can't reduce this against the last item in the reducedList, so we will just add it.
        reducedList.add(thisItem);
      }
    }
    return reducedList;
  }

  /**
   * This is similar to {@link List} equality, except that, instead of using the default {@link Object#equals(Object)}
   * method to compare two items for equality, it uses the supplied {@link BiPredicate}.
   *
   * <p> We typically avoid implementing equals/hashCode in our codebase, because prod code rarely needs it,
   * and test code uses hamcrest matchers. So this is useful in cases where plain list equality wouldn't work. </p>
   */
  public static <T> boolean listsAreSimilar(List<T> list1, List<T> list2, BiPredicate<T, T> itemsAreSimilar) {
    int size = list1.size();
    if (size != list2.size()) {
      return false;
    }
    return IntStream.range(0, size)
        .allMatch(i -> itemsAreSimilar.test(list1.get(i), list2.get(i)));
  }

  /**
   * Transforms a list to a different one using 'external iteration', i.e. knowledge of both the value
   * being transformed, but also the numeric index that we're in.
   */
  public static <T1, T2> List<T2> transformUsingBothIndexAndValue(
      List<T1> list,
      BiFunction<Integer, T1, T2> externalIterationTransformer) {
    // We'd normally do this fluently, but since this is infrastructure / rbcommons code whose implementation
    // is not exposed, we might as well choose the slightly more performant version where we just use an array.
    List<T2> newList = newArrayListWithExpectedSize(list.size());
    for (int i = 0; i < list.size(); i++) {
      newList.add(externalIterationTransformer.apply(i, list.get(i)));
    }
    return newList;
  }

  /**
   * A convenience method to create a copy of the original list with only one if its elements changed.
   */
  public static <T> List<T> copyWithModifiedElement(List<T> originalList, int index, UnaryOperator<T> operator) {
    RBPreconditions.checkArgument(
        0 <= index && index < originalList.size(),
        "%s is not a valid index for an array with %s items: %s",
        index, originalList.size(), originalList);
    List<T> newList = newArrayListWithCapacity(originalList.size());

    for (int i = 0; i < originalList.size(); i++) {
      T currentValue = originalList.get(i);
      newList.add(i == index ? operator.apply(currentValue) : currentValue);
    }
    // We have good enough habits never to modify lists inside the code, but let's add this just in case.
    return unmodifiableList(newList);
  }

  /**
   * Returns a prefix of the list (i.e. the prefix N items) where the supplied predicate holds for all of them.
   */
  public static <T> List<T> getListPrefixWherePredicateHoldsContiguously(
      List<T> originalList,
      Predicate<T> includeItem) {
    Integer firstFailure = null;
    for (int i = 0; i < originalList.size(); i++) {
      if (!includeItem.test(originalList.get(i))) {
        firstFailure = i;
        break;
      }
    }
    return firstFailure == null
        ? originalList
        : originalList.subList(0, firstFailure); // note that the ending index is exclusive, so we don't need -1 here
  }

  /**
   * Returns a suffix of the list (i.e. the last N items) where the supplied predicate holds for all of them.
   */
  public static <T> List<T> getListSuffixWherePredicateHoldsContiguously(
      List<T> originalList,
      Predicate<T> includeItem) {
    Integer firstFailure = null;
    for (int i = originalList.size() - 1; i >= 0; i--) {
      if (!includeItem.test(originalList.get(i))) {
        firstFailure = i;
        break;
      }
    }
    return firstFailure == null
        ? originalList
        : originalList.subList(firstFailure + 1, originalList.size());
  }

  /**
   * Find the index of the first item in the list where the predicate is true.
   *
   * <p> If the predicate is never true, this returns an empty optional. </p>
   */
  public static <T> OptionalInt findFirstWhere(List<T> list, Predicate<T> predicate) {
    return IntStream.range(0, list.size())
        .filter(i -> predicate.test(list.get(i)))
        .findFirst();
  }

  /**
   * Find the index of the last item in the list where the predicate is true.
   *
   * <p> If the predicate is never true, this returns an empty optional. </p>
   */
  public static <T> OptionalInt findLastWhere(List<T> list, Predicate<T> predicate) {
    return IntStream.range(0, list.size())
        .map(i -> list.size() - 1 - i)
        .filter(i -> predicate.test(list.get(i)))
        .findFirst();
  }


  /**
   * Returns a list that only keeps distinct / unique items, but with uniqueness being determined not by using
   * equals() on the list items, but on a field to be extracted from each list item. This is particularly useful
   * when the list items don't implement a non-trivial (i.e. non-Java-default) equals() method, but one of the fields
   * (e.g. some numeric ID) does.
   *
   * <p> This is a limitation on {@link Stream#distinct()}. Our implementation here requires creating an
   * intermediate list, so it's perhaps less efficient than a stream, but it gets around that limitation. </p>
   *
   * @param <T> the datatype of the list item
   * @param <F> the datatype of the field of the list item that will decide 'distinctness'.
   */
  public static <T, F extends Comparable<? super F>> List<T> distinctOnField(
      List<T> list,
      Function<T, F> fieldExtractor,
      BinaryOperator<T> mergeWhenSameField) {
    MutableRBMap<F, T> distinctItems = newMutableRBMap();

    list.forEach(item -> distinctItems.putOrModifyExisting(
        fieldExtractor.apply(item), item, mergeWhenSameField));
    return distinctItems.entrySet()
        .stream()
        .sorted(comparingByKey())
        .map(v -> v.getValue())
        .collect(Collectors.toList());
  }

  /**
   * Returns a copy of the original list, in reversed order.
   */
  public static <T> List<T> reverseList(List<T> original) {
    List<T> reversed = newArrayListWithExpectedSize(original.size());
    for (int i = original.size() - 1; i >= 0; i--) {
      reversed.add(original.get(i));
    }
    return unmodifiableList(reversed);
  }

}
