package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.collections.MutableRBSortedSet;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.RBSortedSet;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import org.apache.commons.math3.util.MultidimensionalCounter;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeSame;

public class RBCollectionMatchers {

  /**
   * Use this when you want to confirm that 2 collections have the same items, regardless of their order.
   * The comparator doesn't need to correspond to some grand notion of smaller/larger;
   * it just needs to be there so that the 2 input collections can be sorted into SOME deterministic order,
   * so that we can match items with each other.
   */
  public static <T> TypeSafeMatcher<Collection<T>> unorderedCollectionMatcher(
      Collection<T> expected, MatcherGenerator<T> matcherGenerator, Comparator<T> singleItemComparator) {
    return new TypeSafeMatcher<Collection<T>>() {
      @Override
      protected boolean matchesSafely(Collection<T> actual) {
        if (expected.size() != actual.size()) {
          return false;
        }
        List<T> sortedExpected = expected
            .stream()
            .sorted(singleItemComparator)
            .collect(Collectors.toList());
        List<T> sortedActual = actual
            .stream()
            .sorted(singleItemComparator)
            .collect(Collectors.toList());
        for (int i = 0; i < sortedExpected.size(); i++) {
          if (!matcherGenerator.apply(sortedExpected.get(i)).matches(sortedActual.get(i))) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Unordered collection: expected %s", expected));
      }
    };
  }

  /**
   * Use this when you want to confirm that 2 collections have the same items, regardless of their order.
   * The comparator doesn't need to correspond to some grand notion of smaller/larger;
   * it just needs to be there so that the 2 input collections can be sorted into SOME deterministic order,
   * so that we can match items with each other.
   *
   * Although this matcher logic is subsumed under unorderedCollectionMatcher, sometimes you need
   * unorderedListMatcher when you use this matcher inside an expectation for a mock, to avoid some static typing problems.
   */
  public static <T> TypeSafeMatcher<List<T>> unorderedListMatcher(
      List<T> expected, MatcherGenerator<T> matcherGenerator, Comparator<T> singleItemComparator) {
    return new TypeSafeMatcher<List<T>>() {
      @Override
      protected boolean matchesSafely(List<T> actual) {
        return unorderedCollectionMatcher(expected, matcherGenerator, singleItemComparator)
            .matches(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Unordered list: expected %s", expected));
      }
    };
  }

  /**
   * Use this if you want to match an RBSet where the values in the expected and actual
   * must be compared using a TypeSafeMatcher, instead of plain equals.
   *
   * A good example is the with() statement inside a mockery.checking block,
   * where you need to perform a check on a parameter of type RBSet.
   *
   * You don't need to use this within RBTestMatcher::willMatch, where you can just use RBSet::asSet.
   */
  public static <T> TypeSafeMatcher<RBSet<T>> rbSetMatcher(
      RBSet<T> expected, MatcherGenerator<T> matcherGenerator, Comparator<T> singleItemComparator) {
    TypeSafeMatcher<Collection<T>> collectionMatcher =
        unorderedCollectionMatcher(expected.asSet(), matcherGenerator, singleItemComparator);
    return new TypeSafeMatcher<RBSet<T>>() {
      @Override
      protected boolean matchesSafely(RBSet<T> rbset) {
        return collectionMatcher.matches(rbset.asSet());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("RBSet: expected %s", expected));
      }
    };
  }

  /**
   * Use this if you want to match an RBSet based on plain old equals-based equality.
   * Otherwise, use rbSetMatcher.
   */
  public static <T> TypeSafeMatcher<RBSet<T>> rbSetEqualsMatcher(RBSet<T> expected) {
    return new TypeSafeMatcher<RBSet<T>>() {
      @Override
      protected boolean matchesSafely(RBSet<T> actual) {
        return expected.equals(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("RBSet: expected %s", expected));
      }
    };
  }

  public static <T> TypeSafeMatcher<Iterable<T>> orderedIterableMatcher(
      Iterable<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> iteratorMatcher(f, matcherGenerator)));
  }

  public static <T> TypeSafeMatcher<Iterable<T>> orderedIterableEqualityMatcher(
      Iterable<T> expected) {
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> typeSafeEqualTo(f)));
  }

  public static <T> TypeSafeMatcher<List<T>> orderedListMatcher(
      List<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> iteratorMatcher(f, matcherGenerator)));
  }

  public static <T> TypeSafeMatcher<List<T>> orderedListEqualityMatcher(
      List<T> expected) {
    return makeMatcher(expected,
        match(v -> v.iterator(), f -> iteratorMatcher(f, f2 -> typeSafeEqualTo(f2))));
  }

  public static <T> TypeSafeMatcher<List<T>> orderedListPointerMatcher(List<T> expected) {
    return orderedListMatcher(expected, f -> typeSafeSame(f));
  }

  public static <T> TypeSafeMatcher<List<T>> unorderedListPointerMatcher(List<T> expected) {
    return makeMatcher(expected, actual -> newRBSet(expected).equals(newRBSet(actual)));
  }

  public static TypeSafeMatcher<List<Double>> doubleListMatcher(List<Double> expected, double epsilon) {
    return orderedListMatcher(expected, d -> doubleAlmostEqualsMatcher(d, epsilon));
  }

  public static TypeSafeMatcher<List<String>> stringListMatcher(List<String> expected) {
    return orderedListEqualityMatcher(expected);
  }

  public static <V extends PreciseValue<? super V>> TypeSafeMatcher<List<V>> preciseValueListMatcher(List<V> expected, double epsilon) {
    return orderedListMatcher(expected, v -> preciseValueMatcher(v, epsilon));
  }

  public static <V extends ImpreciseValue<? super V>> TypeSafeMatcher<List<V>> impreciseValueListMatcher(List<V> expected, double epsilon) {
    return orderedListMatcher(expected, v -> impreciseValueMatcher(v, epsilon));
  }

  public static TypeSafeMatcher<MultidimensionalCounter> multidimensionalCounterMatcher(MultidimensionalCounter expected) {
    return makeMatcher(expected,
        match(v -> v.getSizes(), f -> intArrayMatcher(f)));
  }

  public static <T> TypeSafeMatcher<RBSortedSet<T>> rbSortedSetMatcher(
      RBSortedSet<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        // Usually hamcrest matching is looser than #equals. In this case it's tighter;
        // the underlying SortedSet is equal only based on the elements, whereas this matcher here
        // also expects that the items are in the same sorted order. We can't confirm that the Comparator is
        // the same in the underlying TreeSet, but we can at least confirm that the comparators sort the same way
        // in both cases.
        match(v -> v.iterator(), f -> iteratorMatcher(f, matcherGenerator)));
  }

  public static <T> TypeSafeMatcher<MutableRBSortedSet<T>> mutableRBSortedSetMatcher(
      MutableRBSortedSet<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        // Usually hamcrest matching is looser than #equals. In this case it's tighter;
        // the underlying SortedSet is equal only based on the elements, whereas this matcher here
        // also expects that the items are in the same sorted order. We can't confirm that the Comparator is
        // the same in the underlying TreeSet, but we can at least confirm that the comparators sort the same way
        // in both cases.
        match(v -> v.iterator(), f -> iteratorMatcher(f, matcherGenerator)));
  }

  public static <E extends Enum<E>, V> TypeSafeMatcher<EnumMap<E, V>> enumMapMatcher(
      EnumMap<E, V> expected, MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual -> {
      if (!expected.keySet().equals(actual.keySet())) {
        return false;
      }
      for (Entry<E, V> entryInExpected : expected.entrySet()) {
        E enumKey = entryInExpected.getKey();
        V valueInExpected = entryInExpected.getValue();
        if (!valueMatcherGenerator.apply(valueInExpected).matches(actual.get(enumKey))) {
          return false;
        }
      }
      return true; // no mismatch found for any of the enum keys.
    });
  }

  public static <E extends Enum<E>, V> TypeSafeMatcher<EnumMap<E, V>> enumMapEqualityMatcher(EnumMap<E, V> expected) {
    return enumMapMatcher(expected, f -> typeSafeEqualTo(f));
  }

}
