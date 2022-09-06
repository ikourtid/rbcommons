package com.rb.nonbiz.collections;

import com.google.common.collect.Iterators;
import com.rb.nonbiz.functional.HexFunction;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.QuintFunction;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.PairOfSameType.pairOfSameType;
import static java.util.Collections.emptyIterator;

/**
 * Various static methods pertaining to Iterator objects.
 */
public class RBIterators {

  /**
   * This allows you to walk over an iterator 2 consecutive items at a time and not have to worry about
   * saving the 1st item as 'previous' and remembering to update it.
   * E.g. for an iterator with items A, B, C, D, this will iterate of (A, B), (B, C), and (C, D).
   */
  public static <T> Iterator<PairOfSameType<T>> consecutivePairsIterator(Iterator<T> innerIterator) {
    if (!innerIterator.hasNext()) {
      return emptyIterator();
    }
    return new Iterator<PairOfSameType<T>>() {
      T previousValue = innerIterator.next();

      @Override
      public boolean hasNext() {
        return innerIterator.hasNext();
      }

      @Override
      public PairOfSameType<T> next() {
        T thisValue = innerIterator.next();
        PairOfSameType<T> toReturn = pairOfSameType(previousValue, thisValue);
        previousValue = thisValue;
        return toReturn;
      }
    };
  }

  /**
   * This allows you to walk over an iterator 2 consecutive items at a time and not have to worry about
   * saving the 1st item as 'previous' and remembering to update it.
   * E.g. for an iterator with items A, B, C, D, this will iterate of (A, B) and (C, D).
   * It throws an exception if the iterator does not have an even number of items.
   */
  public static <T> Iterator<PairOfSameType<T>> consecutiveNonOverlappingPairsIterator(Iterator<T> innerIterator) {
    if (!innerIterator.hasNext()) {
      // This is just a performance optimization for cases where the iterator is empty,
      // so we won't bother constructing an iterator to return.
      return emptyIterator();
    }
    return new Iterator<PairOfSameType<T>>() {
      T leftValue;
      T rightValue;

      @Override
      public boolean hasNext() {
        return innerIterator.hasNext();
      }

      @Override
      public PairOfSameType<T> next() {
        leftValue = innerIterator.next();
        RBPreconditions.checkArgument(
            innerIterator.hasNext(),
            "We cannot use consecutiveNonOverlappingPairsIterator on an iterator of odd-numbered size (last item was %s )",
            leftValue);
        rightValue = innerIterator.next();
        return pairOfSameType(leftValue, rightValue);
      }
    };
  }

  /**
   * Useful for iterating between consecutive pairs in an iterator.
   *
   * @see #consecutivePairsIterator
   */
  public static <T> void consecutivePairsForEach(Iterator<T> innerIterator, BiConsumer<T, T> biConsumer) {
    consecutivePairsIterator(innerIterator)
        .forEachRemaining( pairOfSameType -> biConsumer.accept(pairOfSameType.getLeft(), pairOfSameType.getRight()));
  }

  /**
   * This allows you to walk over an iterator 2 consecutive items at a time and not have to worry about
   * saving the 1st item as 'previous' and remembering to update it.
   */
  public static <T> Iterator<List<T>> consecutiveTuplesIterator(int tupleSize, Iterator<T> innerIterator) {
    RBPreconditions.checkArgument(
        tupleSize >= 1,
        "Tuple size in consecutiveTuplesIterator must be >= 1 but was %s",
        tupleSize);
    LinkedList<T> initialRunningTuple = new LinkedList<>();
    for (int i = 0; i < tupleSize - 1; i++) {
      if (!innerIterator.hasNext()) {
        return emptyIterator();
      }
      initialRunningTuple.addLast(innerIterator.next());
    }
    return new Iterator<List<T>>() {
      LinkedList<T> runningTuple = initialRunningTuple;

      @Override
      public boolean hasNext() {
        return innerIterator.hasNext();
      }

      @Override
      public List<T> next() {
        if (runningTuple.size() == tupleSize) {
          runningTuple.removeFirst(); // removing oldest item in consecutive items tuple
        }
        runningTuple.addLast(innerIterator.next());
        return newArrayList(runningTuple);
      }
    };
  }

  /**
   * Useful for iterating between consecutive tuples in an iterator.
   *
   * @see #consecutivePairsForEach
   */
  public static <T> void consecutiveTuplesForEach(int tupleSize, Iterator<T> innerIterator, Consumer<List<T>> consumer) {
    consecutiveTuplesIterator(tupleSize, innerIterator)
        .forEachRemaining( tuple -> consumer.accept(tuple));
  }

  /**
   * This allows you to 'paste' (in the unix utility sense) 2 iterators (assumed to have the same # of elements)
   * and perform some action on the combination of the left and right item.
   *
   * It will also have a precondition that they have the same number of items.
   */
  public static <T1, T2> void forEachPair(Iterator<T1> iter1, Iterator<T2> iter2, BiConsumer<T1, T2> biConsumer) {
    while (iter1.hasNext() && iter2.hasNext()) {
      biConsumer.accept(iter1.next(), iter2.next());
    }
    RBPreconditions.checkArgument(!iter1.hasNext() && !iter2.hasNext());
  }

  /**
   * Returns true if each pair of values in 2 iterators returns true for the supplied {@link BiPredicate}.
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
  public static <T1, T2> boolean allPairsMatch(Iterator<T1> iter1, Iterator<T2> iter2, BiPredicate<T1, T2> biPredicate) {
    boolean allTrue = true;
    while (iter1.hasNext() && iter2.hasNext()) {
      if (!biPredicate.test(iter1.next(), iter2.next())) {
        allTrue = false;
        // We will not exit the loop here, because we want to get to the final precondition and allow it to throw
        // if the two iterators do not have the same number of items.
      }
    }
    RBPreconditions.checkArgument(!iter1.hasNext() && !iter2.hasNext());
    return allTrue;
  }

  /**
   * Transforms 2 iterators into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this will iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the 2 input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2, T3> Iterator<T3> pasteIntoNewIterator(
      Iterator<T1> iter1, Iterator<T2> iter2, BiFunction<T1, T2, T3> biFunction) {
    return new Iterator<T3>() {
      @Override
      public boolean hasNext() {
        RBPreconditions.checkArgument(
            iter1.hasNext() == iter2.hasNext(),
            "Iterators to be pasted in pasteIntoNNewIterator do not have the same # of items");
        return iter1.hasNext() && iter2.hasNext();
      }

      @Override
      public T3 next() {
        return biFunction.apply(iter1.next(), iter2.next());
      }
    };
  }

  /**
   * Transforms 3 iterators into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the 3 input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2, T3, T> Iterator<T> paste3IntoNewIterator(
      Iterator<T1> iter1, Iterator<T2> iter2, Iterator<T3> iter3, TriFunction<T1, T2, T3, T> triFunction) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        int numHasNext = (iter1.hasNext() ? 1 : 0)
            + (iter2.hasNext() ? 1 : 0)
            + (iter3.hasNext() ? 1 : 0);
        if (numHasNext == 0) {
          return false;
        } else if (numHasNext == 3) {
          return true;
        }
        throw new IllegalArgumentException(Strings.format(
            "The 3 iterators are of unequal sizes; %s of 3 have hasNext() be true. Should be all or none",
            numHasNext));
      }

      @Override
      public T next() {
        return triFunction.apply(iter1.next(), iter2.next(), iter3.next());
      }
    };
  }

  /**
   * Transforms 4 iterators into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the 4 input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2, T3, T4, T> Iterator<T> paste4IntoNewIterator(
      Iterator<T1> iter1, Iterator<T2> iter2, Iterator<T3> iter3, Iterator<T4> iter4,
      QuadriFunction<T1, T2, T3, T4, T> quadriFunction) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        int numHasNext = (iter1.hasNext() ? 1 : 0)
            + (iter2.hasNext() ? 1 : 0)
            + (iter3.hasNext() ? 1 : 0)
            + (iter4.hasNext() ? 1 : 0);
        if (numHasNext == 0) {
          return false;
        } else if (numHasNext == 4) {
          return true;
        }
        throw new IllegalArgumentException(Strings.format(
            "The 4 iterators are of unequal sizes; %s of 4 have hasNext() be true. Should be all or none",
            numHasNext));
      }

      @Override
      public T next() {
        return quadriFunction.apply(iter1.next(), iter2.next(), iter3.next(), iter4.next());
      }
    };
  }

  /**
   * Transforms 5 iterators into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the 5 input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2, T3, T4, T5, T> Iterator<T> paste5IntoNewIterator(
      Iterator<T1> iter1, Iterator<T2> iter2, Iterator<T3> iter3, Iterator<T4> iter4, Iterator<T5> iter5,
      QuintFunction<T1, T2, T3, T4, T5, T> quintFunction) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        int numHasNext = (iter1.hasNext() ? 1 : 0)
            + (iter2.hasNext() ? 1 : 0)
            + (iter3.hasNext() ? 1 : 0)
            + (iter4.hasNext() ? 1 : 0)
            + (iter5.hasNext() ? 1 : 0);
        if (numHasNext == 0) {
          return false;
        } else if (numHasNext == 5) {
          return true;
        }
        throw new IllegalArgumentException(Strings.format(
            "The 5 iterators are of unequal sizes; %s of 5 have hasNext() be true. Should be all or none",
            numHasNext));
      }

      @Override
      public T next() {
        return quintFunction.apply(iter1.next(), iter2.next(), iter3.next(), iter4.next(), iter5.next());
      }
    };
  }

  /**
   * Transforms 6 iterators into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the 6 input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2, T3, T4, T5, T6, T> Iterator<T> paste6IntoNewIterator(
      Iterator<T1> iter1, Iterator<T2> iter2, Iterator<T3> iter3, Iterator<T4> iter4, Iterator<T5> iter5,
      Iterator<T6> iter6,
      HexFunction<T1, T2, T3, T4, T5, T6, T> hexFunction) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        int numHasNext = (iter1.hasNext() ? 1 : 0)
            + (iter2.hasNext() ? 1 : 0)
            + (iter3.hasNext() ? 1 : 0)
            + (iter4.hasNext() ? 1 : 0)
            + (iter5.hasNext() ? 1 : 0)
            + (iter6.hasNext() ? 1 : 0);
        if (numHasNext == 0) {
          return false;
        } else if (numHasNext == 6) {
          return true;
        }
        throw new IllegalArgumentException(Strings.format(
            "The 6 iterators are of unequal sizes; %s of 6 have hasNext() be true. Should be all or none",
            numHasNext));
      }

      @Override
      public T next() {
        return hexFunction.apply(iter1.next(), iter2.next(), iter3.next(), iter4.next(), iter5.next(), iter6.next());
      }
    };
  }

  /**
   * Transforms N iterators of the same type into a new iterator, based on a supplied function.
   * If the iterators have unequal # of items, this will iterator will throw when we're done iterating over it.
   * Of course, we'll only find out at runtime, since we can't consume the N input iterators in this method
   * just to see if they have the same # of items.
   */
  public static <T1, T2> Iterator<T2> pasteMultipleIntoNewIterator(
      Function<List<T1>, T2> transformer,
      List<Iterator<T1>> allIterators) {
    return new Iterator<T2>() {
      @Override
      public boolean hasNext() {
        long numHasNext = allIterators.stream().filter(iter -> iter.hasNext()).count();
        if (numHasNext == 0) {
          return false;
        } else if (numHasNext == allIterators.size()) {
          return true;
        }
        throw new IllegalArgumentException(Strings.format(
            "Iterators are of unequal sizes; %s of %s have hasNext() be true. Should be all or neither",
            numHasNext, allIterators.size()));
      }

      @Override
      public T2 next() {
        return transformer.apply(allIterators.stream()
            .map(v -> v.next())
            .collect(Collectors.toList()));
      }
    };
  }

  /**
   * Returns the sum of the products of each pair of values (1 from iterator1, 1 from iterator2).
   * Throws if the 2 iterators are not the same size, or are empty.
   */
  public static double dotProduct(Iterator<Double> iterator1, Iterator<Double> iterator2) {
    double sumOfTerms = 0;
    RBPreconditions.checkArgument(
        iterator1.hasNext() && iterator2.hasNext(),
        "We must have at least 1 element for each vector in order to compute a dot product");
    while (iterator1.hasNext()) {
      double value1 = iterator1.next();
      RBPreconditions.checkArgument(iterator2.hasNext());
      double value2 = iterator2.next();
      sumOfTerms += value1 * value2;
    }
    RBPreconditions.checkArgument(
        !iterator1.hasNext() && !iterator2.hasNext(),
        "The 2 vectors did not have the same number of elements; cannot compute dot product");
    return sumOfTerms;
  }

  public static <T> boolean iteratorItemsAreUnique(Iterator<T> iterator) {
    return !getFirstNonUniqueIteratorItem(iterator).isPresent();
  }

  public static <T> Optional<T> getFirstNonUniqueIteratorItem(Iterator<T> iterator) {
    MutableRBSet<T> mutableSet = newMutableRBSet();
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (mutableSet.contains(item)) {
        return Optional.of(item);
      }
      mutableSet.add(item);
    }
    return Optional.empty();
  }

  /**
   * Finds the first item in an iterator that satisfies a condition,
   * and returns its position in the iterator (0, 1, etc.)
   * Throws if condition applies to 0, or more than 1 item.
   */
  public static <T> int getOnlyIndexWhere(Iterator<T> iterator, Predicate<T> predicate) {
    Integer correct = null;
    int current = 0;
    while (iterator.hasNext()) {
      T value = iterator.next();
      if (predicate.test(value)) {
        RBPreconditions.checkArgument(
            correct == null,
            "The correct item %s has been encountered in 2 positions, %s and %s",
            value, correct, current);
        correct = current;
      }
      current++;
    }
    RBPreconditions.checkNotNull(
        correct,
        "Could not find the right value in any of the %s items",
        current);
    return correct;
  }

  /**
   * Transforms an iterator of ImpreciseValue to an {@code Iterator<Double>}.
   */
  public static <T extends ImpreciseValue> Iterator<Double> transformToDoubleIterator(Iterator<T> iter) {
    return Iterators.transform(iter, v -> v.doubleValue());
  }

  /**
   * Transforms an iterator of PreciseValue to an iterator of BigDecimal
   */
  public static <T extends PreciseValue> Iterator<BigDecimal> transformToBigDecimalIterator(Iterator<T> iter) {
    return Iterators.transform(iter, v -> v.asBigDecimal());
  }

  /**
   * Return empty optional if empty iterator.
   * Return non-empty optional if singleton iterator.
   * Throw otherwise.
   */
  public static <T> Optional<T> fromIteratorOfZeroOrOneItem(Iterator<T> iter) {
    if (!iter.hasNext()) {
      return Optional.empty();
    }
    T onlyItem = iter.next();
    RBPreconditions.checkArgument(
        !iter.hasNext(),
        "There was more than 1 item in the iterator, whereas we only allow either 0 or 1.");
    return Optional.of(onlyItem);
  }

  /**
   * If the iterator only has one item, return that item. If 0 or 2+ items, throw.
   * Similar to Iterators#getOnlyElement except that it lets you specify an error message.
   */
  public static <T> T getOnlyElementOrThrow(Iterator<T> iter, String format, Object ... args) {
    RBPreconditions.checkArgument(iter.hasNext(), format, args);
    T onlyItem = iter.next();
    RBPreconditions.checkArgument(!iter.hasNext(), format, args);
    return onlyItem;
  }

  /**
   * This is for scenarios where an iterator runs multiple times, and the data from iteration n feeds into
   * iteration n + 1.
   * The supplied function is also told whether this is the last item in the iterator and which iteration # we're at,
   * in case it needs it.
   */
  public static <T, V> V forEach(
      Iterator<T> iterator,
      V initialRunningValue,
      QuadriFunction<V, T, Integer, Boolean, V> iterationBody) {
    V runningValue = initialRunningValue;
    int iterationIndex = 0;
    while (iterator.hasNext()) {
      T iteratorEntry = iterator.next();
      boolean isLastItem = !iterator.hasNext();
      runningValue = iterationBody.apply(runningValue, iteratorEntry, iterationIndex, isLastItem);
      iterationIndex++;
    }
    return runningValue;
  }

  /**
   * This is for scenarios where an iterator runs multiple times, and the data from iteration n feeds into
   * iteration n + 1.
   * The supplied function is also told whether this is the last item in the iterator, in case it needs it.
   */
  public static <T, V> V forEach(
      Iterator<T> iterator,
      V initialRunningValue,
      TriFunction<V, T, Boolean, V> iterationBody) {
    return forEach(
        iterator,
        initialRunningValue,
        (runningValue, iteratorEntry, ignoredIterationIndex, isLastItem) ->
            iterationBody.apply(runningValue, iteratorEntry, isLastItem));
  }

  /**
   * This is for scenarios where an iterator runs multiple times, and the data from iteration n feeds into
   * iteration n + 1.
   */
  public static <T, V> V forEach(
      Iterator<T> iterator,
      V initialRunningValue,
      BiFunction<V, T, V> iterationBody) {
    return forEach(
        iterator,
        initialRunningValue,
        (runningValue, iteratorEntry, ignoredIterationIndex, ignoredIsLastItem) ->
            iterationBody.apply(runningValue, iteratorEntry));
  }

}
