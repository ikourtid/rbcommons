package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.DetailedPartitionModification.DetailedPartitionModificationBuilder;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Epsilon;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.DetailedPartitionModification.emptyDetailedPartitionModification;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalPreciseValue;
import static com.rb.nonbiz.testmatchers.Match.matchUsingAlmostEquals;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class DetailedPartitionModificationTest extends RBTestMatcher<DetailedPartitionModification<String>> {

  // Unlike other test*WithSeed methods, this is less useful, because it only works for String keys.
  public static DetailedPartitionModification<String> testDetailedStringPartitionModificationWithSeed(double seed) {
    return DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
        .setKeysToAdd(rbMapOf(
            "a1", unitFraction(0.01 + seed),
            "a2", unitFraction(0.02 + seed)))
        .setKeysToIncrease(rbMapOf(
            "i1", unitFraction(0.07 + seed),
            "i2", unitFraction(0.08 + seed)))
        .setKeysToRemove(rbMapOf(
            "r1", unitFraction(0.03 + seed),
            "r2", unitFraction(0.04 + seed)))
        .setKeysToDecrease(rbMapOf(
            "d1", unitFraction(0.05 + seed),
            "d2", unitFraction(0.06 + seed)))
        // We start with 1e-8 so that a seed of 0 will still result in a valid object.
        .setEpsilonForRemovalSanityChecks(unitFraction(1e-8 + seed))
        .setEpsilonForNetAdditionSanityCheck(unitFraction(1e-8 + seed))
        .build();
  }

  @Test
  public void totalToIncreaseMustEqualTotalToDecrease() {
    DetailedPartitionModification<String> doesNotThrow;
    // Below, 0.1 + 0.7 = 0.2 + 0.6
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6 - 1e-7));
    doesNotThrow                        = makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6 - 1e-9);
    doesNotThrow                        = makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6);
    doesNotThrow                        = makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6 + 1e-9);
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6 + 1e-7));
  }

  @Test
  public void noZerosAllowed() {
    // Using numbers that would otherwise not result in triggering the exception in the previous test.
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1, 0.8, 0.9, 0.0));
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1, 0.8, 0.0, 0.9));
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.9, 0.0, 0.1, 0.8));
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.0, 0.9, 0.1, 0.8));
  }

  @Test
  public void cannotRepeatKeys() {
    QuadriFunction<String, String, String, String, DetailedPartitionModification<String>> maker =
        (keyToAdd, keyToIncrease, keyToRemove, keyToDecrease) ->
            DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
                .setKeysToAdd(singletonRBMap(
                    keyToAdd, unitFraction(0.1)))
                .setKeysToIncrease(singletonRBMap(
                    keyToIncrease, unitFraction(0.7)))
                .setKeysToRemove(singletonRBMap(
                    keyToRemove, unitFraction(0.2)))
                .setKeysToDecrease(singletonRBMap(
                    keyToDecrease, unitFraction(0.6)))
                .useStandardEpsilonForRemovalSanityChecks()
                .useStandardEpsilonForNetAdditionSanityCheck()
                .build();

    // Some of these cases are not unique, but it's clearer this way.
    DetailedPartitionModification<String> doesNotThrow = maker.apply("a", "i", "r", "d");
    assertIllegalArgumentException( () -> maker.apply("a", "a", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "a", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "r", "a"));

    assertIllegalArgumentException( () -> maker.apply("i", "i", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "i", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "r", "i"));

    assertIllegalArgumentException( () -> maker.apply("r", "i", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "r", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "r", "r"));

    assertIllegalArgumentException( () -> maker.apply("d", "i", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "d", "r", "d"));
    assertIllegalArgumentException( () -> maker.apply("a", "i", "d", "d"));
  }

  @Test
  public void totalIncreaseOrDecreaseCannotIncrease100pct() {
    // Increasing by 0.1 + 0.7; decreasing by 0.2 + 0.6
    DetailedPartitionModification<String> doesNotThrow = makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6);
    // 0.1 + 0.33 + 0.7 > 1 = 100%, so this is invalid.
    // Likewise for 0.2 + 0.33 + 0.6.
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1 + 0.33, 0.7, 0.2 + 0.33, 0.6));
  }

  private DetailedPartitionModification<String> makeUsingAddIncreaseRemoveDecrease(
      double toAdd, double toIncrease, double toRemove, double toDecrease) {
    return DetailedPartitionModificationBuilder.<String>detailedPartitionModificationBuilder()
        .setKeysToAdd(singletonRBMap(
            "a", unitFraction(toAdd)))
        .setKeysToIncrease(singletonRBMap(
            "i", unitFraction(toIncrease)))
        .setKeysToRemove(singletonRBMap(
            "r", unitFraction(toRemove)))
        .setKeysToDecrease(singletonRBMap(
            "d", unitFraction(toDecrease)))
        .useStandardEpsilonForRemovalSanityChecks()
        .useStandardEpsilonForNetAdditionSanityCheck()
        .build();
  }

  @Override
  public DetailedPartitionModification<String> makeTrivialObject() {
    return emptyDetailedPartitionModification();
  }

  @Override
  public DetailedPartitionModification<String> makeNontrivialObject() {
    return testDetailedStringPartitionModificationWithSeed(ZERO_SEED);
  }

  @Override
  public DetailedPartitionModification<String> makeMatchingNontrivialObject() {
    // This is a special case; please see comments inside epsilonDetailedPartitionModificationMatcher on why
    // we do not use the usual EPSILON_SEED of 1e-9.
    return testDetailedStringPartitionModificationWithSeed(1e-15);
  }

  @Override
  protected boolean willMatch(DetailedPartitionModification<String> expected, DetailedPartitionModification<String> actual) {
    return detailedPartitionModificationMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<DetailedPartitionModification<T>> detailedPartitionModificationMatcher(
      DetailedPartitionModification<T> expected) {
    return epsilonDetailedPartitionModificationMatcher(expected, DEFAULT_EPSILON_1e_8);
  }

  public static <T> TypeSafeMatcher<DetailedPartitionModification<T>> epsilonDetailedPartitionModificationMatcher(
      DetailedPartitionModification<T> expected, Epsilon epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print the partition modification
    // fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<DetailedPartitionModification<T>>() {
      @Override
      protected boolean matchesSafely(DetailedPartitionModification<T> actual) {
        return makeMatcher(expected,
            match(                 v -> v.getKeysToAdd(),      f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(                 v -> v.getKeysToIncrease(), f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(                 v -> v.getKeysToRemove(),   f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(                 v -> v.getKeysToDecrease(), f -> rbMapPreciseValueMatcher(f, epsilon)),
            // The epsilons themselves should be compared down to a much smaller epsilon, because they are small
            // themselves (1e-8 by default), so we can't use such a large epsilon to compare them; it will be too
            // permissive. Note also that epsilons are specified explicitly in the code, i.e. they appear as double
            // numberic literals; they are almost never the result of some computation that can be off by a tiny bit.
            // So in theory we could even use a double equality comparison, although we avoid that everywhere in the
            // code because it's rarely correct - even if in this case it actually is correct. It's just a bad habit
            // to get into.
            matchUsingAlmostEquals(   v -> v.getEpsilonForRemovalSanityChecks(),    epsilon(1e-14)),
            matchOptionalPreciseValue(v -> v.getEpsilonForNetAdditionSanityCheck(), epsilon(1e-14)))
            .matches(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected DetailedPartitionModification: %s", expected.toString(8)));
      }
    };
  }

}
