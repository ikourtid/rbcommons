package com.rb.nonbiz.collections;

import com.rb.nonbiz.testutils.RBCommonsIntegrationTest;
import org.junit.Test;

import static com.rb.nonbiz.collections.ClosedUnitFractionHardAndSoftRanges.closedUnitFractionHardAndSoftRanges;
import static com.rb.nonbiz.collections.ClosedUnitFractionHardAndSoftRangesTest.closedUnitFractionHardAndSoftRangesMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.ClosedUnitFractionHardAndSoftRange.closedUnitFractionHardAndSoftRange;
import static com.rb.nonbiz.types.ClosedUnitFractionRange.closedUnitFractionRange;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClosedUnitFractionHardAndSoftRangesShrinkerTest
    extends RBCommonsIntegrationTest<ClosedUnitFractionHardAndSoftRangesShrinker> {

  @Test
  public void generalCase_shrinksEveryNumber() {
    assertThat(
        makeRealObject().shrink(
            closedUnitFractionHardAndSoftRanges(
                singletonRBMap("key", closedUnitFractionHardAndSoftRange(
                    closedUnitFractionRange(unitFraction(0.1), unitFraction(0.8)),
                    closedUnitFractionRange(unitFraction(0.4), unitFraction(0.6))))),
            unitFraction(0.7)),
        closedUnitFractionHardAndSoftRangesMatcher(
            closedUnitFractionHardAndSoftRanges(
                singletonRBMap("key", closedUnitFractionHardAndSoftRange(
                    // After the $300 withdrawal, the final portfolio will be $700.
                    // The 0.4 below refers to 40% of the total. Since the new total will be $700,
                    // in dollar terms, this is really 40% * $700 = $280 of the final portfolio.
                    // So that's 0.28 in the pre-withdrawal, $1000 portfolio.
                    closedUnitFractionRange(
                        unitFraction(doubleExplained(0.07, 0.1 * 0.7)),
                        unitFraction(doubleExplained(0.56, 0.8 * 0.7))),
                    closedUnitFractionRange(
                        unitFraction(doubleExplained(0.28, 0.4 * 0.7)),
                        unitFraction(doubleExplained(0.42, 0.6 * 0.7))))))));
  }

  @Override
  protected Class<ClosedUnitFractionHardAndSoftRangesShrinker> getClassBeingTested() {
    return ClosedUnitFractionHardAndSoftRangesShrinker.class;
  }

}
