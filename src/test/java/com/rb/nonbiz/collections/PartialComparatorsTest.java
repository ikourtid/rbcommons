package com.rb.nonbiz.collections;

import com.rb.nonbiz.functional.TriConsumer;
import org.junit.Test;

import static com.rb.nonbiz.collections.PartialComparators.maxFromPartialComparatorOrThrow;
import static com.rb.nonbiz.collections.PartialComparators.minFromPartialComparatorOrThrow;
import static com.rb.nonbiz.collections.PartialComparators.partiallyCompareMultiple;
import static com.rb.nonbiz.collections.PartialComparisonResultTest.partialComparisonResultMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class PartialComparatorsTest {

  @Test
  public void testPartiallyCompareMultiple() {
    // Using 2-char shorthands to make it easier to read this table
    PartialComparisonResult eq = PartialComparisonResult.equal();
    PartialComparisonResult lt = PartialComparisonResult.lessThan();
    PartialComparisonResult gt = PartialComparisonResult.greaterThan();
    PartialComparisonResult na = PartialComparisonResult.noOrderingDefined();

    TriConsumer<PartialComparisonResult, PartialComparisonResult, PartialComparisonResult> asserter =
        (partialComparison1, partialComparison2, expectedResult) -> {
          assertThat(
              partiallyCompareMultiple(partialComparison1, partialComparison2),
              partialComparisonResultMatcher(expectedResult));
          assertThat(
              partiallyCompareMultiple(partialComparison2, partialComparison1),
              partialComparisonResultMatcher(expectedResult));
        };

    // Let's assume we are comparing item A (with 2 properties a1, a2) and B (b1, b2),
    // and that partialComparison1 is the result of the (partial) comparison of a1 vs b1; likewise for *2.

    // if a1 == b1 and a2 == b2, then A == B
    asserter.accept(eq, eq,  eq);
    // if a1 == b1, then the end result is just the comparison of a2 and b2.
    asserter.accept(eq, lt,  lt);
    asserter.accept(eq, gt,  gt);
    // In particular, if a1 == b1, but a2 and b2 have no ordering, how would we order the entire objects A and B?
    asserter.accept(eq, na,  na);

    // we have A < B if:
    // a1 < b1 and a2 < b2
    // a1 == b1 and a2 < b2
    // a1 < b1 and a2 == b2
    // Likewise for the opposite direction.
    asserter.accept(lt, lt,  lt);
    asserter.accept(lt, eq,  lt);
    asserter.accept(eq, lt,  lt);

    asserter.accept(gt, gt,  gt);
    asserter.accept(gt, eq,  gt);
    asserter.accept(eq, gt,  gt);

    // If the comparisons for 1 and 2 are in the opposite direction, then there's no ordering between A and B.
    asserter.accept(gt, lt, na);
    asserter.accept(lt, gt, na);

    // Finally, if one of the two comparisons says there's no ordering, then we can't order objects A and B.
    asserter.accept(na, eq, na);
    asserter.accept(na, lt, na);
    asserter.accept(na, gt, na);
    asserter.accept(na, na, na);
  }

  @Test
  public void testGetMinMaxOrThrow() {
    PartialComparator<String> partialComparator = (v1, v2) ->
        (v1.equals("") || v2.equals(""))
            ? PartialComparisonResult.noOrderingDefined()
            : PartialComparisonResult.definedPartialComparison(v1.compareTo(v2));

    assertIllegalArgumentException( () -> minFromPartialComparatorOrThrow(partialComparator, "", ""));
    assertIllegalArgumentException( () -> minFromPartialComparatorOrThrow(partialComparator, "a", ""));
    assertIllegalArgumentException( () -> minFromPartialComparatorOrThrow(partialComparator, "", "a"));

    assertIllegalArgumentException( () -> maxFromPartialComparatorOrThrow(partialComparator, "", ""));
    assertIllegalArgumentException( () -> maxFromPartialComparatorOrThrow(partialComparator, "a", ""));
    assertIllegalArgumentException( () -> maxFromPartialComparatorOrThrow(partialComparator, "", "a"));

    assertEquals("a", minFromPartialComparatorOrThrow(partialComparator, "a", "b"));
    assertEquals("a", minFromPartialComparatorOrThrow(partialComparator, "b", "a"));
    assertEquals("a", minFromPartialComparatorOrThrow(partialComparator, "a", "a"));

    assertEquals("b", maxFromPartialComparatorOrThrow(partialComparator, "a", "b"));
    assertEquals("b", maxFromPartialComparatorOrThrow(partialComparator, "b", "a"));
    assertEquals("b", maxFromPartialComparatorOrThrow(partialComparator, "b", "b"));
  }

}
