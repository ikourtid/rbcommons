package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.List;
import java.util.OptionalInt;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.RBListWithSpecialValue.emptyRBListWithSpecialValue;
import static com.rb.nonbiz.collections.RBListWithSpecialValue.isSpecialValue;
import static com.rb.nonbiz.collections.RBListWithSpecialValue.rbListWithSpecialValue;
import static com.rb.nonbiz.collections.RBListWithSpecialValue.rbListWithSpecialValueSpecified;
import static com.rb.nonbiz.collections.RBListWithSpecialValue.singletonRBListWithoutSpecialValue;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.matchList;
import static com.rb.nonbiz.testmatchers.Match.matchOptionalInt;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test class is not generic, but the publicly exposed matcher is.
 */
public class RBListWithSpecialValueTest extends RBTestMatcher<RBListWithSpecialValue<Double>> {

  @Test
  public void testExplicitConstructorThatSpecifiesNumericIndex() {
    rbSetOf(OptionalInt.empty(), OptionalInt.of(0)).forEach(v -> {
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList(isSpecialValue())));
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList("a", isSpecialValue())));
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList(isSpecialValue(), "a")));
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList(isSpecialValue(), "a", "b")));
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList("a", isSpecialValue(), "b")));
      assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(v, makeList("a", "b", isSpecialValue())));
    });

    RBListWithSpecialValue<String> doesNotThrow;
    doesNotThrow = rbListWithSpecialValueSpecified(OptionalInt.of(0), singletonList("a"));
    doesNotThrow = rbListWithSpecialValueSpecified(OptionalInt.of(1), ImmutableList.of("a", "b"));
    doesNotThrow = rbListWithSpecialValueSpecified(OptionalInt.of(2), ImmutableList.of("a", "b", "c"));
    assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(OptionalInt.of(1), singletonList("a")));
    assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(OptionalInt.of(2), ImmutableList.of("a", "b")));
    assertIllegalArgumentException( () -> rbListWithSpecialValueSpecified(OptionalInt.of(3), ImmutableList.of("a", "b", "c")));
  }

  @Test
  public void testVarArgsConstructor_threeItems() {
    assertVarArgsConstructor(ImmutableList.of("a", "b", "c"), OptionalInt.empty(), "a", "b", "c");
    assertVarArgsConstructor(ImmutableList.of("a", "b", "c"), OptionalInt.of(0), "a", isSpecialValue(), "b", "c");
    assertVarArgsConstructor(ImmutableList.of("a", "b", "c"), OptionalInt.of(1), "a", "b", isSpecialValue(), "c");
    assertVarArgsConstructor(ImmutableList.of("a", "b", "c"), OptionalInt.of(2), "a", "b", "c", isSpecialValue());
    // isSpecialValue must follow whatever value has been designated as special
    assertIllegalArgumentException( () -> rbListWithSpecialValue(isSpecialValue(), "a", "b"));

    // More than one special value is bad
    String spv = isSpecialValue();
    assertIllegalArgumentException( () -> rbListWithSpecialValue("a", spv, "b", spv, "c"));
    assertIllegalArgumentException( () -> rbListWithSpecialValue("a", spv, "b", "c", spv));
    assertIllegalArgumentException( () -> rbListWithSpecialValue("a", "b", spv, "c", spv));
    assertIllegalArgumentException( () -> rbListWithSpecialValue("a", spv, "b", spv, "c", spv));
  }

  @Test
  public void testHandyConstructor_twoItems() {
    assertVarArgsConstructor(ImmutableList.of("a", "b"), OptionalInt.empty(), "a", "b");
    assertVarArgsConstructor(ImmutableList.of("a", "b"), OptionalInt.of(0), "a", isSpecialValue(), "b");
    assertVarArgsConstructor(ImmutableList.of("a", "b"), OptionalInt.of(1), "a", "b", isSpecialValue());
    // isSpecialValue must follow whatever value has been designated as special
    assertIllegalArgumentException( () -> rbListWithSpecialValue(isSpecialValue(), "a", "b"));

    // More than one special value is bad
    assertIllegalArgumentException( () -> rbListWithSpecialValue("a", isSpecialValue(), "b", isSpecialValue()));
  }

  @Test
  public void testHandyConstructor_oneItem() {
    assertThat(
        singletonRBListWithoutSpecialValue("a"),
        rbListWithSpecialValueEqualityMatcher(
            rbListWithSpecialValueSpecified(OptionalInt.empty(), singletonList("a"))));
    assertVarArgsConstructor(singletonList("a"), OptionalInt.of(0), "a", isSpecialValue());
    // isSpecialValue must follow whatever value has been designated as special
    assertIllegalArgumentException( () -> rbListWithSpecialValue(isSpecialValue(), "a"));
  }

  private void assertVarArgsConstructor(List<String> nonNulls, OptionalInt indexOfSpecialItem, String first, String second, String ... rest) {
    assertThat(
        rbListWithSpecialValue(first, second, rest),
        rbListWithSpecialValueEqualityMatcher(
            rbListWithSpecialValueSpecified(indexOfSpecialItem, nonNulls)));
  }

  // We need this because ImmutableList.of(...) does not allow nulls, and our test uses them.
  private List<String> makeList(String onlyItem) {
    List<String> list = newArrayList();
    list.add(onlyItem);
    return list;
  }

  // We need this because ImmutableList.of(...) does not allow nulls, and our test uses them.
  private List<String> makeList(String first, String second) {
    List<String> list = newArrayList();
    list.add(first);
    list.add(second);
    return list;
  }

  // We need this because ImmutableList.of(...) does not allow nulls, and our test uses them.
  private List<String> makeList(String first, String second, String third) {
    List<String> list = newArrayList();
    list.add(first);
    list.add(second);
    list.add(third);
    return list;
  }

  @Override
  public RBListWithSpecialValue<Double> makeTrivialObject() {
    return emptyRBListWithSpecialValue();
  }

  @Override
  public RBListWithSpecialValue<Double> makeNontrivialObject() {
    return rbListWithSpecialValue(7.0, 7.1, null, 7.2);
  }

  @Override
  public RBListWithSpecialValue<Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return rbListWithSpecialValue(7.0 + e, 7.1 + e, null, 7.2 + e);
  }

  @Override
  protected boolean willMatch(RBListWithSpecialValue<Double> expected, RBListWithSpecialValue<Double> actual) {
    return rbListWithSpecialValueMatcher(expected, f -> doubleAlmostEqualsMatcher(f, 1e-8)).matches(actual);
  }

  public static <T> TypeSafeMatcher<RBListWithSpecialValue<T>> rbListWithSpecialValueEqualityMatcher(
      RBListWithSpecialValue<T> expected) {
    return rbListWithSpecialValueMatcher(expected, f -> typeSafeEqualTo(f));
  }
  
  public static <T> TypeSafeMatcher<RBListWithSpecialValue<T>> rbListWithSpecialValueMatcher(
      RBListWithSpecialValue<T> expected, MatcherGenerator<T> matcherGenerator) {
    return makeMatcher(expected,
        matchList(v -> v.getRawList(), matcherGenerator),
        matchOptionalInt(v -> v.getIndexOfSpecialValue()));
  }
  
}
