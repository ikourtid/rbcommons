package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.biz.types.Money.ZERO_MONEY;
import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;
import static org.hamcrest.MatcherAssert.assertThat;

public class HardAndSoftRangeInterpreterTest extends RBTest<HardAndSoftRangeInterpreter> {

  @Test
  public void testWithBothBounds() {
    BiConsumer<Money, Range<Money>> asserter = (pointInRange, expectedResult) ->
        assertThat(
            makeTestObject().getRangeToUse(
                pointInRange,
                hardAndSoftRange(
                    Range.closedOpen(money(12), money(28)),    // hard range lower bound CLOSED; upper bound OPEN
                    Range.closed(    money(15), money(25)))),  // both soft range bounds MUST be CLOSED
            preciseValueRangeMatcher(
                expectedResult, 1e-8));

    // If below the hard range lower endpoint, we should force the value to go up to the soft range lower endpoint.
    // And since it's also not above the upper endpoint of the hard range, the upper bound will be the tighter of the two.
    asserter.accept(money(0.123), Range.closed(money(15), money(25)));
    asserter.accept(money(11.9),  Range.closed(money(15), money(25)));

    // If AT the hard range lower bound AND the hard range lower bound is CLOSED, we are OK allowing the current value,
    // so we extend the soft range down to it.
    asserter.accept(money(12),   Range.closed(money(12),   money(25)));

    // If between the lower bounds of the hard and soft ranges, we are OK allowing the current value,
    // but don't want it to get any lower. We extend the soft range down to include the current value.
    asserter.accept(money(12.1), Range.closed(money(12.1), money(25)));
    asserter.accept(money(14.9), Range.closed(money(14.9), money(25)));

    // If AT the soft range lower bound, return the soft range.
    asserter.accept(money(15),   Range.closed(money(15),   money(25)));

    // If within the soft range (the tighter one, 15 to 25) then we just need to bound it within that soft range.
    asserter.accept(money(15.1), Range.closed(money(15),   money(25)));
    asserter.accept(money(24.9), Range.closed(money(15),   money(25)));

    // The behavior on the upper bound is the flip side of what we do for the lower bound.
    asserter.accept(money(25),   Range.closed(money(15), money(25)));
    asserter.accept(money(25.1), Range.closed(money(15), money(25.1)));
    asserter.accept(money(27.9), Range.closed(money(15), money(27.9)));

    // If AT the hard range upper bound AND it's an OPEN bound, we force the value back to within the soft range.
    asserter.accept(money(28),   Range.closed(money(15), money(25)));

    // If we exceed the upper bound of the hard (looser) range, we must force the value back down to the upper bound of the
    // soft (tighter) range.
    asserter.accept(money(28.1), Range.closed(money(15), money(25)));
    asserter.accept(money(999),  Range.closed(money(15), money(25)));
  }

  @Test
  public void testWithUpperBound() {
    BiConsumer<Money, Range<Money>> asserter = (pointInRange, expectedResult) ->
        assertThat(
            makeTestObject().getRangeToUse(
                pointInRange,
                hardAndSoftRange(
                    Range.atMost(money(28)),   // hard range upper bound can  be OPEN or CLOSED; here it's CLOSED
                    Range.atMost(money(25)))), // soft range upper bound must be CLOSED
            preciseValueRangeMatcher(
                expectedResult, 1e-8));
    asserter.accept(money(28),   Range.atMost(money(28)));

    // If within the soft range, then return the soft range.
    asserter.accept(ZERO_MONEY,  Range.atMost(money(25)));
    asserter.accept(money(15.1), Range.atMost(money(25)));
    asserter.accept(money(24.9), Range.atMost(money(25)));

    // If AT the soft range upper bound, return the soft range
    asserter.accept(money(25),   Range.atMost(money(25)));

    // If above the soft upper bound but below the hard upper bound, return the current point as a (CLOSED) upper bound.
    asserter.accept(money(25.1), Range.atMost(money(25.1)));
    asserter.accept(money(27.9), Range.atMost(money(27.9)));

    // If AT the hard range upper bound AND that bound is CLOSED, return the hard range
    asserter.accept(money(28),   Range.atMost(money(28)));

    // If we exceed the upper bound of the hard (looser) range, we must force the value back down to the upper bound of the
    // soft (tighter) range.
    asserter.accept(money(28.1), Range.atMost(money(25)));
    asserter.accept(money(999),  Range.atMost(money(25)));
  }

  @Test
  public void testWithLowerBound() {
    BiConsumer<Money, Range<Money>> asserter = (pointInRange, expectedResult) ->
        assertThat(
            makeTestObject().getRangeToUse(
                pointInRange,
                hardAndSoftRange(
                    Range.greaterThan(money(12)),     // hard range lower bound can  be OPEN
                    Range.atLeast(    money(15)))),   // soft range lower bound must be CLOSED
            preciseValueRangeMatcher(
                expectedResult, 1e-8));

    // If below the hard range lower bound, we should force the value to move up to be within the soft range.
    asserter.accept(ZERO_MONEY,  Range.atLeast(money(15)));
    asserter.accept(money(1.23), Range.atLeast(money(15)));
    asserter.accept(money(11.9), Range.atLeast(money(15)));

    // If AT the hard range lower bound AND it's OPEN, return the soft range
    asserter.accept(money(12),   Range.atLeast(money(15)));

    // If between the lower bounds of the hard and soft ranges, we are OK allowing the present value,
    // but don't want it to get any lower.
    asserter.accept(money(12.1), Range.atLeast(money(12.1)));
    asserter.accept(money(14.9), Range.atLeast(money(14.9)));

    // If AT the soft range lower bound, return the soft range.
    asserter.accept(money(15),   Range.atLeast(money(15)));

    // If within the soft range then we just need to return the soft range.
    asserter.accept(money(15.1), Range.atLeast(money(15)));
    asserter.accept(money(24.9), Range.atLeast(money(15)));
  }

  @Override
  protected HardAndSoftRangeInterpreter makeTestObject() {
    return new HardAndSoftRangeInterpreter();
  }

}
