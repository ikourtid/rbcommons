package com.rb.nonbiz.testmatchers;

import com.google.common.collect.BiMap;
import com.google.common.collect.RangeMap;
import com.rb.nonbiz.collections.HasLongMap;
import com.rb.nonbiz.collections.HasLongSet;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.RBEnumMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.HasLongRepresentation;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchIidMap;
import static com.rb.nonbiz.testmatchers.Match.matchUsingEquals;
import static com.rb.nonbiz.testmatchers.RBIterMatchers.iteratorMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.epsilonForNumericsElseEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

public class RBMapMatchers {

  public static <K, V extends PreciseValue> TypeSafeMatcher<RBMap<K, V>> rbMapPreciseValueMatcher(
      RBMap<K, V> expected, Epsilon epsilon) {
    return new TypeSafeMatcher<RBMap<K, V>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, V> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          if (!expected.getOrThrow(key).almostEquals(actual.getOrThrow(key), epsilon)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <K, V extends ImpreciseValue<? super V>> TypeSafeMatcher<RBMap<K, V>> rbMapImpreciseValueMatcher(
      RBMap<K, V> expected, Epsilon epsilon) {
    return new TypeSafeMatcher<RBMap<K, V>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, V> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          if (!expected.getOrThrow(key).almostEquals(actual.getOrThrow(key), epsilon)) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <V extends PreciseValue<? super V>> TypeSafeMatcher<IidMap<V>> iidMapPreciseValueMatcher(
      IidMap<V> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        matchIidMap(v -> v, f -> preciseValueMatcher(f, epsilon)));
  }

  public static <V extends ImpreciseValue<? super V>> TypeSafeMatcher<IidMap<V>> iidMapImpreciseValueMatcher(
      IidMap<V> expected, Epsilon epsilon) {
    return makeMatcher(expected,
        matchIidMap(v -> v, f -> impreciseValueMatcher(f, epsilon)));
  }

  public static <K> TypeSafeMatcher<RBMap<K, BigDecimal>> rbMapBigDecimalMatcher(
      RBMap<K, BigDecimal> expected, Epsilon epsilon) {
    return new TypeSafeMatcher<RBMap<K, BigDecimal>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, BigDecimal> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          if (expected.getOrThrow(key).subtract(actual.getOrThrow(key)).abs().doubleValue() > epsilon.doubleValue()) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  public static <K> TypeSafeMatcher<RBMap<K, Double>> rbMapDoubleMatcher(
      RBMap<K, Double> expected, Epsilon epsilon) {
    return new TypeSafeMatcher<RBMap<K, Double>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, Double> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          if (!epsilon.valuesAreWithin(expected.getOrThrow(key), actual.getOrThrow(key))) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("expected: %s", expected));
      }
    };
  }

  /**
   * Use this if you want to match an RBMap where the values in the expected and actual key
   * must be compared using a TypeSafeMatcher, instead of plain equals.
   * I thought that I could use something from org.hamcrest for plain equality, but I can't figure it out.
   */
  public static <K, V> TypeSafeMatcher<RBMap<K, V>> rbMapMatcher(
      RBMap<K, V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    return new TypeSafeMatcher<RBMap<K, V>>() {
      @Override
      protected boolean matchesSafely(RBMap<K, V> actual) {
        if (!expected.keySet().equals(actual.keySet())) {
          return false;
        }
        for (K key : expected.keySet()) {
          if (!valuesMatcherGenerator.apply(expected.getOrThrow(key)).matches(actual.getOrThrow(key))) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("RBMap: expected %s", expected));
      }
    };
  }

  public static <E extends Enum<E>, V> TypeSafeMatcher<RBEnumMap<E, V>> rbEnumMapMatcher(
      RBEnumMap<E, V> expected,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual -> {
      if (!expected.keySet().equals(actual.keySet())) {
        return false;
      }
      for (Entry<E, V> entryInExpected : expected.entrySet()) {
        E enumKey = entryInExpected.getKey();
        V valueInExpected = entryInExpected.getValue();
        if (!valueMatcherGenerator.apply(valueInExpected).matches(actual.getOrThrow(enumKey))) {
          return false;
        }
      }
      return true; // no mismatch found for any of the enum keys.
    });
  }

  public static <E extends Enum<E>, V> TypeSafeMatcher<RBEnumMap<E, V>> rbEnumMapEqualityMatcher(
      RBEnumMap<E, V> expected) {
    return rbEnumMapMatcher(expected, f -> typeSafeEqualTo(f));
  }

  public static <K extends Comparable, V> TypeSafeMatcher<RangeMap<K, V>> rangeMapGeneralMatcher(
      RangeMap<K, V> expected, MatcherGenerator<K> keysMatcherGenerator, MatcherGenerator<V> valuesMatcherGenerator) {
    return makeMatcher(expected, actual ->
        iteratorMatcher(
            expected.asDescendingMapOfRanges().entrySet().iterator(),
            expectedEntry -> makeMatcher(expectedEntry, actualEntry ->
                rangeMatcher(expectedEntry.getKey(), keysMatcherGenerator).matches(actualEntry.getKey())
                && valuesMatcherGenerator.apply(expectedEntry.getValue()).matches(actualEntry.getValue())))
            .matches(actual.asDescendingMapOfRanges().entrySet().iterator()));
  }

  /**
   * Use this when the key does not need a special matcher and #equals is defined - e.g. it's a LocalDate.
   * In practice, most keys that extend Comparable probably also have an #equals defined.
   */
  public static <K extends Comparable, V> TypeSafeMatcher<RangeMap<K, V>> rangeMapMatcher(
      RangeMap<K, V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    return rangeMapGeneralMatcher(expected, key -> typeSafeEqualTo(key), valuesMatcherGenerator);
  }

  /**
   * Use this when neither key (in range) nor value need a special matcher,
   * i.e. plain equality suffices.
   */
  public static <K extends Comparable, V> TypeSafeMatcher<RangeMap<K, V>> rangeMapEqualityMatcher(RangeMap<K, V> expected) {
    return rangeMapGeneralMatcher(expected, key -> typeSafeEqualTo(key), value -> typeSafeEqualTo(value));
  }

  public static <K extends HasLongRepresentation> TypeSafeMatcher<HasLongSet<K>>
  hasLongSetMatcher(HasLongSet<K> expected) {
    return makeMatcher(expected, actual -> {
      if (expected.size() != actual.size()) {
        return false; // not needed, but small performance optimization.
      }
      TLongHashSet actualRaw = actual.getRawSetUnsafe();
      TLongIterator expectedIterator = expected.rawTroveIterator();
      while (expectedIterator.hasNext()) {
        long expectedValue = expectedIterator.next();
        if (!actualRaw.contains(expectedValue)) {
          return false;
        }
      }
      return true;
    });
  }

  public static <K extends HasLongRepresentation, V> TypeSafeMatcher<HasLongMap<K, V>>
  hasLongMapMatcher(HasLongMap<K, V> expected, MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected, actual -> {
      if (expected.size() != actual.size()) {
        return false; // not needed, but small performance optimization.
      }
      TLongObjectHashMap<V> actualRaw = actual.getRawMapUnsafe();
      TLongObjectIterator<V> expectedIterator = expected.rawTroveIterator();
      while (expectedIterator.hasNext()) {
        expectedIterator.advance();
        long longKey = expectedIterator.key();
        V expectedValue = expectedIterator.value();
        V actualValue = actualRaw.get(longKey);
        if (actualValue == null) {
          return false;
        }
        if (!matcherGenerator.apply(expectedValue).matches(actualValue)) {
          return false;
        }
      }
      return true;
    });
  }

  public static <K extends HasLongRepresentation, V> TypeSafeMatcher<HasLongMap<K, V>>
  hasLongMapEqualityMatcher(HasLongMap<K, V> expected) {
    return hasLongMapMatcher(expected, f -> typeSafeEqualTo(f));
  }

  /**
   * Unlike an RBMap which can be matched in other ways (e.g. values are epsilon-different),
   * we cannot do this with a BiMap, because the values themselves are also used as keys in the inverted map.
   * So a map of "a" {@code ->} 1.1, "b" {@code ->} 1.1 + e cannot be inverted and have as keys 1.1 and 1.1 + e; it's not a good idea,
   * although it is possible. For the same reason why checking two doubles for equality is generally not good,
   * it's also not good to have doubles as keys, because if your (double) key is epsilon-different, it will look like
   * the map doesn't have a value for that key.
   * 
   * Hence the only matcher for an BiMap uses equality.
   */
  public static <A, B> TypeSafeMatcher<BiMap<A, B>> biMapEqualityMatcher(BiMap<A, B> expected) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.entrySet()));
  }

  public static <K, V> TypeSafeMatcher<Map.Entry<K, V>> mapEntryMatcher(
      Map.Entry<K, V> expected,
      MatcherGenerator<V> matcherGenerator) {
    return makeMatcher(expected,
        matchUsingEquals(v -> v.getKey()),
        match(v -> v.getValue(), matcherGenerator));
  }

  public static <K, V> TypeSafeMatcher<LinkedHashMap<K, V>> linkedHashMapMatcher(
      LinkedHashMap<K, V> expected, MatcherGenerator<V> valuesMatcherGenerator) {
    // Because LinkedHashMap uses a fixed ordering of entries, we must check them in order
    return makeMatcher(expected,
        match(v -> v.entrySet().iterator(), f -> iteratorMatcher(f,
            f2 -> mapEntryMatcher(f2, valuesMatcherGenerator))));
  }

  public static <K, V> TypeSafeMatcher<TreeMap<K, V>> treeMapMatcher(
      TreeMap<K, V> expected,
      MatcherGenerator<K> keysMatcherGenerator,
      MatcherGenerator<V> valuesMatcherGenerator) {
    // This is a bit trickier than rbMapMatcher, because we need inapproximate checks.
    return makeMatcher(expected,
        // First, the keys have to match; iteratorMatcher also checks that we have the same # of keys.
        match(v -> v.descendingKeySet().iterator(), f -> iteratorMatcher(f, keysMatcherGenerator)),

        // Then, the values must match. Those are in a deterministic (increasing) order for both maps,
        // so this will work.
        match(v -> v.values().iterator(), f -> iteratorMatcher(f, valuesMatcherGenerator)));
  }

}
