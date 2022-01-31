package com.rb.nonbiz.types;

import com.google.common.collect.Range;
import com.rb.biz.types.Money;
import com.rb.nonbiz.testutils.RBCommonsTestPlusIntegration;
import org.jmock.Expectations;
import org.junit.Test;

import java.util.Optional;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.types.HardAndSoftRange.hardAndSoftRange;
import static com.rb.nonbiz.types.HardAndSoftRangeTest.hardAndSoftRangeMatcher;
import static org.hamcrest.MatcherAssert.assertThat;

public class HardAndSoftRangeGeneralInterpreterTest
    extends RBCommonsTestPlusIntegration<HardAndSoftRangeGeneralInterpreter> {

  HardAndSoftRangeInterpreter hardAndSoftRangeInterpreter =
      mockery.mock(HardAndSoftRangeInterpreter.class);

  // Remember that, unlike ClosedUnitFractionHardAndSoftRange, this does not have to be closed on both endpoints.
  // So let's just use a more general object here.
  private final HardAndSoftRange<Money> HARD_AND_SOFT_RANGE = hardAndSoftRange(
      Range.closedOpen(money(12), money(28)),   // hard range lower bound CLOSED; upper bound OPEN
      Range.closed(    money(15), money(25)));  // both soft range bounds MUST be CLOSED

  @Test
  public void currentPointExists_delegates() {
    mockery.checking(new Expectations() {{
      oneOf(hardAndSoftRangeInterpreter).getRangeToUse(
          with(equal(money(13))),
          with(hardAndSoftRangeMatcher(HARD_AND_SOFT_RANGE)));
      will(returnValue(Range.closed(money(13), money(25))));
    }});

    rbSetOf(makeTestObject(), makeRealObject())
        .forEach(interpreter -> assertThat(
            interpreter.getRangeToUse(Optional.of(money(13)), HARD_AND_SOFT_RANGE),
            preciseValueRangeMatcher(
                Range.closed(money(13), money(25)), 1e-8)));
  }

  @Test
  public void currentPointIsEmpty_returnsSoftRange() {
    mockery.checking(new Expectations() {{
      never(hardAndSoftRangeInterpreter);
    }});

    rbSetOf(makeTestObject(), makeRealObject())
        .forEach(interpreter -> assertThat(
            interpreter.getRangeToUse(Optional.<Money>empty(), HARD_AND_SOFT_RANGE),
            preciseValueRangeMatcher(
                // Returns the soft range
                Range.closed(money(15), money(25)), 1e-8)));
  }

  @Override
  protected Class<HardAndSoftRangeGeneralInterpreter> getClassBeingTested() {
    return HardAndSoftRangeGeneralInterpreter.class;
  }

  @Override
  protected HardAndSoftRangeGeneralInterpreter makeTestObject() {
    HardAndSoftRangeGeneralInterpreter testObject = new HardAndSoftRangeGeneralInterpreter();
    testObject.hardAndSoftRangeInterpreter = hardAndSoftRangeInterpreter;
    return testObject;
  }

}
