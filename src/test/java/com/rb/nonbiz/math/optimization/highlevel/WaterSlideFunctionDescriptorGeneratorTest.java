package com.rb.nonbiz.math.optimization.highlevel;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.BitSet2D;
import com.rb.nonbiz.testcategories.DataPrinters;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import static com.rb.nonbiz.collections.BitSet2D.bitSet2D;
import static com.rb.nonbiz.math.optimization.highlevel.X90.x90;
import static com.rb.nonbiz.math.optimization.highlevel.X90Test.x90Matcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.doubleListMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBIntegrationTest.makeRealObject;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class WaterSlideFunctionDescriptorGeneratorTest
    extends RBTest<WaterSlideFunctionDescriptorGenerator> {

  /**
   * This is f(x_90), if we use the notation in the comments in the prod class. Here is why this formula holds.
   *
   * From the prod class, we know that:
   * <pre>
   * (1) {@code f(x) = d * [sqrt(1 + (x / d) ^ 2) - 1]}
   * (2) {@code x_90 = 2.06474160484 * d.}
   * </pre>
   *
   * Therefore, from (1),
   *
   * <pre>
   * {@code
   * f(x90) = d * [sqrt(1 + (x90 / d) ^ 2) - 1]
   *        = d * (sqrt(1 + 2.06474160484 ^ 2) - 1)
   *        = d * 1.29415733871
   * }
   * </pre>
   */
  public static double getWaterSlideValueAtX90(double divisor) {
    return 1.29415733871 * divisor;
  }

  @Test
  public void generalCase() {
    // We could use this for an asset class with a target of e.g. 35%,
    // so that the penalty gets effectively linear (90% of the final linear slope, to be exact)
    // once we have misallocation of 15%, i.e. we start holding 50% of that asset class vs the target of 35%.
    WaterSlideFunctionDescriptor descriptor = makeTestObject().generate(unitFraction(0.9), x90(0.15));
    DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(descriptor);
    double linearSlopeAtLimit = fPrime.applyAsDouble(999);
    assertEquals(
        "The slope of the water slide function at x = 0.15 should be 90% of the final linear slope",
        0.9,
        fPrime.applyAsDouble(0.15) / linearSlopeAtLimit,
        1e-8);
  }

  @Test
  public void valueAtPointOf90pctSlopeIsAlwaysSomeSimpleFunctionOfTheDivisor() {
    for (double pointOf90pctSlope : ImmutableList.of(0.05, 0.10, 0.15, 0.20)) {
      WaterSlideFunctionDescriptor f = makeTestObject().generate(unitFraction(0.9), x90(pointOf90pctSlope));
      DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(f);
      assertEquals(
          0.9,
          fPrime.applyAsDouble(pointOf90pctSlope) / fPrime.applyAsDouble(999),
          1e-8);
      assertEquals(
          getWaterSlideValueAtX90(f.getDivisor()),
          f.asFunction().applyAsDouble(pointOf90pctSlope),
          1e-8);
    }
  }

  /**
   * We can rewrite some of the formulas in the prod code comments as:
   *
   * <pre>
   *   {@code x_q = d * sqrt(q ^ 2 / (1 - q ^ 2))}
   * </pre>
   *
   * For x90 (i.e. q = 0.9), which becomes
   * <pre>
   *    {@code <==> w =}
   *    {@code <==> x = sqrt(0.81 / 0.19) * d ~= 2.06474160484 * d}
   * </pre>
   *
   * That x would be the 'x50' point.
   */
  @Test
  public void testXPoints() {
    for (X90 x90 : ImmutableList.of(x90(0.05), x90(0.10), x90(0.15), x90(0.20))) {
      WaterSlideFunctionDescriptor f = makeTestObject().generate(unitFraction(0.9), x90);
      DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(f);

      Arrays.stream(new double[][] {
          // x_i = multiplier * divisor (x_i as in x90, x80, etc.)
          // i    multiplier
          { 0.1,  0.1005037815 },
          { 0.2,  0.2041241452 },
          { 0.3,  0.314485451 },
          { 0.4,  0.4364357805 },
          { 0.45, 0.5039032599 },
          { 0.5,  0.5773502692 },
          { 0.6,  0.75 },
          { 0.7,  0.9801960588 },
          { 0.8,  1.3333333333 },
          { 0.9,  2.0647416048 },

          { 0.95, 3.042435 },
          { 0.98, 4.924685 },
          { 0.995, 9.96247 },
          { 0.9950371902099892, 10.0 }
      }).forEach(testLine -> {
            double derivativeAtXPoint = testLine[0];
            double multiplier = testLine[1];

            assertEquals(
                derivativeAtXPoint,
                fPrime.applyAsDouble(f.getDivisor() * multiplier),
                1e-8);
          });
    }
  }

  /**
   * This demonstrates that, regardless of the water slide function's divisor,
   *   x_i  /   x90  is constant (for e.g. i = 80)
   * f(x_i) / f(x90) is constant (for e.g. i = 80)
   * ... and shows a table of the ratios, so we can get a better understanding of how the function looks.
   */
  @Test
  public void waterSlideFunctionValueRatiosDoNotDependOnDivisor() {
    // This demonstrates how the x_*0 points (e.g. x10, x20, ... x90) are spaced against each other.
    // Since the water slide function slowly tends towards linear, the x points will be denser on the left side
    // than on the right side.
    // The underscores are in a weird location, but they make it easier to see 2 relevant digits.
    List<Double> expectedXRatios = ImmutableList.of(
        0.0,                   // x0  / x90
        0.04_8676203000243626, // x10 / x90
        0.09_886183565149144,  // x20 / x90
        0.15_231225556920136,  // x30 / x90
        0.21_1375495838311,    // x40 / x90
        0.27_962349760764477,  // x50 / x90
        0.36_32415786283894,   // x60 / x90
        0.47_473061835178354,  // x70 / x90
        0.64_57628064343259,   // x80 / x90
        1.0,                   // x90 / x90
        1.4735185230323518,    // x95 / x90
        2.3851338048634,       // x98 / x90
        4.8250444397839605);   // x99_5 / x90

    // This demonstrates the function values at each x_*0 point (x10, x20, etc.) as a fraction of the value at the x90.
    // Note that about half (0.5151357155097042) of the function value is reached at x80.
    // The previous table shows that x80 is about 2/3 (0.6457628064343259) of the way between 0 and x90.
    // This says e.g. for an asset class with a 10% target and an x90 of 5%, going from a misallocation of
    // 2 / 3 * 5% = 3.33% to a misallocation of 5% is twice as bad, according to this model.
    List<Double> expectedYRatios = ImmutableList.of(
        0.0,                    // f(x0)  / f(x90)
        0.00_38927378503926403, // f(x10) / f(x90)
        0.01_5933708782579853,  // f(x20) / f(x90)
        0.03_730986586717199,   // f(x30) / f(x90)
        0.07_038514442493826,   // f(x40) / f(x90)
        0.11_953765880038787,   // f(x50) / f(x90)
        0.19_317589332386606,   // f(x60) / f(x90)
        0.30_92978512368467,    // f(x70) / f(x90)
        0.51_51357155097042,    // f(x80) / f(x90)
        1.0,                    // f(x90) / f(x90)
        1.7019284163105441,     // f(x95) / f(x90)
        3.110277759153469,      // f(x98) / f(x90)
        6.964016122484251);     // f(x99_5) / f(x90)

    for (X90 x90 : ImmutableList.of(x90(0.05), x90(0.10), x90(0.15), x90(0.20))) {
      WaterSlideFunctionDescriptor f = makeTestObject().generate(unitFraction(0.9), x90);
      DoubleUnaryOperator fPrime = makeRealObject(WaterSlideFunctionFirstDerivativeGenerator.class).takeFirstDerivative(f);
      double d = f.getDivisor();

      // #see testXPoints for an explanation of these constants.
      List<Double> x_q = ImmutableList.of(
          0.0,              // x0
          0.1005037815 * d, // x10
          0.2041241452 * d, // x20
          0.314485451  * d, // x30
          0.4364357805 * d, // x40
          0.5773502692 * d, // x50
          0.75         * d, // x60
          0.9801960588 * d, // x70
          1.3333333333 * d, // x80
          2.0647416048 * d, // x90
          3.042435     * d, // x95
          4.924685     * d, // x98
          9.96247      * d);// x99_5
      assertThat(
          x90(2.0647416048 * d),
          x90Matcher(x90));
      List<Double> calculatedY = x_q
          .stream()
          .map(v -> f.asFunction().applyAsDouble(v))
          .collect(Collectors.toList());
      assertThat(
          "Confirming that the slope at each x_q point is correct",
          ImmutableList.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.95, 0.98, 0.995),
          doubleListMatcher(
              x_q
                  .stream()
                  .map(v -> fPrime.applyAsDouble(v))
                  .collect(Collectors.toList()),
              1e-8));

      List<Double> calculatedXRatios = x_q
          .stream()
          .map(v -> v / x90.getRawDouble())
          .collect(Collectors.toList());
      double y90 = f.asFunction().applyAsDouble(x90.getRawDouble());
      List<Double> calculatedYRatios = calculatedY
          .stream()
          .map(v -> v / y90)
          .collect(Collectors.toList());

      assertThat(calculatedXRatios, doubleListMatcher(expectedXRatios, 1e-8));
      assertThat(calculatedYRatios, doubleListMatcher(expectedYRatios, 1e-8));
    }
  }

  /**
   * This prints a cheesy ascii version of the water slide function from 0 to x90,
   * where the top-right corner is (x90, f(x90)).
   *
   * For a real plot, take a look at this:
   * It plots the water slide function so that x90 = 1.
   * It also plots a straight line so you can see the curvature a bit better.
   *
   * http://www.wolframalpha.com/input/?i=plot+0.4843*%5Bsqrt(1+%2B+(x+%2F+0.4843)%5E2)+-+1%5D+and+0.62680147601*x+from+0+to+1
   *
   * The bold dots are the water slide function.
   * The steep line is x = y.
   * I had to stretch the 'chart' in the y dimension because printed characters are taller than they are wide.
   * The flatter incline just connects the endpoints of the water slide curve, so it's easier to see the curvature.
   |                                             ·                                            •
   |                                            ·                                           ·•
   |                                           ·                                          ·••
   |                                          ·                                         · •
   |                                         ·                                        ·  •
   |                                        ·                                       ·  ••
   |                                       ·                                      ·   •
   |                                      ·                                     ·   ••
   |                                     ·                                    ·    •
   |                                    ·                                   ·    ••
   |                                   ·                                  ·     •
   |                                  ·                                 ·      •
   |                                 ·                                ·      ••
   |                                ·                               ·       •
   |                               ·                              ·       ••
   |                              ·                             ·        •
   |                             ·                            ·        ••
   |                            ·                           ·         •
   |                           ·                          ·         ••
   |                          ·                         ·          •
   |                         ·                        ·          ••
   |                        ·                       ·           •
   |                       ·                      ·           ••
   |                      ·                     ·           ••
   |                     ·                    ·            •
   |                    ·                   ·            ••
   |                   ·                  ·             •
   |                  ·                 ·             ••
   |                 ·                ·             ••
   |                ·               ·             ••
   |               ·              ·              •
   |              ·             ·              ••
   |             ·            ·              ••
   |            ·           ·              ••
   |           ·          ·              ••
   |          ·         ·              ••
   |         ·        ·              ••
   |        ·       ·              ••
   |       ·      ·              ••
   |      ·     ·              ••
   |     ·    ·             •••
   |    ·   ·            •••
   |   ·  ·           •••
   |  · ·          •••
   | ··       •••••
   |••••••••••
   -------------------------------------------------------------------------------------------
   */
  @Test
  @Category(DataPrinters.class)
  public void printAsciiQuadraticWaterSlideFunction() {
    int size = 90;
    BitSet2D bitset = bitSet2D(size / 2 + 1, size + 1);
    X90 x90 = x90(0.05);
    DoubleUnaryOperator f = makeTestObject().generate(unitFraction(0.9), x90).asFunction();
    double y90 = f.applyAsDouble(x90.getRawDouble());
    for (int i = 0; i <= size; i++) {
      double x_i = i * x90.getRawDouble() / 90;
      double y_i = f.applyAsDouble(x_i);
      bitset.set((int) Math.round(size * y_i / y90) / 2, i);
    }
    for (int row = bitset.getNumRows() - 1; row >= 0; row--) {
      StringBuilder sb = new StringBuilder("|");
      for (int col = 0; col < bitset.getNumColumns(); col++) {
        sb.append(
            bitset.get(row, col) ? '•'
            : 2 * row == col ? '·'
                : row == col ? '·' : ' ');
      }
      System.out.println(sb.toString());
    }
    StringBuilder sb = new StringBuilder("+");
    for (int col = 0; col < bitset.getNumColumns(); col++) {
      sb.append('-');
    }
    System.out.println(sb.toString());
  }

  @Test
  public void throwsForFractionOfAlmostZeroOrOne() {
    assertIllegalArgumentException( () -> makeTestObject().generate(UNIT_FRACTION_0, x90(0.15)));
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(1e-9), x90(0.15)));
    WaterSlideFunctionDescriptor doesNotThrow;
    doesNotThrow = makeTestObject().generate(unitFraction(1e-7), x90(0.15));
    doesNotThrow = makeTestObject().generate(unitFraction(0.1), x90(0.15));
    doesNotThrow = makeTestObject().generate(unitFraction(0.9), x90(0.15));
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(1 - 1e-9), x90(0.15)));
    assertIllegalArgumentException( () -> makeTestObject().generate(UNIT_FRACTION_1, x90(0.15)));
  }

  @Test
  public void xWhereDerivativeValueIsAchievedMustBePositiveBeyondEpsilon() {
    WaterSlideFunctionDescriptor doesNotThrow;
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(0.9), x90(-999)));
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(0.9), x90(-1e-9)));
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(0.9), x90(0)));
    assertIllegalArgumentException( () -> makeTestObject().generate(unitFraction(0.9), x90(1e-9)));
    doesNotThrow = makeTestObject().generate(unitFraction(0.9), x90(1e-7));
    doesNotThrow = makeTestObject().generate(unitFraction(0.9), x90(0.1));
  }

  @Override
  protected WaterSlideFunctionDescriptorGenerator makeTestObject() {
    return new WaterSlideFunctionDescriptorGenerator();
  }

}
