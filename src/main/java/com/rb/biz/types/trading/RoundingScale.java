package com.rb.biz.types.trading;

import com.google.common.collect.Range;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;
import static java.math.BigDecimal.TEN;

/**
 * <p> A specification of how many digits of precision to use. </p>
 *
 * <p> For example, if x = 123.1234, then the effect of each
 * of the following rounding scales would be: </p>
 * <ol start = 0>
 *   <li> <pre>  x = 123     </pre> </li>
 *   <li> <pre>  x = 123.1   </pre> </li>
 *   <li> <pre>  x = 123.12  </pre> </li>
 *   <li> <pre>  x = 123.123 </pre> </li>
 * </ol>
 *
 * <p> Rounding scales can also be negative, indicating rounding to powers of ten: </p>
 * <ol start = -2>
 *   <li> <pre>  x = 100 </pre> </li>
 *   <li> <pre>  x = 120 </pre> </li>
 * </ol>
 *
 */
public class RoundingScale implements Comparable<RoundingScale> {

  public static final RoundingScale INTEGER_ROUNDING_SCALE = roundingScale(0);

  private static final int MIN_ROUNDING_SCALE = -10;
  private static final int MAX_ROUNDING_SCALE =  10;

  private final int roundingScale;

  // precompute the values (powers of 10) corresponding to the allowable rounding scales, both as BigDecimal and as double
  // (so that if we need to access them as double, we won't always need to convert the BigDecimals).
  private final static List<BigDecimal> roundingScaleValuesAsBigDecimals;
  private final static List<Double> roundingScaleValuesAsDoubles;

  static {
    roundingScaleValuesAsBigDecimals = IntStream.range(0, MAX_ROUNDING_SCALE - MIN_ROUNDING_SCALE + 1)
        .map(i -> i - MAX_ROUNDING_SCALE)
        .boxed()
        .map(n -> n <= 0
            ? BigDecimal.TEN.pow(-n)
            : BigDecimal.ONE.divide(TEN.pow(n), DEFAULT_MATH_CONTEXT))
        .collect(Collectors.toCollection(ArrayList::new));
    roundingScaleValuesAsDoubles = roundingScaleValuesAsBigDecimals
        .stream()
        .map(v -> v.doubleValue())
        .collect(Collectors.toList());
  }

  protected RoundingScale(int roundingScale) {
    RBPreconditions.checkInRange(
        roundingScale,
        Range.closed(MIN_ROUNDING_SCALE, MAX_ROUNDING_SCALE),
        "Should have min_rounding_scale %s <= scale %s <= max_rounding_scale %s",
        MIN_ROUNDING_SCALE, roundingScale, MAX_ROUNDING_SCALE);
    this.roundingScale = roundingScale;
  }

  public static RoundingScale roundingScale(int roundingScale) {
    return new RoundingScale(roundingScale);
  }

  public int getRawInt() {
    return roundingScale;
  }

  public BigDecimal getAsValue() {
    // instead of computing Math.pow(10, -n), look up the pre-computed value
    return roundingScaleValuesAsBigDecimals.get(roundingScale + MAX_ROUNDING_SCALE);
  }

  public double getPowerOf10AsDouble() {
    // instead of computing Math.pow(10, -n), look up the pre-computed value
    return roundingScaleValuesAsDoubles.get(roundingScale + MAX_ROUNDING_SCALE);
  }

  public double getPowerOf10InverseAsDouble() {
    // instead of computing Math.pow(10, n), look up the pre-computed value
    return roundingScaleValuesAsDoubles.get(-roundingScale + MAX_ROUNDING_SCALE);
  }

  /**
   * Implements the usual imperfect rounding of scaling down a number, rounding it as an int, and scaling it up.
   * We add 'simplistically' to the name of this method, just to be clear to the caller. In many cases this is good
   * enough (e.g. writing numbers to JSON files for visualizations).
   */
  public double roundSimplistically(double value) {
    // use two multiplications for improved accuracy (and speed)
    return Math.round(value * getPowerOf10InverseAsDouble()) * getPowerOf10AsDouble();
  }

  @Override
  public int compareTo(RoundingScale other) {
    return Integer.compare(roundingScale, other.roundingScale);
  }

  @Override
  public String toString() {
    return Strings.format("[RS %s RS]", roundingScale);
  }

}
