package com.rb.nonbiz.math;

import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.util.RBPreconditions;

/**
 * A typesafe representation of an angle.
 *
 * <p> This breaks the convention of dumb immutable classes, but is handy because it lazy-evaluates cosine (from degrees)
 * or degrees (from cosine). These conversions are heavy operation, so it's good to avoid when possible. </p>
 *
 * <p> Also, depending on how you view it, this is still immutable, because once the calculation happens,
 * it won't happen again. It's not like there is some field that can be modified with a setter. </p>
 *
 * <p> {@link PreciseValue} also follows this pattern for supporting both a BigDecimal and a double version. </p>
 */
public class Angle {

  public static final Angle RIGHT_ANGLE = angleInDegrees(90);

  private Double cosine;
  private Double degrees;

  private Angle(Double cosine, Double degrees) {
    this.cosine = cosine;
    this.degrees = degrees;
  }

  /**
   * Use this when you know the cosine of the angle, but not the angle in degrees.
   */
  public static Angle angleWithCosine(double cosine) {
    if (-1 - 1e-8 < cosine && cosine < -1) {
      cosine = -1;
    } else if (1 < cosine && cosine < 1 + 1e-8) {
      cosine = 1;
    }
    RBPreconditions.checkArgument(
        -1 <= cosine && cosine <= 1,
        "cosine must be between -1 and 1 but was %s",
        cosine);
    return new Angle(cosine, null);
  }

  /**
   * Use this when you know the angle in degrees, but not the cosine of the angle.
   */
  public static Angle angleInDegrees(double degrees) {
    RBPreconditions.checkArgument(
        -180 <= degrees && degrees <= 180,
        "Angle in degrees must be between -180 and 180 degrees but was %s",
        degrees);
    return new Angle(null, degrees);
  }

  synchronized public double getCosine() {
    if (cosine == null) {
      cosine = Math.cos(Math.toRadians(degrees));
    }
    return cosine;
  }

  synchronized public double getAngleInDegrees() {
    if (degrees == null) {
      degrees = Math.toDegrees(Math.acos(cosine));
    }
    return degrees;
  }

  @Override
  public String toString() {
    return Strings.format("%s°", getAngleInDegrees());
  }

}
