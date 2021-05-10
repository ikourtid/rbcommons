package com.rb.nonbiz.math.optimization.highlevel.integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.nonbiz.collections.ClosedRange;
import com.rb.nonbiz.collections.RBIterables;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.math.optimization.general.AllRawVariablesInOrder;
import com.rb.nonbiz.math.optimization.general.RawVariable;
import com.rb.nonbiz.math.optimization.highlevel.GeneralSuperVar;
import com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVars;
import com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarsGenerator;
import com.rb.nonbiz.testutils.RBIntegrationTest;
import com.rb.nonbiz.text.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.math.optimization.general.AllRawVariablesAndOptimalValues.allRawVariablesAndOptimalValues;
import static com.rb.nonbiz.math.optimization.general.RawVariable.rawVariable;
import static com.rb.nonbiz.math.optimization.highlevel.ConstantTerm.constantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.ContiguousLinearApproximationIndividualSegmentSuperVars.contiguousLinearApproximationIndividualSegmentSuperVars;
import static com.rb.nonbiz.math.optimization.highlevel.GeometricallyIncreasingRangesGenerationInstructions.GeometricallyIncreasingRangesGenerationInstructionsBuilder.geometricallyIncreasingRangesGenerationInstructionsBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarConstraint.highLevelVarsEqualZeroConstraint;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.highLevelVarExpression;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarExpression.highLevelVarExpressionWithoutConstantTerm;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVarWithWeight.highLevelVarWithWeight;
import static com.rb.nonbiz.math.optimization.highlevel.HighLevelVariablesBuilder.highLevelVariablesBuilder;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarRangesAndValues.linearApproximationVarRangesAndValues;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVars.linearApproximationVars;
import static com.rb.nonbiz.math.optimization.highlevel.LinearApproximationVarsTest.linearApproximationVarsMatcher;
import static com.rb.nonbiz.math.optimization.highlevel.QuadraticFunctionDescriptor.quadraticFunctionDescriptor;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testGeneralSuperVarWithoutArtificialTerms;
import static com.rb.nonbiz.math.optimization.highlevel.RBPublicTestOnlySuperVarConstructors.testLinearApproximationIndividualSegmentSuperVar;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;

/**
 * Note that, in prod, we mostly use the 'water slide function', not the square (x^2). But in the context of this test,
 * it's easier to understand the behavior of a plain quadratic.
 */
public class LinearApproximationVarsGeneratorIntegrationTest extends RBIntegrationTest<LinearApproximationVarsGenerator> {

  GeometricallyIncreasingLinearApproximationVarRangesGenerator geometricallyIncreasingLinearApproximationVarRangesGenerator =
      makeRealObject(GeometricallyIncreasingLinearApproximationVarRangesGenerator.class);

  @Test
  public void happyPath_xSquared_lowerBoundIsZero_simpleWithBigStepMultiplier_newVariable() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    double initialStep = 0.01;
    double stepMultiplier = 5.0;
    LinearApproximationVars linearApproximationVars = linearApproximationVars(
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpressionWithoutConstantTerm(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  0), 1),
                    highLevelVarWithWeight(rawVariable("x_0.01_to_0.06", 1), 1),
                    highLevelVarWithWeight(rawVariable("x_0.06_to_0.31", 2), 1),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_1.0",  3), 1)))),
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpressionWithoutConstantTerm(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  0), doubleExplained(0.01, getXSquaredSlope(0.00, 0.01))),
                    highLevelVarWithWeight(rawVariable("x_0.01_to_0.06", 1), doubleExplained(0.07, getXSquaredSlope(0.01, 0.06))),
                    highLevelVarWithWeight(rawVariable("x_0.06_to_0.31", 2), doubleExplained(0.37, getXSquaredSlope(0.06, 0.31))),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_1.0",  3), doubleExplained(1.31, getXSquaredSlope(0.31, 1.00)))))),
        contiguousLinearApproximationIndividualSegmentSuperVars(ImmutableList.of(
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.0_to_0.01",  0), Range.closedOpen(0.00, 0.01), 0.01),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.01_to_0.06", 1), Range.closedOpen(0.01, 0.06), 0.07),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.06_to_0.31", 2), Range.closedOpen(0.06, 0.31), 0.37),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.31_to_1.0",  3), Range.closed(    0.31, 1.00), 1.31))));
    assertThat(
        generateQuadraticVars(builder, "x", closedRange(0.0, 1.0), initialStep, stepMultiplier),
        linearApproximationVarsMatcher(
            linearApproximationVars));

    AllRawVariablesInOrder allRawVariablesInOrder = builder.getAllRawVariablesInOrderBuilder().build();
    BiConsumer<Double, double[]> linearPartAsserter = (expectedValue, segmentVarValues) ->
        Assert.assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            1e-8);

    double d = 1e-2;
    double max0 = 0.01;
    double max1 = 0.06 - 0.01;
    double max2 = 0.31 - 0.06;
    double max3 = 1.00 - 0.31;

    linearPartAsserter.accept(0.0,                           new double[] { 0,        0,        0,        0 });
    linearPartAsserter.accept(d,                             new double[] { d,        0,        0,        0 });
    linearPartAsserter.accept(max0 - d,                      new double[] { max0 - d, 0,        0,        0 });
    linearPartAsserter.accept(max0,                          new double[] { max0,     0,        0,        0 });

    linearPartAsserter.accept(max0 + d,                      new double[] { max0,     d,        0,        0 });
    linearPartAsserter.accept(max0 + max1 - d,               new double[] { max0,     max1 - d, 0,        0 });
    linearPartAsserter.accept(max0 + max1,                   new double[] { max0,     max1,     0,        0 });

    linearPartAsserter.accept(max0 + max1 + d,               new double[] { max0,     max1,     d,        0 });
    linearPartAsserter.accept(max0 + max1 + max2 - d,        new double[] { max0,     max1,     max2 - d, 0 });
    linearPartAsserter.accept(max0 + max1 + max2,            new double[] { max0,     max1,     max2,     0 });

    linearPartAsserter.accept(max0 + max1 + max2 + d,        new double[] { max0,     max1,     max2,     d });
    linearPartAsserter.accept(max0 + max1 + max2 + max3 - d, new double[] { max0,     max1,     max2,     max3 - d });
    linearPartAsserter.accept(max0 + max1 + max2 + max3,     new double[] { max0,     max1,     max2,     max3 });

    TriConsumer<Double, Double, double[]> quadraticPartAsserter = (epsilon, expectedValue, segmentVarValues) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            epsilon);

    // Let's assert on the monotonicity of the approximated squares. They should increase as the approximated linear
    // value (as expressed by the individual line segment vars) increases.
    RBIterables.consecutivePairsForEach(
        ImmutableList.of(
            new double[] { 0,     0,     0,    0 },
            new double[] { 0.001, 0,     0,    0 },
            new double[] { 0.005, 0,     0,    0 },
            new double[] { 0.009, 0,     0,    0 },
            new double[] { max0,  0,     0,    0 },
            new double[] { max0,  0.001, 0,    0 },
            new double[] { max0,  0.020, 0,    0 },
            new double[] { max0,  0.049, 0,    0 },
            new double[] { max0,  max1,  0,    0 },
            new double[] { max0,  max1,  0.01, 0 },
            new double[] { max0,  max1,  0.09, 0 },
            new double[] { max0,  max1,  0.24, 0 },
            new double[] { max0,  max1,  max2, 0 },
            new double[] { max0,  max1,  max2, 0.01 },
            new double[] { max0,  max1,  max2, 0.46 },
            new double[] { max0,  max1,  max2, 0.68 },
            new double[] { max0,  max1,  max2, 0.69 })
            .stream()
            .map(segmentVarValues -> makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)))
            .collect(Collectors.toList()),
        (val1, val2) -> assertTrue(Strings.format("We must have %s < %s", val1, val2), val1 < val2));

    // First, make sure the values are correct for the corner cases where the individual line segment variables
    // are either at their min or at their max.
    // Those points aren't interpolated so they should be exact, therefore we'll use a tiny epsilon.
    quadraticPartAsserter.accept(1e-8, sq(0.00),     new double[] { 0,    0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.01),     new double[] { max0, 0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.06),     new double[] { max0, max1, 0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.31),     new double[] { max0, max1, max2, 0 });
    quadraticPartAsserter.accept(1e-8, sq(1.00),     new double[] { max0, max1, max2, max3 });

    // Now, take the midpoints of the 4 line segments. The approximation would be off by the largest amount there.
    // We will use bigger/coarser epsilons here. The epsilons were backfitted a bit here (i.e. I ran the tests and
    // then used epsilons as small as possible to make them pass), but they are still relatively small-ish
    // percentage-wise in comparison to the values being compared.
    quadraticPartAsserter.accept(0.0001, sq(doubleExplained(0.005, (0.00 + 0.01) / 2)), new double[] { 0.005, 0,     0,     0 });
    quadraticPartAsserter.accept(0.0065, sq(doubleExplained(0.035, (0.01 + 0.06) / 2)), new double[] { max0,  0.025, 0,     0 });
    quadraticPartAsserter.accept(0.0160, sq(doubleExplained(0.185, (0.06 + 0.31) / 2)), new double[] { max0,  max1,  0.125, 0 });
    quadraticPartAsserter.accept(0.1200, sq(doubleExplained(0.655, (0.31 + 1.00) / 2)), new double[] { max0,  max1,  max2,  0.345 });
  }

  @Test
  public void happyPath_xSquared_lowerBoundIsZero_simpleWithBigStepMultiplier_existingVariable() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    double initialStep = 0.01;
    double stepMultiplier = 5.0;
    RawVariable rawVar = builder.addConstrainedRawVariable("x", Range.closed(0.0, 1.0));
    SuperVar simplePositiveVar = builder.addSuperVar(GeneralSuperVar.superVarOfSingleVariable(rawVar));

    LinearApproximationVars linearApproximationVars = linearApproximationVars(
        simplePositiveVar,
        testGeneralSuperVarWithoutArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpressionWithoutConstantTerm(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  1), doubleExplained(0.01, getXSquaredSlope(0.00, 0.01))),
                    highLevelVarWithWeight(rawVariable("x_0.01_to_0.06", 2), doubleExplained(0.07, getXSquaredSlope(0.01, 0.06))),
                    highLevelVarWithWeight(rawVariable("x_0.06_to_0.31", 3), doubleExplained(0.37, getXSquaredSlope(0.06, 0.31))),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_1.0",  4), doubleExplained(1.31, getXSquaredSlope(0.31, 1.00))))),
            self -> singletonList(
                highLevelVarsEqualZeroConstraint(
                    DUMMY_LABEL,
                    highLevelVarExpressionWithoutConstantTerm(
                        ImmutableList.of(
                            highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  1), 1),
                            highLevelVarWithWeight(rawVariable("x_0.01_to_0.06", 2), 1),
                            highLevelVarWithWeight(rawVariable("x_0.06_to_0.31", 3), 1),
                            highLevelVarWithWeight(rawVariable("x_0.31_to_1.0",  4), 1),
                            highLevelVarWithWeight(simplePositiveVar, -1)))))),
        contiguousLinearApproximationIndividualSegmentSuperVars(ImmutableList.of(
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.0_to_0.01",  1), Range.closedOpen(0.00, 0.01), 0.01),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.01_to_0.06", 2), Range.closedOpen(0.01, 0.06), 0.07),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.06_to_0.31", 3), Range.closedOpen(0.06, 0.31), 0.37),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.31_to_1.0",  4), Range.closed(    0.31, 1.00), 1.31))));

    assertThat(
        generateQuadraticVarsOfExistingVariable(
            builder, "x", closedRange(0.0, 1.0), initialStep, stepMultiplier, simplePositiveVar),
        linearApproximationVarsMatcher(linearApproximationVars));

    AllRawVariablesInOrder allRawVariablesInOrder = builder.getAllRawVariablesInOrderBuilder().build();
    BiConsumer<Double, Double> linearPartAsserter = (expectedValue, actualValueForExistingSuperVar) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, new double[] {
                    actualValueForExistingSuperVar, 
                    DUMMY_DOUBLE, 
                    DUMMY_DOUBLE, 
                    DUMMY_DOUBLE, 
                    DUMMY_DOUBLE })),
            1e-8);

    double d = 1e-2;
    double max0 = 0.01;
    double max1 = doubleExplained(0.05, 0.06 - 0.01);
    double max2 = doubleExplained(0.25, 0.31 - 0.06);
    double max3 = doubleExplained(0.69, 1.00 - 0.31);

    for (double valueForLinearPart : ImmutableList.of(
        0.0,    d, max0 - d, max0,
        0.0,    d, max1 - d, max1,
        0.0,    d, max2 - d, max2,
        0.0,    d, max3 - d, max3)) {
      linearPartAsserter.accept(valueForLinearPart, valueForLinearPart);
    }

    TriConsumer<Double, Double, double[]> quadraticPartAsserter = (epsilon, expectedValue, segmentVarValues) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            epsilon);

    // Let's assert on the monotonicity of the approximated squares. They should increase as the approximated linear
    // value (as expressed by the individual line segment vars) increases.
    RBIterables.consecutivePairsForEach(
        ImmutableList.of(
            new double[] { DUMMY_DOUBLE, 0,     0,            0,           0 },
            new double[] { DUMMY_DOUBLE, 0.001, 0,            0,           0 },
            new double[] { DUMMY_DOUBLE, 0.005, 0,            0,           0 },
            new double[] { DUMMY_DOUBLE, 0.009, 0,            0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  0,            0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  0.011 - 0.01, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  0.030 - 0.01, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  0.059 - 0.01, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         0,           0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         0.07 - 0.06, 0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         0.15 - 0.06, 0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         0.30 - 0.06, 0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         max2,        0 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         max2,        0.32 - 0.31 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         max2,        0.77 - 0.31 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         max2,        0.99 - 0.31 },
            new double[] { DUMMY_DOUBLE, max0,  max1,         max2,        1.00 - 0.31 })
            .stream()
            .map(segmentVarValues -> makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)))
            .collect(Collectors.toList()),
        (val1, val2) -> assertTrue(val1 < val2));

    // First, make sure the values are correct for the corner cases where the individual line segment variables
    // are either at their min or at their max.
    // Those points aren't interpolated so they should be exact, therefore we'll use a tiny epsilon.
    quadraticPartAsserter.accept(1e-8, sq(0),                         new double[] { DUMMY_DOUBLE, 0,    0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(max0),                      new double[] { DUMMY_DOUBLE, max0, 0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(max0 + max1),               new double[] { DUMMY_DOUBLE, max0, max1, 0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(max0 + max1 + max2),        new double[] { DUMMY_DOUBLE, max0, max1, max2, 0 });
    quadraticPartAsserter.accept(1e-8, sq(max0 + max1 + max2 + max3), new double[] { DUMMY_DOUBLE, max0, max1, max2, max3 });

    // Now, take the midpoints of the 4 line segments. The approximation would be off by the largest amount there.
    // We will use bigger/coarser epsilons here. The epsilons were backfitted a bit here (i.e. I ran the tests and
    // then used epsilons as small as possible to make them pass), but they are still relatively small-ish
    // percentage-wise in comparison to the values being compared.
    quadraticPartAsserter.accept(0.0001, sq(doubleExplained(0.005,        max0 / 2)), new double[] { DUMMY_DOUBLE, 0.005, 0,            0,            0 });
    quadraticPartAsserter.accept(0.0065, sq(doubleExplained(0.035, 0.01 + max1 / 2)), new double[] { DUMMY_DOUBLE, max0,  0.035 - 0.01, 0,            0 });
    quadraticPartAsserter.accept(0.0160, sq(doubleExplained(0.185, 0.06 + max2 / 2)), new double[] { DUMMY_DOUBLE, max0,  max1,         0.185 - 0.06, 0 });
    quadraticPartAsserter.accept(0.1200, sq(doubleExplained(0.655, 0.31 + max3 / 2)), new double[] { DUMMY_DOUBLE, max0,  max1,         max2,         0.655 - 0.31 });
  }

  @Test
  public void happyPath_xSquared_lowerBoundIsZero_biggerStepMultiplier() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    double initialStep = 0.01;
    double stepMultiplier = 2.0;
    assertThat(
        generateQuadraticVars(builder, "x", closedRange(0.0, 1.0), initialStep, stepMultiplier),
        linearApproximationVarsMatcher(
            linearApproximationVars(
                testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                    DUMMY_LABEL,
                    highLevelVarExpressionWithoutConstantTerm(
                        ImmutableList.of(
                            highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  0), 1),
                            highLevelVarWithWeight(rawVariable("x_0.01_to_0.03", 1), 1),
                            highLevelVarWithWeight(rawVariable("x_0.03_to_0.07", 2), 1),
                            highLevelVarWithWeight(rawVariable("x_0.07_to_0.15", 3), 1),
                            highLevelVarWithWeight(rawVariable("x_0.15_to_0.31", 4), 1),
                            highLevelVarWithWeight(rawVariable("x_0.31_to_0.63", 5), 1),
                            highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",  6), 1)))),
                testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
                    DUMMY_LABEL,
                    highLevelVarExpressionWithoutConstantTerm(
                        ImmutableList.of(
                            highLevelVarWithWeight(rawVariable("x_0.0_to_0.01",  0), doubleExplained(0.01, getXSquaredSlope(0.00, 0.01))),
                            highLevelVarWithWeight(rawVariable("x_0.01_to_0.03", 1), doubleExplained(0.04, getXSquaredSlope(0.01, 0.03))),
                            highLevelVarWithWeight(rawVariable("x_0.03_to_0.07", 2), doubleExplained(0.10, getXSquaredSlope(0.03, 0.07))),
                            highLevelVarWithWeight(rawVariable("x_0.07_to_0.15", 3), doubleExplained(0.22, getXSquaredSlope(0.07, 0.15))),
                            highLevelVarWithWeight(rawVariable("x_0.15_to_0.31", 4), doubleExplained(0.46, getXSquaredSlope(0.15, 0.31))),
                            highLevelVarWithWeight(rawVariable("x_0.31_to_0.63", 5), doubleExplained(0.94, getXSquaredSlope(0.31, 0.63))),
                            highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",  6), doubleExplained(1.63, getXSquaredSlope(0.63, 1.00)))))),
                contiguousLinearApproximationIndividualSegmentSuperVars(ImmutableList.of(
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.0_to_0.01",  0), Range.closedOpen(0.00, 0.01), 0.01),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.01_to_0.03", 1), Range.closedOpen(0.01, 0.03), 0.04),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.03_to_0.07", 2), Range.closedOpen(0.03, 0.07), 0.10),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.07_to_0.15", 3), Range.closedOpen(0.07, 0.15), 0.22),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.15_to_0.31", 4), Range.closedOpen(0.15, 0.31), 0.46),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.31_to_0.63", 5), Range.closedOpen(0.31, 0.63), 0.94),
                    testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.63_to_1.0",  6), Range.closed(    0.63, 1.00), 1.63))))));
  }

  @Test
  public void happyPath_xSquared_lowerBoundIsNonZero_newVariable() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    double initialStep = 0.01;
    double stepMultiplier = 2.0;
    LinearApproximationVars linearApproximationVars = linearApproximationVars(
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpression(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.1234_to_0.15", 0), 1), // text truncated from 0.12344
                    highLevelVarWithWeight(rawVariable("x_0.15_to_0.31",   1), 1),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_0.63",   2), 1),
                    highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",    3), 1)),
                // this way, if every x_? variable is at its lower bound of 0, the original expression will have
                // a value of 0.12344, the lower bound
                constantTerm(0.12344))),
        testGeneralSuperVarWithoutAddedConstraintsOrArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpression(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.1234_to_0.15", 0), doubleExplained(0.27344, getXSquaredSlope(0.12344, 0.15))),
                    highLevelVarWithWeight(rawVariable("x_0.15_to_0.31",   1), doubleExplained(0.46, getXSquaredSlope(0.15, 0.31))),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_0.63",   2), doubleExplained(0.94, getXSquaredSlope(0.31, 0.63))),
                    highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",    3), doubleExplained(1.63, getXSquaredSlope(0.63, 1.00)))),
                // this way, if every x_? variable is at its lower bound of 0, the square of the original
                // expression will have a value of 0.12344, the lower bound
                constantTerm(doubleExplained(0.0152374336, 0.12344 * 0.12344)))),
        contiguousLinearApproximationIndividualSegmentSuperVars(ImmutableList.of(
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.1234_to_0.15", 0), Range.closedOpen(0.12344, 0.15), 0.27344),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.15_to_0.31",   1), Range.closedOpen(0.15,    0.31), 0.46),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.31_to_0.63",   2), Range.closedOpen(0.31,    0.63), 0.94),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.63_to_1.0",    3), Range.closed(    0.63,    1.00), 1.63))));
    assertThat(
        generateQuadraticVars(builder, "x", closedRange(0.12344, 1.0), initialStep, stepMultiplier),
        linearApproximationVarsMatcher(linearApproximationVars));

    AllRawVariablesInOrder allRawVariablesInOrder = builder.getAllRawVariablesInOrderBuilder().build();
    BiConsumer<Double, double[]> linearPartAsserter = (expectedValue, segmentVarValues) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            1e-8);
    double d = 1e-2;
    double max0 = 0.15 - 0.12344;
    double max1 = 0.31 - 0.15;
    double max2 = 0.63 - 0.31;
    double max3 = 1.00 - 0.63;

    linearPartAsserter.accept(0.12344,                                 new double[] { 0,        0,        0,        0 });
    linearPartAsserter.accept(0.12344 + d,                             new double[] { d,        0,        0,        0 });
    linearPartAsserter.accept(0.12344 + max0 - d,                      new double[] { max0 - d, 0,        0,        0 });
    linearPartAsserter.accept(0.12344 + max0,                          new double[] { max0,     0,        0,        0 });

    linearPartAsserter.accept(0.12344 + max0 + d,                      new double[] { max0,     d,        0,        0 });
    linearPartAsserter.accept(0.12344 + max0 + max1 - d,               new double[] { max0,     max1 - d, 0,        0 });
    linearPartAsserter.accept(0.12344 + max0 + max1,                   new double[] { max0,     max1,     0,        0 });

    linearPartAsserter.accept(0.12344 + max0 + max1 + d,               new double[] { max0,     max1,     d,        0 });
    linearPartAsserter.accept(0.12344 + max0 + max1 + max2 - d,        new double[] { max0,     max1,     max2 - d, 0 });
    linearPartAsserter.accept(0.12344 + max0 + max1 + max2,            new double[] { max0,     max1,     max2,     0 });

    linearPartAsserter.accept(0.12344 + max0 + max1 + max2 + d,        new double[] { max0,     max1,     max2,     0 + d });
    linearPartAsserter.accept(0.12344 + max0 + max1 + max2 + max3 - d, new double[] { max0,     max1,     max2,     max3 - d });
    linearPartAsserter.accept(0.12344 + max0 + max1 + max2 + max3,     new double[] { max0,     max1,     max2,     max3 });

    // Let's assert on the monotonicity of the approximated squares. They should increase as the approximated linear
    // value (as expressed by the individual line segment vars) increases.
    RBIterables.consecutivePairsForEach(
        ImmutableList.of(
            new double[] { 0,              0,           0,           0 },
            new double[] { 0.13 - 0.12344, 0,           0,           0 },
            new double[] { 0.14 - 0.12344, 0,           0,           0 },
            new double[] { max0,           0,           0,           0 },
            new double[] { max0,           0.16 - 0.15, 0,           0 },
            new double[] { max0,           0.24 - 0.15, 0,           0 },
            new double[] { max0,           0.30 - 0.15, 0,           0 },
            new double[] { max0,           max1,        0,           0 },
            new double[] { max0,           max1,        0.32 - 0.31, 0 },
            new double[] { max0,           max1,        0.48 - 0.31, 0 },
            new double[] { max0,           max1,        0.62 - 0.31, 0 },
            new double[] { max0,           max1,        max2,        0 },
            new double[] { max0,           max1,        max2,        0.64 - 0.63 },
            new double[] { max0,           max1,        max2,        0.82 - 0.63 },
            new double[] { max0,           max1,        max2,        0.99 - 0.63 },
            new double[] { max0,           max1,        max2,        1.00 - 0.63 })
            .stream()
            .map(segmentVarValues -> makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)))
            .collect(Collectors.toList()),
        (val1, val2) -> assertTrue(val1 < val2));

    TriConsumer<Double, Double, double[]> quadraticPartAsserter = (epsilon, expectedValue, segmentVarValues) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            epsilon);

    // First, make sure the values are correct for the corner cases where the individual line segment variables
    // are either at their min or at their max.
    // Those points aren't interpolated so they should be exact, therefore we'll use a tiny epsilon.
    quadraticPartAsserter.accept(1e-8, sq(0.12344),                             new double[] { 0,    0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0),                      new double[] { max0, 0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1),               new double[] { max0, max1, 0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1 + max2),        new double[] { max0, max1, max2, 0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1 + max2 + max3), new double[] { max0, max1, max2, max3 });

    // Now, take the midpoints of the 4 line segments. The approximation would be off by the largest amount there.
    // We will use bigger/coarser epsilons here. The epsilons were backfitted a bit here (i.e. I ran the tests and
    // then used epsilons as small as possible to make them pass), but they are still relatively small-ish
    // percentage-wise in comparison to the values being compared.
    quadraticPartAsserter.accept(0.0002, sq(doubleExplained(0.13672, 0.12344 + max0 / 2)), new double[] { 0.13672 - 0.12344, 0,           0,           0 });
    quadraticPartAsserter.accept(0.0070, sq(doubleExplained(0.23,    0.15    + max1 / 2)), new double[] { max0,              0.23 - 0.15, 0,           0 });
    quadraticPartAsserter.accept(0.0350, sq(doubleExplained(0.47,    0.31    + max2 / 2)), new double[] { max0,              max1,        0.47 - 0.31, 0 });
    quadraticPartAsserter.accept(0.0350, sq(doubleExplained(0.815,   0.63    + max3 / 2)), new double[] { max0,              max1,        max2,        0.815 - 0.63 });
  }

  @Test
  public void happyPath_xSquared_lowerBoundIsNonZero_existingVariable() {
    HighLevelVariablesBuilder builder = highLevelVariablesBuilder();
    double initialStep = 0.01;
    double stepMultiplier = 2.0;

    RawVariable rawVar = builder.addConstrainedRawVariable("x", Range.closed(0.12344, 1.0));
    SuperVar simplePositiveVar = builder.addSuperVar(GeneralSuperVar.superVarOfSingleVariable(rawVar));

    LinearApproximationVars linearApproximationVars = linearApproximationVars(
        simplePositiveVar,
        testGeneralSuperVarWithoutArtificialTerms(
            DUMMY_LABEL,
            highLevelVarExpression(
                ImmutableList.of(
                    highLevelVarWithWeight(rawVariable("x_0.1234_to_0.15", 1), doubleExplained(0.27344, getXSquaredSlope(0.12344, 0.15))),
                    highLevelVarWithWeight(rawVariable("x_0.15_to_0.31",  2), doubleExplained(0.46, getXSquaredSlope(0.15, 0.31))),
                    highLevelVarWithWeight(rawVariable("x_0.31_to_0.63",  3), doubleExplained(0.94, getXSquaredSlope(0.31, 0.63))),
                    highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",   4), doubleExplained(1.63, getXSquaredSlope(0.63, 1.00)))),
                // this way, if every x_? variable is at its lower bound of 0, the square of the original
                // expression will have a value of 0.12344, the lower bound
                constantTerm(doubleExplained(0.0152374336, 0.12344 * 0.12344))),
            self -> singletonList(
                highLevelVarsEqualZeroConstraint(
                    DUMMY_LABEL,
                    highLevelVarExpression(
                        ImmutableList.of(
                            highLevelVarWithWeight(rawVariable("x_0.1234_to_0.15", 1), 1),
                            highLevelVarWithWeight(rawVariable("x_0.15_to_0.31",   2), 1),
                            highLevelVarWithWeight(rawVariable("x_0.31_to_0.63",   3), 1),
                            highLevelVarWithWeight(rawVariable("x_0.63_to_1.0",    4), 1),
                            highLevelVarWithWeight(simplePositiveVar, -1)),
                        // this way, if every x_? variable is at its lower bound of 0, the original expression will have
                        // a value of 0.12344, the lower bound
                        constantTerm(0.12344))))),
        contiguousLinearApproximationIndividualSegmentSuperVars(ImmutableList.of(
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.1234_to_0.15", 1), Range.closedOpen(0.12344, 0.15), 0.27344),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.15_to_0.31", 2), Range.closedOpen(0.15,    0.31), 0.46),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.31_to_0.63", 3), Range.closedOpen(0.31,    0.63), 0.94),
            testLinearApproximationIndividualSegmentSuperVar(DUMMY_LABEL, rawVariable("x_0.63_to_1.0", 4), Range.closed(    0.63,    1.00), 1.63))));
    assertThat(
        generateQuadraticVarsOfExistingVariable(
            builder, "x", closedRange(0.12344, 1.0), initialStep, stepMultiplier, simplePositiveVar),
        linearApproximationVarsMatcher(linearApproximationVars));

    AllRawVariablesInOrder allRawVariablesInOrder = builder.getAllRawVariablesInOrderBuilder().build();
    BiConsumer<Double, Double> linearPartAsserter = (expectedValue, actualValueForExistingSuperVar) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, new double[] {
                    actualValueForExistingSuperVar,
                    DUMMY_DOUBLE,
                    DUMMY_DOUBLE,
                    DUMMY_DOUBLE,
                    DUMMY_DOUBLE })),
            1e-8);

    double d = 1e-2;
    double max0 = doubleExplained(0.02656, 0.15 - 0.12344);
    double max1 = doubleExplained(0.16, 0.31 - 0.15);
    double max2 = doubleExplained(0.32, 0.63 - 0.31);
    double max3 = doubleExplained(0.37, 1.00 - 0.63);

    for (double valueForLinearPart : ImmutableList.of(
        0.0,    d, max0 - d, max0,
        0.0,    d, max1 - d, max1,
        0.0,    d, max2 - d, max2,
        0.0,    d, max3 - d, max3)) {
      linearPartAsserter.accept(valueForLinearPart, valueForLinearPart);
    }

    // Let's assert on the monotonicity of the approximated squares. They should increase as the approximated linear
    // value (as expressed by the individual line segment vars) increases.
    RBIterables.consecutivePairsForEach(
        ImmutableList.of(
            new double[] { DUMMY_DOUBLE, 0,              0,           0,           0 },
            new double[] { DUMMY_DOUBLE, 0.13 - 0.12344, 0,           0,           0 },
            new double[] { DUMMY_DOUBLE, 0.14 - 0.12344, 0,           0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           0,           0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           0.16 - 0.15, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           0.24 - 0.15, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           0.30 - 0.15, 0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        0,           0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        0.32 - 0.31, 0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        0.48 - 0.31, 0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        0.62 - 0.31, 0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        max2,        0 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        max2,        0.64 - 0.63 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        max2,        0.82 - 0.63 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        max2,        0.99 - 0.63 },
            new double[] { DUMMY_DOUBLE, max0,           max1,        max2,        1.00 - 0.63 })
            .stream()
            .map(segmentVarValues -> makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)))
            .collect(Collectors.toList()),
        (val1, val2) -> assertTrue(val1 < val2));

    TriConsumer<Double, Double, double[]> quadraticPartAsserter = (epsilon, expectedValue, segmentVarValues) ->
        assertEquals(
            expectedValue,
            makeRealObject(HighLevelVarEvaluator.class).evaluateHighLevelVar(
                linearApproximationVars.getApproximatedNonLinearPart(),
                allRawVariablesAndOptimalValues(allRawVariablesInOrder, segmentVarValues)),
            epsilon);

    double sqAtMin = doubleExplained(0.0152374336, 0.12344 * 0.12344);
    // First, make sure the values are correct for the corner cases where the individual line segment variables
    // are either at their min or at their max.
    // Those points aren't interpolated so they should be exact, therefore we'll use a tiny epsilon.
    quadraticPartAsserter.accept(1e-8, sq(0.12344),                            new double[] { DUMMY_DOUBLE, 0,    0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0),                     new double[] { DUMMY_DOUBLE, max0, 0,    0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1),              new double[] { DUMMY_DOUBLE, max0, max1, 0,    0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1 + max2),       new double[] { DUMMY_DOUBLE, max0, max1, max2, 0 });
    quadraticPartAsserter.accept(1e-8, sq(0.12344 + max0 + max1 + max2+ max3), new double[] { DUMMY_DOUBLE, max0, max1, max2, max3 });

    // Now, take the midpoints of the 4 line segments. The approximation would be off by the largest amount there.
    // We will use bigger/coarser epsilons here. The epsilons were backfitted a bit here (i.e. I ran the tests and
    // then used epsilons as small as possible to make them pass), but they are still relatively small-ish
    // percentage-wise in comparison to the values being compared.
    quadraticPartAsserter.accept(0.0002, sq(doubleExplained(0.13672, 0.12344 + max0 / 2)), new double[] { DUMMY_DOUBLE, 0.13672 - 0.12344, 0,           0,           0 });
    quadraticPartAsserter.accept(0.0070, sq(doubleExplained(0.23,    0.15    + max1 / 2)), new double[] { DUMMY_DOUBLE, max0,              0.23 - 0.15, 0,           0 });
    quadraticPartAsserter.accept(0.0260, sq(doubleExplained(0.47,    0.31    + max2 / 2)), new double[] { DUMMY_DOUBLE, max0,              max1,        0.47 - 0.31, 0 });
    quadraticPartAsserter.accept(0.0350, sq(doubleExplained(0.815,   0.63    + max3 / 2)), new double[] { DUMMY_DOUBLE, max0,              max1,        max2,        0.815 - 0.63 });
  }

  // shorthand notation to keep tests legible
  private double sq(double x) { 
    return x * x;
  }
  
  private double getXSquaredSlope(double x1, double x2) {
    // This assumes that it's the quadratic function that we are approximating -
    // but that is indeed the case in any tests that use this.
    return (x2 * x2 - x1 * x1) / (x2 - x1);
  }

  private LinearApproximationVars generateQuadraticVars(
      HighLevelVariablesBuilder builder, String varPrefix, ClosedRange<Double> rangeForFinal,
      double initialStep, double stepMultiplier) {
    LinearApproximationVarRanges linearApproximationVarRanges =
        geometricallyIncreasingLinearApproximationVarRangesGenerator.calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(initialStep)
                .setStepMultiplier(stepMultiplier)
                .setBoundsForOriginalExpression(rangeForFinal)
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build());
    return makeRealObject().generateLinearApproximationOfSingleVariable(
        builder,
        varPrefix,
        linearApproximationVarRangesAndValues(linearApproximationVarRanges, quadraticFunctionDescriptor()));
  }

  private LinearApproximationVars generateQuadraticVarsOfExistingVariable(
      HighLevelVariablesBuilder builder, String varPrefix, ClosedRange<Double> rangeForFinal,
      double initialStep, double stepMultiplier, SuperVar existingVar) {
    LinearApproximationVarRanges linearApproximationVarRanges =
        geometricallyIncreasingLinearApproximationVarRangesGenerator.calculateGeometricallyIncreasingRanges(
            geometricallyIncreasingRangesGenerationInstructionsBuilder()
                .setInitialStep(initialStep)
                .setStepMultiplier(stepMultiplier)
                .setBoundsForOriginalExpression(rangeForFinal)
                .approximationStartsAt0()
                .doNotStopMultiplyingStepsUntilEndOfInterval()
                .build());
    return makeRealObject().generateLinearApproximationOfExistingHighLevelVar(
        builder,
        varPrefix,
        linearApproximationVarRangesAndValues(linearApproximationVarRanges, quadraticFunctionDescriptor()),
        existingVar);
  }

  @Override
  protected Class<LinearApproximationVarsGenerator> getClassBeingTested() {
    return LinearApproximationVarsGenerator.class;
  }
  
}
