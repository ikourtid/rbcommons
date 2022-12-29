package com.rb.nonbiz.types;

import com.rb.nonbiz.types.RBDoubles.EpsilonComparisonVisitor;

import static com.rb.nonbiz.types.RBDoubles.epsilonCompareDoubles;

/**
 * Various utilities pertaining to {@link ImpreciseValue}.
 *
 * @see ImpreciseValue
 */
public class ImpreciseValues {

  /**
   * This method gives an abstraction over the Comparable::compareTo, which returns negative / 0 / positive,
   * and also lets you do a comparison subject to an epsilon.
   */
  public static <T1 extends ImpreciseValue<T1>, T2> T2 epsilonCompareImpreciseValues(
      T1 left, T1 right, Epsilon epsilon, EpsilonComparisonVisitor<T2> visitor) {
    return epsilonCompareDoubles(left.doubleValue(), right.doubleValue(), epsilon, visitor);
  }

}
