package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.IidSet;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.IntegerValue;
import com.rb.nonbiz.types.PreciseValue;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.IidSetTest.iidSetMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.arrayMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalDoubleMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalIntMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.classMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.enumMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.impreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.integerValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;

/**
 * This helps avoid the mistake of having the expected and actual on both sides of a match
 * in the (typical) static functions that return TypeSafeMatcher in a data class test that extends RBTestMatcher.
 *
 * F = short for 'field'
 */
public class Match<T, F> {

  private final Function<T, F> fieldExtractor;
  private final MatcherGenerator<F> matcherGenerator;

  private Match(Function<T, F> fieldExtractor, MatcherGenerator<F> matcherGenerator) {
    this.fieldExtractor = fieldExtractor;
    this.matcherGenerator = matcherGenerator;
  }

  public static <T, F> Match<T, F> match(Function<T, F> fieldExtractor, MatcherGenerator<F> matcherGenerator) {
    return new Match<T, F>(fieldExtractor, matcherGenerator);
  }

  public static <S, T extends S> Match<T, S> matchSuperClass(MatcherGenerator<S> matcherGenerator) {
    return new Match<T, S>(v -> v, matcherGenerator);
  }

  public static <T, K, V> Match<T, RBMap<K, V>> matchRBMap(Function<T, RBMap<K, V>> fieldExtractor,
                                                           MatcherGenerator<V> matcherGenerator) {
    return new Match<T, RBMap<K, V>>(fieldExtractor, f -> rbMapMatcher(f, matcherGenerator));
  }

  public static <T, V> Match<T, IidMap<V>> matchIidMap(Function<T, IidMap<V>> fieldExtractor,
                                                       MatcherGenerator<V> matcherGenerator) {
    return new Match<>(fieldExtractor, f -> iidMapMatcher(f, matcherGenerator));
  }

  public static <T, V extends PreciseValue<? super V>> Match<T, IidMap<V>> matchIidPreciseValuesMap(
      Function<T, IidMap<V>> fieldExtractor,
      double epsilon) {
    return new Match<>(fieldExtractor, f -> iidMapMatcher(f, f2 -> preciseValueMatcher(f2, epsilon)));
  }

  public static <T, K, V extends PreciseValue<? super V>> Match<T, RBMap<K, V>> matchPreciseValuesRBMap(
      Function<T, RBMap<K, V>> fieldExtractor,
      double epsilon) {
    return new Match<>(fieldExtractor, f -> rbMapMatcher(f, f2 -> preciseValueMatcher(f2, epsilon)));
  }

  public static <T, K, V extends ImpreciseValue<? super V>> Match<T, RBMap<K, V>> matchImpreciseValuesRBMap(
      Function<T, RBMap<K, V>> fieldExtractor,
      double epsilon) {
    return new Match<>(fieldExtractor, f -> rbMapMatcher(f, f2 -> impreciseValueMatcher(f2, epsilon)));
  }

  // Added for extra clarity; the same functionality could be obtained using matchUsingEquals()
  public static <T, F extends Enum<? super F>> Match<T, F> matchEnum(
    Function<T, F> fieldExtractor) {
    return new Match<>(fieldExtractor, f -> enumMatcher(f));
  }

  public static <T, F> Match<T, Optional<F>> matchOptional(
      Function<T, Optional<F>> fieldExtractor, MatcherGenerator<F> matcherGenerator) {
    return match(fieldExtractor, f -> optionalMatcher(f, matcherGenerator));
  }

  public static <T, F> Match<T, Optional<F>> matchOptionalUsingEquals(
      Function<T, Optional<F>> fieldExtractor) {
    return match(fieldExtractor, f -> typeSafeEqualTo(f));
  }

  public static <T> Match<T, OptionalDouble> matchOptionalDouble(
      Function<T, OptionalDouble> fieldExtractor, double epsilon) {
    return new Match<T, OptionalDouble>(fieldExtractor, f -> optionalDoubleMatcher(f, epsilon));
  }

  public static <T> Match<T, OptionalInt> matchOptionalInt(
      Function<T, OptionalInt> fieldExtractor) {
    return new Match<T, OptionalInt>(fieldExtractor, f -> optionalIntMatcher(f));
  }

  public static <T, V extends PreciseValue> Match<T, Optional<V>> matchOptionalPreciseValue(
      Function<T, Optional<V>> fieldExtractor, double epsilon) {
    return match(fieldExtractor, f -> optionalMatcher(f, f2 -> preciseValueMatcher(f2, epsilon)));
  }

  public static <T, F extends Enum<? super F>> Match<T, Optional<F>> matchOptionalEnum(
      Function<T, Optional<F>> fieldExtractor) {
    return matchOptional(fieldExtractor, f -> enumMatcher(f));
  }

  public static <T, V extends ImpreciseValue<V>> Match<T, Optional<V>> matchOptionalImpreciseValue(
      Function<T, Optional<V>> fieldExtractor, double epsilon) {
    return match(fieldExtractor, f -> optionalMatcher(f, f2 -> impreciseValueMatcher(f2, epsilon)));
  }

  public static <T, F> Match<T, F> matchUsingEquals(Function<T, F> fieldExtractor) {
    return match(fieldExtractor, f -> typeSafeEqualTo(f));
  }

  public static <T, F extends PreciseValue<F>> Match<T, F> matchUsingAlmostEquals(
      Function<T, F> fieldExtractor, double epsilon) {
    return match(fieldExtractor, f -> preciseValueMatcher(f, epsilon));
  }

  public static <T, F extends ImpreciseValue<F>> Match<T, F> matchUsingImpreciseAlmostEquals(
      Function<T, F> fieldExtractor, double epsilon) {
    return match(fieldExtractor, f -> impreciseValueMatcher(f, epsilon));
  }

  public static <T, F extends IntegerValue<F>> Match<T, F> matchIntegerValue(
      Function<T, F> fieldExtractor) {
    return match(fieldExtractor, f -> integerValueMatcher(f));
  }

  public static <T> Match<T, Double> matchUsingDoubleAlmostEquals(
      Function<T, Double> fieldExtractor, double epsilon) {
    return match(fieldExtractor, f -> doubleAlmostEqualsMatcher(f, epsilon));
  }

  public static <T, V> Match<T, List<V>> matchList(
      Function<T, List<V>> listFieldExtractor, MatcherGenerator<V> listItemMatcherGenerator) {
    return match(listFieldExtractor, f -> orderedListMatcher(f, listItemMatcherGenerator));
  }

  public static <T> Match<T, List<Double>> matchDoubleList(
      Function<T, List<Double>> listFieldExtractor, double epsilon) {
    return match(listFieldExtractor, f -> orderedListMatcher(f, f2 -> doubleAlmostEqualsMatcher(f2, epsilon)));
  }

  public static <T, V> Match<T, List<V>> matchListUsingEquals(
      Function<T, List<V>> listFieldExtractor) {
    return matchList(listFieldExtractor, f -> typeSafeEqualTo(f));
  }

  public static <T> Match<T, IidSet> matchIidSet(Function<T, IidSet> fieldExtractor) {
    return match(fieldExtractor, f -> iidSetMatcher(f));
  }

  public static <T, V extends PreciseValue<V>> Match<T, List<V>> matchListUsingAlmostEquals(
      Function<T, List<V>> listFieldExtractor, double epsilon) {
    return matchList(listFieldExtractor, f -> preciseValueMatcher(f, epsilon));
  }

  public static <T, V> Match<T, V[]> matchArrayUsingEquals(
      Function<T, V[]> arrayFieldExtractor) {
    return match(arrayFieldExtractor, f -> arrayMatcher(f, f2 -> typeSafeEqualTo(f2)));
  }

  public static <T> Match<T, int[]> matchIntArray(
      Function<T, int[]> arrayFieldExtractor) {
    return match(arrayFieldExtractor, f -> intArrayMatcher(f));
  }

  public static <T> Match<T, Class<T>> matchClass(
      Function<T, T> objectExtractor) {
    return match(v -> (Class<T>) objectExtractor.apply(v).getClass(), f -> classMatcher(f));
  }

  public boolean matches(T expected, T actual) {
    boolean result = matcherGenerator.apply(fieldExtractor.apply(expected)).matches(fieldExtractor.apply(actual));
    return result;
  }

}
