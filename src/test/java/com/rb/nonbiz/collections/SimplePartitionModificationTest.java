package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.SimplePartitionModification.SimplePartitionModificationBuilder;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.SimplePartitionModification.emptySimplePartitionModification;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.EPSILON_SEED;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.ZERO_SEED;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class SimplePartitionModificationTest extends RBTestMatcher<SimplePartitionModification<String>> {

  // Unlike other test*WithSeed methods, this is less useful, because it only works for String keys.
  public static SimplePartitionModification<String> testDetailedStringPartitionModificationWithSeed(double seed) {
    return SimplePartitionModificationBuilder.<String>simplePartitionModificationBuilder()
        .setKeysToAddOrIncrease(rbMapOf(
            "a1", unitFraction(0.01 + seed),
            "a2", unitFraction(0.02 + seed),
            "i1", unitFraction(0.07 + seed),
            "i2", unitFraction(0.08 + seed)))
        .setKeysToRemoveOrDecrease(rbMapOf(
            "r1", unitFraction(0.03 + seed),
            "r2", unitFraction(0.04 + seed),
            "d1", unitFraction(0.05 + seed),
            "d2", unitFraction(0.06 + seed)))
        .build();
  }

  @Test
  public void totalToIncreaseMustEqualTotalToDecrease() {
    SimplePartitionModification<String> doesNotThrow;
    assertIllegalArgumentException( () -> makeWithAdditionsAndDeletions(0.123, 0.123 - 1e-7));
    doesNotThrow                        = makeWithAdditionsAndDeletions(0.123, 0.123 - 1e-9);
    doesNotThrow                        = makeWithAdditionsAndDeletions(0.123, 0.123);
    doesNotThrow                        = makeWithAdditionsAndDeletions(0.123, 0.123 + 1e-9);
    assertIllegalArgumentException( () -> makeWithAdditionsAndDeletions(0.123, 0.123 + 1e-7));
  }

  @Test
  public void noZerosAllowed() {
    // Using numbers that would otherwise not result in triggering the exception in the previous test.
    assertIllegalArgumentException( () -> makeWithAdditionsAndDeletions(0.0, 0.0));
    assertIllegalArgumentException( () -> makeWithAdditionsAndDeletions(1e-9, 1e-9));
    SimplePartitionModification<String> doesNotThrow = makeWithAdditionsAndDeletions(1e-7, 1e-7);
  }

  @Test
  public void cannotRepeatKeys() {
    Function<String, SimplePartitionModification<String>> maker =
        keyToRemoveOrDecrease ->
            SimplePartitionModificationBuilder.<String>simplePartitionModificationBuilder()
                .setKeysToAddOrIncrease(singletonRBMap(
                    "key", unitFraction(0.1)))
                .setKeysToRemoveOrDecrease(singletonRBMap(
                    keyToRemoveOrDecrease, unitFraction(0.1)))
                .build();

    // Some of these cases are not unique, but it's clearer this way.
    assertIllegalArgumentException( () -> maker.apply("key"));
    SimplePartitionModification<String> doesNotThrow = maker.apply("different_key");
  }

  @Test
  public void totalIncreaseOrDecreaseCannotIncrease100pct() {
    Function<UnitFraction, SimplePartitionModification<String>> maker = weightOnSecondKeys ->
        SimplePartitionModificationBuilder.<String>simplePartitionModificationBuilder()
        .setKeysToAddOrIncrease(rbMapOf(
            "a1", unitFraction(0.4),
            "a2", weightOnSecondKeys))
        .setKeysToRemoveOrDecrease(rbMapOf(
            "r1", unitFraction(0.4),
            "r2", weightOnSecondKeys))
        .build();

    SimplePartitionModification<String> doesNotThrow;
    doesNotThrow = maker.apply(unitFraction(0.6 - 1e-7));
    doesNotThrow = maker.apply(unitFraction(0.6 - 1e-9));
    doesNotThrow = maker.apply(unitFraction(0.6));
    // Although we usually allow for some epsilon in inequalities inside preconditions,
    // in this case having a hard cutoff of exactly 100% is fine, because in practice it should be extremely
    // unlikely to have this happen.
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.6 + 1e-9)));
    // 0.4 + 0.6 + 1e-7 is well above 1 = 100%
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.6 + 1e-7)));
  }

  private SimplePartitionModification<String> makeWithAdditionsAndDeletions(
      double toAddOrIncrease, double toRemoveOrDecrease) {
    return SimplePartitionModificationBuilder.<String>simplePartitionModificationBuilder()
        .setKeysToAddOrIncrease(singletonRBMap(
            "a", unitFraction(toAddOrIncrease)))
        .setKeysToRemoveOrDecrease(singletonRBMap(
            "r", unitFraction(toRemoveOrDecrease)))
        .build();
  }

  @Override
  public SimplePartitionModification<String> makeTrivialObject() {
    return emptySimplePartitionModification();
  }

  @Override
  public SimplePartitionModification<String> makeNontrivialObject() {
    return testDetailedStringPartitionModificationWithSeed(ZERO_SEED);
  }

  @Override
  public SimplePartitionModification<String> makeMatchingNontrivialObject() {
    return testDetailedStringPartitionModificationWithSeed(EPSILON_SEED);
  }

  @Override
  protected boolean willMatch(SimplePartitionModification<String> expected, SimplePartitionModification<String> actual) {
    return simplePartitionModificationMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<SimplePartitionModification<T>> simplePartitionModificationMatcher(
      SimplePartitionModification<T> expected) {
    return epsilonSimplePartitionModificationMatcher(expected, 1e-8);
  }

  public static <T> TypeSafeMatcher<SimplePartitionModification<T>> epsilonSimplePartitionModificationMatcher(
      SimplePartitionModification<T> expected, double epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print the partition modification
    // fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<SimplePartitionModification<T>>() {
      @Override
      protected boolean matchesSafely(SimplePartitionModification<T> actual) {
        return makeMatcher(expected,
            match(v -> v.getKeysToAddOrIncrease(),    f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(v -> v.getKeysToRemoveOrDecrease(), f -> rbMapPreciseValueMatcher(f, epsilon)))
            .matches(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected SimplePartitionModification: %s", expected.toString(8)));
      }
    };
  }

}
