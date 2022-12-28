package com.rb.nonbiz.functional;

import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.RBNumeric;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Optional;
import java.util.function.DoubleFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.functional.RBStringToNumericFunctionThatHandlesMissingValues.RBStringToNumericFunctionThatHandlesMissingValuesBuilder.rbStringToNumericFunctionThatHandlesMissingValuesBuilder;
import static com.rb.nonbiz.testmatchers.Match.matchOptional;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.rbNumericMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_LABEL;
import static com.rb.nonbiz.text.SimpleHumanReadableLabel.label;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;

// This test class is not generic, but the publicly exposed static matcher is
public class RBStringToNumericFunctionThatHandlesMissingValuesTest
    extends RBTestMatcher<RBStringToNumericFunctionThatHandlesMissingValues<Money>> {

  public static <Y extends RBNumeric<? super Y>> RBStringToNumericFunctionThatHandlesMissingValues<Y>
  emptyRBStringToNumericFunctionThatThrowsOnUnknownAndMissingValues(DoubleFunction<Y> instantiator) {
    return rbStringToNumericFunctionThatHandlesMissingValuesBuilder(instantiator)
        .setLabel(DUMMY_LABEL)
        .setStringToValueMap(emptyRBMap())
        .throwOnUnknownString()
        .returnEmptyOnMissingString()
        .build();
  }

  @Test
  public void testWhenThrowsOnMissing() {
    RBStringToNumericFunctionThatHandlesMissingValues<Money> function = rbStringToNumericFunctionThatHandlesMissingValuesBuilder(v -> money(v))
        .setLabel(DUMMY_LABEL)
        .setStringToValueMap(
            rbMapOf(
                "_100", money(100),
                "_200", money(200)))
        .throwOnUnknownString()
        .returnEmptyOnMissingString()
        .build();
    assertOptionalEquals(money(100), function.apply(testAllowsMissingValues(Optional.of("_100"))));
    assertOptionalEquals(money(200), function.apply(testAllowsMissingValues(Optional.of("_200"))));
    assertIllegalArgumentException( () -> function.apply(testAllowsMissingValues(Optional.of("UNKNOWN STRING"))));
    assertOptionalEmpty(function.apply(testAllowsMissingValues(Optional.empty())));
  }

  @Test
  public void testWhenAllowsMissing() {
    RBStringToNumericFunctionThatHandlesMissingValues<Money> function = rbStringToNumericFunctionThatHandlesMissingValuesBuilder(v -> money(v))
        .setLabel(DUMMY_LABEL)
        .setStringToValueMap(
            rbMapOf(
                "_100", money(100),
                "_200", money(200)))
        .useThisForUnknownString(money(333))
        .useThisForMissingString(money(444))
        .build();
    assertOptionalEquals(money(100), function.apply(testAllowsMissingValues(Optional.of("_100"))));
    assertOptionalEquals(money(200), function.apply(testAllowsMissingValues(Optional.of("_200"))));
    assertOptionalEquals(money(333), function.apply(testAllowsMissingValues(Optional.of("UNKNOWN STRING"))));
    assertOptionalEquals(money(444), function.apply(testAllowsMissingValues(Optional.empty())));
  }

  private AllowsMissingValues<String> testAllowsMissingValues(Optional<String> optional) {
    return new AllowsMissingValues<String>() {
      @Override
      public <T> T visit(Visitor<T, String> visitor) {
        return optional.isPresent()
            ? visitor.visitPresentValue(optional.get())
            : visitor.visitMissingValue();
      }
    };
  }

  @Override
  public RBStringToNumericFunctionThatHandlesMissingValues<Money> makeTrivialObject() {
    return emptyRBStringToNumericFunctionThatThrowsOnUnknownAndMissingValues(v -> money(v));
  }

  @Override
  public RBStringToNumericFunctionThatHandlesMissingValues<Money> makeNontrivialObject() {
    return rbStringToNumericFunctionThatHandlesMissingValuesBuilder(v -> money(v))
        .setLabel(label("abc"))
        .setStringToValueMap(
            rbMapOf(
                "_100", money(100),
                "_200", money(200)))
        .useThisForUnknownString(money(333))
        .useThisForMissingString(money(444))
        .build();
  }

  @Override
  public RBStringToNumericFunctionThatHandlesMissingValues<Money> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbStringToNumericFunctionThatHandlesMissingValuesBuilder(v -> money(v))
        .setLabel(label("abc"))
        .setStringToValueMap(
            rbMapOf(
                "_100", money(100 + e),
                "_200", money(200 + e)))
        .useThisForUnknownString(money(333 + e))
        .useThisForMissingString(money(444 + e))
        .build();
  }

  @Override
  protected boolean willMatch(RBStringToNumericFunctionThatHandlesMissingValues<Money> expected,
                              RBStringToNumericFunctionThatHandlesMissingValues<Money> actual) {
    return rbStringToNumericFunctionThatHandlesMissingValuesMatcher(expected).matches(actual);
  }

  public static <Y extends RBNumeric<? super Y>> TypeSafeMatcher<RBStringToNumericFunctionThatHandlesMissingValues<Y>>
  rbStringToNumericFunctionThatHandlesMissingValuesMatcher(
      RBStringToNumericFunctionThatHandlesMissingValues<Y> expected) {
    return makeMatcher(expected,
        matchRBMap(   v -> v.getStringToValueMap(),      f -> rbNumericMatcher(f, DEFAULT_EPSILON_1e_8)),
        matchOptional(v -> v.getValueForUnknownString(), f -> rbNumericMatcher(f, DEFAULT_EPSILON_1e_8)),
        matchOptional(v -> v.getValueForMissingString(), f -> rbNumericMatcher(f, DEFAULT_EPSILON_1e_8)));
  }

}
