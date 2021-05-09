package com.rb.nonbiz.types;

import com.rb.nonbiz.text.Strings;

/**
 * Represents 2 doubles, usually with 'before/after' semantics'.
 * Do not use this for price returns though; @see OnesBasedReturn.
 *
 * This is useful for e.g. looking at how eigenvalues jump around from one day to the next.
 */
public class DoubleMovement {

  private final double valueBefore;
  private final double valueAfter;

  private DoubleMovement(double valueBefore, double valueAfter) {
    this.valueBefore = valueBefore;
    this.valueAfter = valueAfter;
  }

  public static DoubleMovement doubleMovement(double valueBefore, double valueAfter) {
    return new DoubleMovement(valueBefore, valueAfter);
  }

  public double getValueBefore() {
    return valueBefore;
  }

  public double getValueAfter() {
    return valueAfter;
  }

  public double getFractionalJump() {
    return valueAfter == 0 ? 0 : (valueAfter - valueBefore) / valueBefore;
  }

  @Override
  public String toString() {
    return Strings.format("%s -> %s", valueBefore, valueAfter);
  }

}
