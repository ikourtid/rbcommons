package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRanges.linearApproximationVarRanges;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValuesWithValuesPrecomputed;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValuesTest.linearApproximationVarRangesAndValuesMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.WaterSlideFunctionDescriptor.unscaledWaterSlideFunctionDescriptor;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.hamcrest.MatcherAssert.assertThat;

public class LinearApproximationVarRangesAndValuesMergerTest
    extends RBTest<LinearApproximationVarRangesAndValuesMerger> {

  FunctionDescriptor functionDescriptor = unscaledWaterSlideFunctionDescriptor(2.2, 5.5);

  @Test
  public void resultIsNotConvexFunction_slopeDoesNotIncreaseEnoughAtJoinPoint_throws() {
    // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to allow these cases to happen
    Function<Double, LinearApproximationVarRangesAndValues> merger = offset -> makeTestObject().merge(
        linearApproximationVarRangesAndValuesWithValuesPrecomputed(
            linearApproximationVarRanges(ImmutableList.of(10.0, 11.0)),
            ImmutableList.of(80.0, 81.0),
            functionDescriptor),
        linearApproximationVarRangesAndValuesWithValuesPrecomputed(
            linearApproximationVarRanges(ImmutableList.of(11.0, 12.0)),
            ImmutableList.of(81.0, 82 + offset),
            functionDescriptor));

    for (double v : new double[] { 1e-9, 1e-7, 0.1, 999 }) {
      assertThat(
          "If the slope increases, this are valid",
          merger.apply(v),
          linearApproximationVarRangesAndValuesMatcher(
              linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                  linearApproximationVarRanges(ImmutableList.of(10.0, 11.0, 12.0)),
                  ImmutableList.of(80.0, 81.0, 82 + v),
                  functionDescriptor)));
    }
    assertIllegalArgumentException( () -> merger.apply(-0.01));
    for (double v : new double[] { -2, -1, -0.01, -1e-7, -1e-9, 0 }) {
      assertIllegalArgumentException( () -> merger.apply(v));
    }
  }

  @Test
  public void approximationsAreExactlyContiguousInX_mergesCorrectly() {
    // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to allow these cases to happen
    assertThat(
        makeTestObject().merge(
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3)),
                ImmutableList.of(100.1, 100.2, 100.3),
                functionDescriptor),
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.3, 1.4, 1.5)),
                ImmutableList.of(100.3, 100.44, 100.55),
                functionDescriptor)),
        linearApproximationVarRangesAndValuesMatcher(
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3, 1.4, 1.5)),
                ImmutableList.of(100.1, 100.2, 100.3, 100.44, 100.55),
                functionDescriptor)));
    }

  @Test
  public void approximationsAreNotExactlyContiguousInX_mergesCorrectlyIfAlmostContiguous_throwsOtherwise() {
    // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to allow these cases to happen
    BiConsumer<Double, Double> asserter = (endOf1, startOf2) ->
    assertThat(
        makeTestObject().merge(
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3 + endOf1)),
                ImmutableList.of(100.1, 100.2, 100.3),
                functionDescriptor),
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.3 + startOf2, 1.4, 1.5)),
                ImmutableList.of(100.3, 100.44, 100.55),
                functionDescriptor)),
        linearApproximationVarRangesAndValuesMatcher(
            linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3 + startOf2, 1.4, 1.5)),
                ImmutableList.of(100.1, 100.2, 100.3, 100.44, 100.55),
                functionDescriptor)));
    {
      double e = 1e-9; // small epsilon
      asserter.accept(0.0, 0.0);
      asserter.accept(-e, 0.0);
      asserter.accept(e, 0.0);
      asserter.accept(0.0, -e);
      asserter.accept(0.0, e);
    }
    {
      double e = 1e-7; // big epsilon
      asserter.accept(0.0, 0.0);
      assertIllegalArgumentException( () -> asserter.accept(-e, 0.0));
      assertIllegalArgumentException( () -> asserter.accept(e, 0.0));
      assertIllegalArgumentException( () -> asserter.accept(0.0, -e));
      assertIllegalArgumentException( () -> asserter.accept(0.0, e));
    }
  }

  @Test
  public void approximationsAreNotExactlyContiguousInY_mergesCorrectlyIfAlmostContiguous_throwsOtherwise() {
    // Using linearApproximationVarRangesAndValuesWithValuesPrecomputed to allow these cases to happen
    BiConsumer<Double, Double> asserter = (endOf1, startOf2) ->
        assertThat(
            makeTestObject().merge(
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3)),
                    ImmutableList.of(100.1, 100.2, 100.3 + endOf1),
                    functionDescriptor),
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    linearApproximationVarRanges(ImmutableList.of(1.3, 1.4, 1.5)),
                    ImmutableList.of(100.3 + startOf2, 100.44, 100.55),
                    functionDescriptor)),
            linearApproximationVarRangesAndValuesMatcher(
                linearApproximationVarRangesAndValuesWithValuesPrecomputed(
                    linearApproximationVarRanges(ImmutableList.of(1.1, 1.2, 1.3, 1.4, 1.5)),
                    ImmutableList.of(100.1, 100.2, 100.3 + startOf2, 100.44, 100.55),
                    functionDescriptor)));
    {
      double e = 1e-9; // small epsilon
      asserter.accept(0.0, 0.0);
      asserter.accept(-e, 0.0);
      asserter.accept(e, 0.0);
      asserter.accept(0.0, -e);
      asserter.accept(0.0, e);
    }
    {
      double e = 1e-7; // big epsilon
      asserter.accept(0.0, 0.0);
      assertIllegalArgumentException( () -> asserter.accept(-e, 0.0));
      assertIllegalArgumentException( () -> asserter.accept(e, 0.0));
      assertIllegalArgumentException( () -> asserter.accept(0.0, -e));
      assertIllegalArgumentException( () -> asserter.accept(0.0, e));
    }
  }

  @Override
  protected LinearApproximationVarRangesAndValuesMerger makeTestObject() {
    return new LinearApproximationVarRangesAndValuesMerger();
  }

}
