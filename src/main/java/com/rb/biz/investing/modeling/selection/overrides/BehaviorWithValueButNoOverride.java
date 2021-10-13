package com.rb.biz.investing.modeling.selection.overrides;

/**
 * Describes what we should do in cases where no override exists for a value.
 *
 * @see Overrides
 */
public abstract class BehaviorWithValueButNoOverride<T> {

  public interface Visitor<T> {

    T visitUseExistingValueWhenOverrideMissing();
    T visitUseFixedValueWhenOverrideMissing(T fixedValue);

  }

  public abstract T visit(Visitor<T> visitor);

  public static class UseExistingValueWhenOverrideMissing<T> extends BehaviorWithValueButNoOverride<T> {

    public static <T> UseExistingValueWhenOverrideMissing<T> useExistingValueWhenOverrideMissing() {
      return new UseExistingValueWhenOverrideMissing<>();
    }

    @Override
    public T visit(Visitor<T> visitor) {
      return visitor.visitUseExistingValueWhenOverrideMissing();
    }

    @Override
    public String toString() {
      return "use_existing_value_when_override_missing";
    }

  }

  public static class UseFixedValueWhenOverrideMissing<T> extends BehaviorWithValueButNoOverride<T> {

    private final T fixedValueToUse;

    private UseFixedValueWhenOverrideMissing(T fixedValueToUse) {
      this.fixedValueToUse = fixedValueToUse;
    }

    public static <T> UseFixedValueWhenOverrideMissing<T> useFixedValueWhenOverrideMissing(
        T fixedValueToUse) {
      return new UseFixedValueWhenOverrideMissing<>(fixedValueToUse);
    }

    public T getFixedValueToUse() {
      return fixedValueToUse;
    }

    @Override
    public T visit(Visitor<T> visitor) {
      return visitor.visitUseFixedValueWhenOverrideMissing(fixedValueToUse);
    }

    @Override
    public String toString() {
      return "use_fixed_value_when_override_missing";
    }

  }

}
