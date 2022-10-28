package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.PartitionModification.PartitionModificationBuilder;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.PartitionModification.emptyPartitionModification;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapPreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;

public class PartitionModificationTest extends RBTestMatcher<PartitionModification<String>> {

  @Test
  public void totalToIncreaseMustEqualTotalToDecrease() {
    Function<UnitFraction, PartitionModification<String>> maker = decreaseInB ->
        PartitionModificationBuilder.<String>partitionModificationBuilder()
            .setKeysToIncrease(singletonRBMap(
                "a", unitFraction(0.4)))
            .setKeysToDecrease(rbMapOf(
                "b", decreaseInB,
                "c", unitFraction(0.1)))
            .build();

    PartitionModification<String> doesNotThrow;
    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.3 - 1e-7)));
    doesNotThrow = maker.apply(unitFraction(0.3 - 1e-9));
    doesNotThrow = maker.apply(unitFraction(0.3));
    doesNotThrow = maker.apply(unitFraction(0.3 + 1e-9));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(0.3 + 1e-7)));
  }

  @Test
  public void noZerosAllowed() {
    Function<UnitFraction, PartitionModification<String>> maker = increaseAndDecreaseFraction ->
        PartitionModificationBuilder.<String>partitionModificationBuilder()
            .setKeysToIncrease(singletonRBMap(
                "a", increaseAndDecreaseFraction))
            .setKeysToDecrease(singletonRBMap(
                "b", increaseAndDecreaseFraction))
            .build();

    assertIllegalArgumentException( () -> maker.apply(UNIT_FRACTION_0));
    assertIllegalArgumentException( () -> maker.apply(unitFraction(1e-9)));
    PartitionModification<String> doesNotThrow;
    doesNotThrow = maker.apply(unitFraction(1e-7));
    doesNotThrow = maker.apply(unitFraction(0.1234));
  }

  @Test
  public void cannotIncreaseAndDecrease() {
    Function<String, PartitionModification<String>> maker = keyToDecrease ->
        PartitionModificationBuilder.<String>partitionModificationBuilder()
            .setKeysToIncrease(singletonRBMap(
                "a", unitFraction(0.1234)))
            .setKeysToDecrease(singletonRBMap(
                keyToDecrease, unitFraction(0.1234)))
            .build();

    assertIllegalArgumentException( () -> maker.apply("a"));
    PartitionModification<String> doesNotThrow = maker.apply("b");
  }

  @Override
  public PartitionModification<String> makeTrivialObject() {
    return emptyPartitionModification();
  }

  @Override
  public PartitionModification<String> makeNontrivialObject() {
    return PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToIncrease(singletonRBMap(
            "a", unitFraction(0.4)))
        .setKeysToDecrease(rbMapOf(
            "b", unitFraction(0.3),
            "c", unitFraction(0.1)))
        .build();
  }

  @Override
  public PartitionModification<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return PartitionModificationBuilder.<String>partitionModificationBuilder()
        .setKeysToIncrease(singletonRBMap(
            "a", unitFraction(0.4 + e)))
        .setKeysToDecrease(rbMapOf(
            "b", unitFraction(0.3 + e),
            "c", unitFraction(0.1 + e)))
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
        return rbMapPreciseValueMatcher(expected.getKeysToIncrease(), epsilon)
            .matches(actual.getKeysToIncrease())
            && rbMapPreciseValueMatcher(expected.getKeysToDecrease(), epsilon)
            .matches(actual.getKeysToDecrease());
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(Strings.format("Expected partitionModification: %s", expected.toString(8)));
      }
    };
  }

}
