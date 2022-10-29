package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionModification.PartitionModificationBuilder;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.PartitionModification.emptyPartitionModification;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class PartitionModificationTest extends RBTestMatcher<PartitionModification<String>> {

  @Test
  public void totalToIncreaseMustEqualTotalToDecrease() {
    PartitionModification<String> doesNotThrow;
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
    QuadriFunction<String, String, String, String, PartitionModification<String>> maker =
        (keyToAdd, keyToIncrease, keyToRemove, keyToDecrease) ->
            PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToAdd(singletonRBMap(
            keyToAdd, unitFraction(0.1)))
        .setKeysToIncrease(singletonRBMap(
            keyToIncrease, unitFraction(0.7)))
        .setKeysToRemove(singletonRBMap(
            keyToRemove, unitFraction(0.2)))
        .setKeysToDecrease(singletonRBMap(
            keyToDecrease, unitFraction(0.6)))
        .build();

    // Some of these cases are not unique, but it's clearer this way.
    PartitionModification<String> doesNotThrow = maker.apply("a", "i", "r", "d");
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
    PartitionModification<String> doesNotThrow = makeUsingAddIncreaseRemoveDecrease(0.1, 0.7, 0.2, 0.6);
    // 0.1 + 0.33 + 0.7 > 1 = 100%, so this is invalid.
    // Likewise for 0.2 + 0.33 + 0.6.
    assertIllegalArgumentException( () -> makeUsingAddIncreaseRemoveDecrease(0.1 + 0.33, 0.7, 0.2 + 0.33, 0.6));
  }

  private PartitionModification<String> makeUsingAddIncreaseRemoveDecrease(
      double toAdd, double toIncrease, double toRemove, double toDecrease) {
    return PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToAdd(singletonRBMap(
            "a", unitFraction(toAdd)))
        .setKeysToIncrease(singletonRBMap(
            "i", unitFraction(toIncrease)))
        .setKeysToRemove(singletonRBMap(
            "r", unitFraction(toRemove)))
        .setKeysToDecrease(singletonRBMap(
            "d", unitFraction(toDecrease)))
        .build();
  }

  @Override
  public PartitionModification<String> makeTrivialObject() {
    return emptyPartitionModification();
  }

  @Override
  public PartitionModification<String> makeNontrivialObject() {
    return PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToAdd(rbMapOf(
            "a1", unitFraction(0.01),
            "a2", unitFraction(0.02)))
        .setKeysToIncrease(rbMapOf(
            "i1", unitFraction(0.07),
            "i2", unitFraction(0.08)))
        .setKeysToRemove(rbMapOf(
            "r1", unitFraction(0.03),
            "r2", unitFraction(0.04)))
        .setKeysToDecrease(rbMapOf(
            "d1", unitFraction(0.05),
            "d2", unitFraction(0.06)))
        .build();
  }

  @Override
  public PartitionModification<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToAdd(rbMapOf(
            "a1", unitFraction(0.01 + e),
            "a2", unitFraction(0.02 + e)))
        .setKeysToIncrease(rbMapOf(
            "i1", unitFraction(0.07 + e),
            "i2", unitFraction(0.08 + e)))
        .setKeysToRemove(rbMapOf(
            "r1", unitFraction(0.03 + e),
            "r2", unitFraction(0.04 + e)))
        .setKeysToDecrease(rbMapOf(
            "d1", unitFraction(0.05 + e),
            "d2", unitFraction(0.06 + e)))
        .build();
  }

  @Override
  protected boolean willMatch(PartitionModification<String> expected, PartitionModification<String> actual) {
    return partitionModificationMatcher(expected).matches(actual);
  }

  public static <T> TypeSafeMatcher<PartitionModification<T>> partitionModificationMatcher(
      PartitionModification<T> expected) {
    return epsilonPartitionModificationMatcher(expected, 1e-8);
  }

  public static <T> TypeSafeMatcher<PartitionModification<T>> epsilonPartitionModificationMatcher(
      PartitionModification<T> expected, double epsilon) {
    // Here, we won't use the usual makeMatcher approach, because we want to be able to print the partitionModification
    // fraction at a high precision, whereas the default toString() only prints round percentages.
    return new TypeSafeMatcher<PartitionModification<T>>() {
      @Override
      protected boolean matchesSafely(PartitionModification<T> actual) {
        return makeMatcher(expected,
            match(v -> v.getKeysToAdd(),      f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(v -> v.getKeysToIncrease(), f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(v -> v.getKeysToRemove(),   f -> rbMapPreciseValueMatcher(f, epsilon)),
            match(v -> v.getKeysToDecrease(), f -> rbMapPreciseValueMatcher(f, epsilon)))
            .matches(actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected partitionModification: %s", expected.toString(8)));
      }
    };
  }

}
