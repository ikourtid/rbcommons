package com.rb.nonbiz.functional;

import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.util.function.BiPredicate;

import static com.rb.biz.investing.modeling.RBCommonsConstants.DEFAULT_MATH_CONTEXT;

public class RBBiPredicates {

  public static <C extends Comparable<? super C>> BiPredicate<C, C> isGreaterThan() {
    return (c1, c2) -> c1.compareTo(c2) > 0;
  }

  public static <C extends Comparable<? super C>> BiPredicate<C, C> isGreaterThanOrEqualTo() {
    return (c1, c2) -> c1.compareTo(c2) >= 0;
  }

  public static <C extends Comparable<? super C>> BiPredicate<C, C> isLessThan() {
    return (c1, c2) -> c1.compareTo(c2) < 0;
  }

  public static <C extends Comparable<? super C>> BiPredicate<C, C> isLessThanOrEqualTo() {
    return (c1, c2) -> c1.compareTo(c2) <= 0;
  }

  public static <C extends Comparable<? super C>> BiPredicate<C, C> isEqualTo() {
    return (c1, c2) -> c1.compareTo(c2) == 0;
  }

  public static BiPredicate<Double, Double> doubleIsAlmostEqualTo(double epsilon) {
    return (c1, c2) -> Math.abs(c1 - c2) <= epsilon;
  }

  public static <V extends PreciseValue<? super V>> BiPredicate<V, V> isAlmostEqualTo(double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return (v1, v2) -> Math.abs(v1.doubleValue() - v2.doubleValue()) <= epsilon;
  }

  public static <T extends RBNumeric<? super T>> BiPredicate<T, T> isAlmostMultipliedBy(
      double expectedMultiplier, double epsilon) {
    RBPreconditions.checkArgument(epsilon >= 0);
    return (v1, v2) -> {
      // Note that we intentionally use a tiny epsilon of 1e-8, instead of the epsilon being passed in,
      // which isn't guarantee to be tiny, and also has a different purpose.
      // 1e-8 means "number is small and likely to cause instability in results, or even divide-by-zero problems".
      // The 'epsilon' parameter is the predicate itself, i.e. to control how tightly the ratio should hug
      // the 1st parameter (multiplier).
      if (Math.abs(v1.doubleValue()) < 1e-8) {
        // avoid division by zero
        return Math.abs(v2.doubleValue()) <= 1e-8;
      }

      return Math.abs(v2.doubleValue() / v1.doubleValue() - expectedMultiplier) <= epsilon;
    };
  }

  public static BiPredicate<Double, Double> doubleIsWithin(UnitFraction unitFraction) {
    return (v1, v2) -> {
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 <= 1e-8;
      }
      return Math.abs((v2 - v1) / v1) <= unitFraction.doubleValue();
    };
  }

  public static <V extends PreciseValue<? super V>> BiPredicate<V, V> isWithin(UnitFraction unitFraction) {
    return (p1, p2) -> {
      BigDecimal v1 = p1.asBigDecimal();
      BigDecimal v2 = p2.asBigDecimal();
      if (v1.signum() == 0) {
        return v2.signum() == 0;
      }
      return v1.subtract(v2).divide(v1, DEFAULT_MATH_CONTEXT).abs().compareTo(unitFraction.asBigDecimal()) <= 0;
    };
  }

  public static <V extends PreciseValue<? super V>> BiPredicate<V, V> isNotWithin(UnitFraction unitFraction) {
    return not(RBBiPredicates.<V>isWithin(unitFraction));
  }

  public static BiPredicate<Double, Double> doubleMustNotIncreaseByMoreThan(UnitFraction unitFraction) {
    return (v1, v2) -> {
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 <= 1e-8;
      }
      return (v2 - v1) / Math.abs(v1) <= unitFraction.doubleValue();
    };
  }

  public static BiPredicate<Double, Double> doubleMustNotDecreaseByMoreThan(UnitFraction unitFraction) {
    return (v1, v2) -> {
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 >= -1e-8;
      }
      return (v1 - v2) / Math.abs(v1) <= unitFraction.doubleValue();
    };
  }

  public static BiPredicate<Double, Double> doubleMustIncreaseByAtLeast(UnitFraction unitFraction) {
    return (v1, v2) -> {
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 >= -1e-8;
      }
      return (v2 - v1) / Math.abs(v1) >= unitFraction.doubleValue();
    };
  }

  public static <T extends RBNumeric<? super T>> BiPredicate<T, T> mustNotIncreaseByMoreThan(UnitFraction unitFraction) {
    return (p1, p2) -> {
      double v1 = p1.doubleValue();
      double v2 = p2.doubleValue();

      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 <= 0;
      }
      return (v2 - v1) / Math.abs(v1) <= unitFraction.doubleValue();
    };
  }

  public static <T extends RBNumeric<? super T>> BiPredicate<T, T> mustIncreaseByAtLeast(UnitFraction unitFraction) {
    return (p1, p2) -> {
      double v1 = p1.doubleValue();
      double v2 = p2.doubleValue();
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 >= 0;
      }
      return (v2 - v1) / Math.abs(v1) >= unitFraction.doubleValue();
    };
  }

  public static <T extends RBNumeric<? super T>> BiPredicate<T, T> mustDecreaseByAtLeast(UnitFraction unitFraction) {
    return (p1, p2) -> {
      double v1 = p1.doubleValue();
      double v2 = p2.doubleValue();
      if (Math.abs(v1) < 1e-8) { // avoid division by zero
        // If we go from 0 to any number v2, then we are either increasing infinitely (if v2 > 0)
        // or decreasing infinitely (if v2 < 0). There's no other in-between scenario. So the following is the best
        // possible answer.
        return v2 < 0;
      }
      return (v1 - v2) / v1 <= unitFraction.doubleValue();
    };
  }

  public static <T extends RBNumeric<? super T>> BiPredicate<T, T> mustNotDecreaseByMoreThan(UnitFraction unitFraction) {
    return (p1, p2) -> {
      double v1 = p1.doubleValue();
      double v2 = p2.doubleValue();
      if (Math.abs(v1) < 1e-8) {
        // avoid division by zero
        return v2 >= 0;
      }
      return (v1 - v2) / Math.abs(v1) <= unitFraction.doubleValue();
    };
  }

  public static <T1, T2> BiPredicate<T1, T2> not(BiPredicate<T1, T2> biPredicate) {
    return (v1, v2) -> !biPredicate.test(v1, v2);
  }

}
