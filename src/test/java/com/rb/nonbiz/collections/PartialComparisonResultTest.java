package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PartialComparisonResult.definedPartialComparison;
import static com.rb.nonbiz.collections.PartialComparisonResult.noOrderingDefined;
import static com.rb.nonbiz.collections.PartialComparisonResult.orderingDefined;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.optionalIntMatcher;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartialComparisonResultTest extends RBTestMatcher<PartialComparisonResult> {

  @Test
  public void testConstructorWithLambda() {
    assertThat(
        definedPartialComparison(pair(1, "B"), pair(2, "A"), p -> p.getLeft(), Integer::compare),
        partialComparisonResultMatcher(PartialComparisonResult.lessThan()));
    assertThat(
        definedPartialComparison(pair(1, "B"), pair(2, "A"), p -> p.getRight(), String::compareTo),
        partialComparisonResultMatcher(PartialComparisonResult.greaterThan()));
    assertThat(
        definedPartialComparison(pair(1, "B"), pair(2, "A"), p -> p.getRight(), (v1, v2) -> 0),
        partialComparisonResultMatcher(PartialComparisonResult.equal()));
  }

  @Test
  public void testBooleanPredicates() {
    // Using 2-char shorthands to make it easier to read this table
    PartialComparisonResult eq = PartialComparisonResult.equal();
    PartialComparisonResult lt = PartialComparisonResult.lessThan();
    PartialComparisonResult gt = PartialComparisonResult.greaterThan();
    PartialComparisonResult na = PartialComparisonResult.noOrderingDefined();

    rbSetOf(eq, lt, gt).forEach(v -> assertTrue(v.isDefined()));
    assertFalse(na.isDefined());

    assertTrue(eq.isEqual());
    assertTrue(lt.isLessThan());
    assertTrue(lt.isLessThanOrEqualTo());
    assertTrue(gt.isGreaterThan());
    assertTrue(gt.isGreaterThanOrEqualTo());

    rbSetOf(lt, gt, na).forEach(v -> assertFalse(v.isEqual()));
    rbSetOf(eq, gt, na).forEach(v -> assertFalse(v.isLessThan()));
    rbSetOf(    gt, na).forEach(v -> assertFalse(v.isLessThanOrEqualTo()));
    rbSetOf(lt, eq, na).forEach(v -> assertFalse(v.isGreaterThan()));
    rbSetOf(lt,     na).forEach(v -> assertFalse(v.isGreaterThanOrEqualTo()));
  }

  @Override
  public PartialComparisonResult makeTrivialObject() {
    return noOrderingDefined();
  }

  @Override
  public PartialComparisonResult makeNontrivialObject() {
    // Typically the result of a comparison is -1, 0, or 1 (as per Comparator / Comparable), but that's not a requirement.
    return orderingDefined(-123);
  }

  @Override
  public PartialComparisonResult makeMatchingNontrivialObject() {
    return orderingDefined(-123);
  }

  @Override
  protected boolean willMatch(PartialComparisonResult expected, PartialComparisonResult actual) {
    return partialComparisonResultMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<PartialComparisonResult> partialComparisonResultMatcher(
      PartialComparisonResult expected) {
    // Strictly speaking, since less-than & greater-than don't *have* to be represented with -1 and 1
    // (e.g. we could have -22 and +33), then matching the exact value is too restrictive.
    // In practice, though, this should never be a problem.
    return makeMatcher(expected,
        match(v -> v.getRawResult(), f -> optionalIntMatcher(f)));
  }

}
