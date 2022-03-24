package com.rb.biz.investing.modeling.selection.overrides;

/**
 * Describes what we should do in cases where there is both a value and an override.
 *
 * @see Overrides
 */
public abstract class BehaviorWithValueAndOverride<T extends Comparable<? super T>> {

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
