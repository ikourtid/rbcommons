package com.rb.biz.investing.modeling.selection.overrides;

/**
 * Describes what we should do in cases where there is both a value and an override.
 *
 * <p> The 3 different method names are self-explanatory about what they do. The option to 'further reduce'
 * is the least obvious one. Here is an example. Our backtesting code (not part of this repo) supports 3 modes for
 * any instrument in the backtest: untradable; only sellable; freely tradable. Note that the generic class T
 * implements {@link Comparable}, so there is a requirement for ordering here. </p>
 *
 * <p> Say that the default value is specified as 'untradable'. If an override says 'AAPL is freely tradable',
 * then AAPL should be 'freely tradable'. If an override says 'XYZ is untradable', then XYZ will be 'only sellable'.
 * </p>
 *
 * @see BehaviorWithValueButNoOverride
 * @see Overrides
 * @see OverridesApplier
 */
public abstract class BehaviorWithValueAndOverride<T extends Comparable<? super T>> {

  /**
   * <p> Such Visitor classes are basically a way to implement Java 17's 'sealed classes' that works
   * in previous Java versions and does not require language support. </p>
   *
   * <p> There are almost 100 such 'visitors' across the entire Rowboat codebase.
   * This comment does not get repeated on all such visitor classes, but it's helpful for anyone going through
   * this codebase sequentially to get a feel for how things are written. </p>
   */
  public interface Visitor<T> {

    T visitAlwaysUseOverrideAndIgnoreExistingValue();
    T visitAlwaysUseExistingValueAndIgnoreOverride();
    T visitOnlyUseOverrideToFurtherReduceExistingValue();

  }

  abstract T visit(Visitor<T> visitor);


  public static class AlwaysUseOverrideAndIgnoreExistingValue<T extends Comparable<? super T>>
      extends BehaviorWithValueAndOverride<T> {

    public static <T extends Comparable<? super T>> AlwaysUseOverrideAndIgnoreExistingValue<T> alwaysUseOverrideAndIgnoreExistingValue() {
      return new AlwaysUseOverrideAndIgnoreExistingValue<>();
    }

    @Override
    T visit(Visitor<T> visitor) {
      return visitor.visitAlwaysUseOverrideAndIgnoreExistingValue();
    }

    @Override
    public String toString() {
      return "always_use_override_and_ignore_existing_value";
    }

  }


  public static class AlwaysUseExistingValueAndIgnoreOverride<T extends Comparable<? super T>>
      extends BehaviorWithValueAndOverride<T> {

    public static <T extends Comparable<? super T>> AlwaysUseExistingValueAndIgnoreOverride<T> alwaysUseExistingValueAndIgnoreOverride() {
      return new AlwaysUseExistingValueAndIgnoreOverride<>();
    }

    @Override
    T visit(Visitor<T> visitor) {
      return visitor.visitAlwaysUseExistingValueAndIgnoreOverride();
    }

    @Override
    public String toString() {
      return "always_use_existing_value_and_ignore_override";
    }

  }


  public static class OnlyUseOverrideToFurtherReduceExistingValue<T extends Comparable<? super T>>
      extends BehaviorWithValueAndOverride<T> {

    public static <T extends Comparable<? super T>> OnlyUseOverrideToFurtherReduceExistingValue<T>
    onlyUseOverrideToFurtherReduceExistingValue() {
      return new OnlyUseOverrideToFurtherReduceExistingValue<>();
    }

    @Override
    T visit(Visitor<T> visitor) {
      return visitor.visitOnlyUseOverrideToFurtherReduceExistingValue();
    }

    @Override
    public String toString() {
      return "only_use_override_to_further_reduce_existing_value";
    }

  }

}
