package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

import java.util.OptionalDouble;

import static com.rb.nonbiz.types.UnitFraction.unitFraction;

/**
 * Represents fractions with values between 0 and 1, inclusive, by storing both the numerator
 * and denominator as integers.
 *
 * <p> Like {@link UnitFraction}, except that: </p>
 * <ol type="a">
 *  <li> this is in Q (both numerator and denominator are integers) </li>
 *  <li> beyond just storing a single value of numerator / denominator,
 *    this actually remembers the numerator and denominator. </li>
 * </ol>
 *
 * <p> It's better to use this in cases where we want to represent some portion of a population
 * e.g. we traded on <i>k</i> out <i>m</i> days. </p>
 *
 * @see UnitFraction
 */
public class RationalUnitFraction extends PreciseValue<RationalUnitFraction> {

  public static final RationalUnitFraction ZERO_RATIONAL_FRACTION = new RationalUnitFraction(0, 1);

  private final int numerator;
  private final int denominator;

  private RationalUnitFraction(int numerator, int denominator) {
    super(unitFraction(numerator, denominator).asBigDecimal());
    this.numerator = numerator;
    this.denominator = denominator;
  }

  public static RationalUnitFraction rationalUnitFraction(int numerator, int denominator) {
    if (numerator < 0 || denominator <= 0) {
      throw new IllegalArgumentException(Strings.format(
          "A rational unitFraction must have numerator >= 0 and denominator > 0; got %s / %s",
          numerator, denominator));
    }
    return new RationalUnitFraction(numerator, denominator);
  }

  public UnitFraction asUnitFraction() {
    return unitFraction(asBigDecimal());
  }

  public OptionalDouble inverse() {
     return numerator == 0
         ? OptionalDouble.empty()
         : OptionalDouble.of((double) denominator / numerator);
  }

  public int getNumerator() {
    return numerator;
  }

  public int getDenominator() {
    return denominator;
  }

  @Override
  public boolean isZero() {
    return numerator == 0;
  }

  public boolean isOne() {
    return numerator == denominator;
  }

  @Override
  public String toString() {
    return Strings.format("%s / %s ( %s )",
        numerator, denominator, asUnitFraction().toPercentString());
  }

}
