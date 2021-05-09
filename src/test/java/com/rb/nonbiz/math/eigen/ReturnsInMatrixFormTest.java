package com.rb.nonbiz.math.eigen;

import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D;
import com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.function.BiFunction;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2D.emptyImmutableDoubleIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableDoubleIndexableArray2DTest.immutableDoubleIndexableArray2DMatcher;
import static com.rb.nonbiz.collections.ImmutableIndexableArray2D.immutableIndexableArray2D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray2DTest.immutableIndexableArray2DEqualityMatcher;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.ACTUAL;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.BACK_FILLED;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.ReturnsQuality.GAP_FILLED;
import static com.rb.nonbiz.math.eigen.ReturnsInMatrixForm.returnsInMatrixForm;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY0;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY1;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DAY2;

public class ReturnsInMatrixFormTest extends RBTestMatcher<ReturnsInMatrixForm> {

  @Test
  public void isEmpty_throws() {
    assertIllegalArgumentException( () -> returnsInMatrixForm(
        emptyImmutableDoubleIndexableArray2D(),
        immutableIndexableArray2D(
            new ReturnsQuality[][] { }, simpleArrayIndexMapping(), simpleArrayIndexMapping())));
  }

  @Test
  public void mappingsDontMatch_throws() {
    BiFunction<ArrayIndexMapping<LocalDate>, ArrayIndexMapping<InstrumentId>, ReturnsInMatrixForm> maker =
        (dayMapping, instrumentMapping) -> returnsInMatrixForm(
        ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D(
            new double[][] {
                // A     B     C
                { 0.98, 0.99, 1.00 }, // DAY0
                { 1.01, 1.02, 1.03 }, // DAY1
            },
            simpleArrayIndexMapping(DAY0, DAY1),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)),
        immutableIndexableArray2D(
            new ReturnsQuality[][] {
                // A           B           C
                { BACK_FILLED, ACTUAL,     ACTUAL }, // DAY0
                { ACTUAL,      GAP_FILLED, ACTUAL }, // DAY1
            },
            dayMapping,
            instrumentMapping));

    ReturnsInMatrixForm doesNotThrow = maker.apply(
        simpleArrayIndexMapping(DAY0, DAY1),
        simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C));
    assertIllegalArgumentException( () -> maker.apply(
        simpleArrayIndexMapping(DAY1, DAY0),
        simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)));
    assertIllegalArgumentException( () -> maker.apply(
        simpleArrayIndexMapping(DAY0, DAY1),
        simpleArrayIndexMapping(STOCK_B, STOCK_A, STOCK_C)));
  }

  @Test
  public void noActualReturns_throws() {
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, BACK_FILLED, BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, BACK_FILLED, GAP_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, GAP_FILLED,  BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, GAP_FILLED,  GAP_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(GAP_FILLED,  BACK_FILLED, BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(GAP_FILLED,  BACK_FILLED, GAP_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(GAP_FILLED,  GAP_FILLED,  BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(GAP_FILLED,  GAP_FILLED,  GAP_FILLED));
  }

  @Test
  public void backfilledMayOnlyAppearInBeginning_otherwiseThrows() {
    ReturnsInMatrixForm doesNotThrow;
    doesNotThrow = makeSingleDayReturns(BACK_FILLED, ACTUAL,      ACTUAL);
    doesNotThrow = makeSingleDayReturns(BACK_FILLED, BACK_FILLED, ACTUAL);
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, ACTUAL,      BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(ACTUAL,      BACK_FILLED, BACK_FILLED));
    assertIllegalArgumentException( () -> makeSingleDayReturns(ACTUAL,      BACK_FILLED, ACTUAL));
  }

  @Test
  public void gapFilledMayNotHaveBackFilledBefore_otherwiseThrows() {
    ReturnsInMatrixForm doesNotThrow;
    doesNotThrow = makeSingleDayReturns(BACK_FILLED, ACTUAL, GAP_FILLED);
    assertIllegalArgumentException( () -> makeSingleDayReturns(BACK_FILLED, GAP_FILLED, ACTUAL));
  }

  @Test
  public void returnsMustBeValid_otherwiseThrows() {
    assertIllegalArgumentException( () -> makeSimplestReturns(-1.2345));
    assertIllegalArgumentException( () -> makeSimplestReturns(0));
    assertIllegalArgumentException( () -> makeSimplestReturns(1e-5));
    ReturnsInMatrixForm doesNotThrow;
    doesNotThrow = makeSimplestReturns(0.1);
    doesNotThrow = makeSimplestReturns(0.9);
    doesNotThrow = makeSimplestReturns(1.0);
    doesNotThrow = makeSimplestReturns(1.1);
    doesNotThrow = makeSimplestReturns(10);
    assertIllegalArgumentException( () -> makeSimplestReturns(1e5));
  }

  private ReturnsInMatrixForm makeSingleDayReturns(ReturnsQuality rq1, ReturnsQuality rq2, ReturnsQuality rq3) {
    return returnsInMatrixForm(
        ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D(
            new double[][] {
                // A
                { 0.98 }, // DAY0
                { 0.99 }, // DAY1
                { 1.01 }, // DAY2
            },
            simpleArrayIndexMapping(DAY0, DAY1, DAY2),
            simpleArrayIndexMapping(STOCK_A)),
        immutableIndexableArray2D(
            new ReturnsQuality[][] {
                { rq1 },
                { rq2 },
                { rq3 }
            },
            simpleArrayIndexMapping(DAY0, DAY1, DAY2),
            simpleArrayIndexMapping(STOCK_A)));
  }

  private ReturnsInMatrixForm makeSimplestReturns(double onesBasedReturn) {
    return returnsInMatrixForm(
        ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D(
            new double[][] { { onesBasedReturn }}, simpleArrayIndexMapping(DAY0), simpleArrayIndexMapping(STOCK_A)),
        immutableIndexableArray2D(
            new ReturnsQuality[][] { { ACTUAL }}, simpleArrayIndexMapping(DAY0), simpleArrayIndexMapping(STOCK_A)));
  }

  @Override
  public ReturnsInMatrixForm makeTrivialObject() {
    return makeSimplestReturns(1.0);
  }

  @Override
  public ReturnsInMatrixForm makeNontrivialObject() {
    return returnsInMatrixForm(
        ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D(
            new double[][] {
                // A     B     C
                { 0.98, 0.99, 1.00 }, // DAY0
                { 1.01, 1.02, 1.03 }, // DAY1
            },
            simpleArrayIndexMapping(DAY0, DAY1),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)),
        immutableIndexableArray2D(
            new ReturnsQuality[][] {
                // A           B           C
                { BACK_FILLED, ACTUAL,     ACTUAL }, // DAY0
                { ACTUAL,      GAP_FILLED, ACTUAL }, // DAY1
            },
            simpleArrayIndexMapping(DAY0, DAY1),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)));
  }

  @Override
  public ReturnsInMatrixForm makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return returnsInMatrixForm(
        ImmutableDoubleIndexableArray2D.immutableDoubleIndexableArray2D(
            new double[][] {
                // A     B     C
                { 0.98 + e, 0.99 + e, 1.00 + e }, // DAY0
                { 1.01 + e, 1.02 + e, 1.03 + e }, // DAY1
            },
            simpleArrayIndexMapping(DAY0, DAY1),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)),
        immutableIndexableArray2D(
            new ReturnsQuality[][] {
                // A           B           C
                { BACK_FILLED, ACTUAL,     ACTUAL }, // DAY0
                { ACTUAL,      GAP_FILLED, ACTUAL }, // DAY1
            },
            simpleArrayIndexMapping(DAY0, DAY1),
            simpleArrayIndexMapping(STOCK_A, STOCK_B, STOCK_C)));
  }

  @Override
  protected boolean willMatch(ReturnsInMatrixForm expected, ReturnsInMatrixForm actual) {
    return returnsInMatrixFormMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<ReturnsInMatrixForm> returnsInMatrixFormMatcher(ReturnsInMatrixForm expected) {
    return makeMatcher(expected,
        match(v -> v.getReturnsMatrix(), f -> immutableDoubleIndexableArray2DMatcher(f)),
        match(v -> v.getQualityMatrix(), f -> immutableIndexableArray2DEqualityMatcher(f)));
  }

}
