package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableSet;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.types.ClosedSignedFractionRange;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.function.Function;

import static com.rb.nonbiz.collections.ClosedSignedFractionRanges.closedSignedFractionRanges;
import static com.rb.nonbiz.collections.ClosedSignedFractionRanges.emptyClosedSignedFractionRanges;
import static com.rb.nonbiz.collections.ClosedSignedFractionRanges.nonEmptyClosedSignedFractionRanges;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.Match.matchRBMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.types.ClosedSignedFractionRange.closedSignedFractionRange;
import static com.rb.nonbiz.types.ClosedSignedFractionRangeTest.closedSignedFractionRangeMatcher;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_0;
import static com.rb.nonbiz.types.SignedFraction.SIGNED_FRACTION_1;
import static com.rb.nonbiz.types.SignedFraction.signedFraction;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

// This test class is not generic, but the publicly exposed static matcher is
public class ClosedSignedFractionRangesTest extends RBTestMatcher<ClosedSignedFractionRanges<String>> {

  @Test
  public void testGetters() {
    ClosedSignedFractionRanges<String> closedSignedFractionRanges = makeNontrivialObject();

    assertEquals(3, closedSignedFractionRanges.keySet().size());
    assertEquals(3, closedSignedFractionRanges.entrySet().size());
    assertEquals(3, closedSignedFractionRanges.getRawMap().size());

    assertEquals(
        ImmutableSet.of("a", "b", "c"),
        closedSignedFractionRanges.keySet());

    assertThat(
        closedSignedFractionRanges.getClosedSignedFractionRange("a"),
        nonEmptyOptionalMatcher(
            closedSignedFractionRangeMatcher(closedSignedFractionRange(SIGNED_FRACTION_0, SIGNED_FRACTION_1))));

    assertThat(
        closedSignedFractionRanges.getRawMap(),
        rbMapMatcher(
            rbMapOf(
                "a", closedSignedFractionRange(SIGNED_FRACTION_0,    SIGNED_FRACTION_1),
                "b", closedSignedFractionRange(signedFraction(0.1),  signedFraction(0.9)),
                "c", closedSignedFractionRange(signedFraction(-2.2), signedFraction(3.3))),
            v -> closedSignedFractionRangeMatcher(v)));
  }

  @Test
  public void testEmptyConstructor() {
    assertEquals(0, emptyClosedSignedFractionRanges().keySet().size());
    assertEquals(0, emptyClosedSignedFractionRanges().getRawMap().size());
    assertEquals(0, emptyClosedSignedFractionRanges().entrySet().size());
    assertOptionalEmpty(emptyClosedSignedFractionRanges().getClosedSignedFractionRange("a"));
  }

  @Test
  public void testNonEmptyConstructor() {
    Function<RBMap<String, ClosedSignedFractionRange>, ClosedSignedFractionRanges> maker = rbMap ->
        nonEmptyClosedSignedFractionRanges(rbMap);

    ClosedSignedFractionRanges doesNotThrow = maker.apply(
        singletonRBMap("A", closedSignedFractionRange(SIGNED_FRACTION_0, SIGNED_FRACTION_1)));

    assertIllegalArgumentException( () -> maker.apply(emptyRBMap()));
  }

  @Override
  public ClosedSignedFractionRanges<String> makeTrivialObject() {
    return emptyClosedSignedFractionRanges();
  }

  @Override
  public ClosedSignedFractionRanges<String> makeNontrivialObject() {
    return closedSignedFractionRanges(rbMapOf(
        "a", closedSignedFractionRange(SIGNED_FRACTION_0, SIGNED_FRACTION_1),
        "b", closedSignedFractionRange(signedFraction(0.1), signedFraction(0.9)),
        "c", closedSignedFractionRange(signedFraction(-2.2), signedFraction(3.3))));
  }

  @Override
  public ClosedSignedFractionRanges<String> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return closedSignedFractionRanges(rbMapOf(
        "c", closedSignedFractionRange(signedFraction(-2.2 - e), signedFraction(3.3 + e)),
        "b", closedSignedFractionRange(signedFraction(0.1 - e), signedFraction(0.9 + e)),
        "a", closedSignedFractionRange(signedFraction(e), signedFraction(1 - e))));
  }

  @Override
  protected boolean willMatch(ClosedSignedFractionRanges<String> expected, ClosedSignedFractionRanges<String> actual) {
    return closedSignedFractionRangesMatcher(expected).matches(actual);
  }

  public static <K> TypeSafeMatcher<ClosedSignedFractionRanges<K>> closedSignedFractionRangesMatcher(
      ClosedSignedFractionRanges<K> expected) {
    return makeMatcher(expected,
        matchRBMap(v -> v.getRawMap(), f -> closedSignedFractionRangeMatcher(f)));
  }

}
