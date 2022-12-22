package com.rb.nonbiz.types;

/**
 * This is a common base class for {@link ImpreciseValue} and {@link PreciseValue} which allows us to write methods
 * that work both on them. Moreover, because RBNumeric extends Number, it means that we can *also* write methods that
 * can work on all 3 of PreciseValue, ImpreciseValue, and Double - although those may look a bit hacky because
 * technically they should also support long, int, etc., which also extend Number.
 *
 * <p> Moreover, Java's {@link Comparable} interface has well-defined semantics, but it's hard to read.
 * {@code foo1.compareTo(foo2) < 0 }
 * is harder to read than
 * {@code foo1.isLessThan(foo2) }
 *
 * This class adds some intuitive comparison methods that are easier to read. </p>
 */
public abstract class RBNumeric<T> extends Number implements Comparable<T> {

  public boolean isGreaterThan(T other) {
    return this.compareTo(other) > 0;
  }

  public boolean isGreaterThanOrEqualTo(T other) {
    return this.compareTo(other) >= 0;
  }

  public boolean isLessThan(T other) {
    return this.compareTo(other) < 0;
  }

  public boolean isLessThanOrEqualTo(T other) {
    return this.compareTo(other) <= 0;
  }

}
