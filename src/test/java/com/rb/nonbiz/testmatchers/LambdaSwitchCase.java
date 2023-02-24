package com.rb.nonbiz.testmatchers;

import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.collections.RBLists.concatenateFirstAndRest;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;

/**
 * This is useful for matching objects that can belong to more than one type.
 *
 * In our codebase, we usually use visitors and generalVisitorMatcher to do this sort of thing.
 * However, we can't do this for classes that we did not write.
 *
 * It's harder to explain this here than it is to look at an example of where and how {@link #lambdaSwitchMatcher}
 * gets used.
 */
public class LambdaSwitchCase<T, V> {

  private final Predicate<T> isTypeV;
  private final Function<T, V> convertToTypeV;
  private final MatcherGenerator<V> matcherGenerator;

  private LambdaSwitchCase(Predicate<T> isTypeV, Function<T, V> convertToTypeV, MatcherGenerator<V> matcherGenerator) {
    this.isTypeV = isTypeV;
    this.convertToTypeV = convertToTypeV;
    this.matcherGenerator = matcherGenerator;
  }

  public static <T, V> LambdaSwitchCase<T, V> lambdaCase(
      Predicate<T> isTypeV,
      Function<T, V> convertToTypeV,
      MatcherGenerator<V> matcherGenerator) {
    return new LambdaSwitchCase<>(isTypeV, convertToTypeV, matcherGenerator);
  }

  public boolean isTypeV(T object) {
    return isTypeV.test(object);
  }

  public V convertToTypeV(T object) {
    return convertToTypeV.apply(object);
  }

  public boolean objectsMatch(T object1, T object2) {
    V v1 = convertToTypeV(object1);
    V v2 = convertToTypeV(object2);
    boolean matches = matcherGenerator.apply(v1).matches(v2);
    if (!matches) {
      int dummy = 0; // this is convenient for putting in breakpoints
    }
    return matches;
  }

  @SafeVarargs
  public static <T> TypeSafeMatcher<T> lambdaSwitchMatcher(
      T expected,
      LambdaSwitchCase<T, ?> first,
      LambdaSwitchCase<T, ?>... rest) {
    return makeMatcher(expected, actual -> {
      List<LambdaSwitchCase<T, ?>> lambdaSwitchCases = concatenateFirstAndRest(first, rest);
      // We could do this with an 'enhanced for' loop, but knowing i is useful if we ever want to use breakpoints
      // here to debug a matcher.
      for (int i = 0; i < lambdaSwitchCases.size(); i++) {
        LambdaSwitchCase<T, ?> singleCase = lambdaSwitchCases.get(i);
        if (singleCase.isTypeV(expected)) {
          if (!singleCase.isTypeV(actual)) {
            return false; // not of same type
          }
          return singleCase.objectsMatch(expected, actual);
        }
      }

      throw new IllegalArgumentException(smartFormat(
          "Unknown case of expected= %s , actual= %s", expected, actual));
    });
  }

}
