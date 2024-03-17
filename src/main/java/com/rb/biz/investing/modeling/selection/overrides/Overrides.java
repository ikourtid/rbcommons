package com.rb.biz.investing.modeling.selection.overrides;

import com.google.common.annotations.VisibleForTesting;
import com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.Visitor;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.Pointer;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;

import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.AlwaysUseExistingValueAndIgnoreOverride.alwaysUseExistingValueAndIgnoreOverride;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.AlwaysUseOverrideAndIgnoreExistingValue.alwaysUseOverrideAndIgnoreExistingValue;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseExistingValueWhenOverrideMissing.useExistingValueWhenOverrideMissing;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.text.Strings.formatOptional;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;

/**
 * Specifies a set of override values, as well as instructions on how to apply them.
 *
 * <p> Here is the original use case: When we run individual backtests in a batch of 'comparable backtests',
 * we may want to restrict further which instruments we want to trade. </p>
 *
 * <p> We have a one-off step where we pre-generate such info about how good a stock is to trade in a backtest
 * in general, and store it in protobuf files. Those protobuf files are meant to be somewhat maximal;
 * if we determine that stock XYZ is good enough to hold and/or sell as an individual stock <em>in general</em>
 * (e.g. enough trading volume, etc.) under relatively permissive inclusion rules, then its
 * corresponding info will be in the protobufs. </p>
 *
 * <p> However, in some rudimentary backtests where we only want to be trading the 'standard' asset class ETFs,
 * we may want to override stock XYZ from tradable/sellable to e.g. excluded. This data class contains those overrides. </p>
 *
 * <p> In this particular example that references backtests, note that these overrides are not date-specific;
 * they are meant to apply to all dates in the test. </p>
 *
 * <p> Backtest note: in theory, these could be MORE permissive instead of less permissive
 * vs. what instruments have already been loaded. However, there is no easy way to check for that.
 * Just make sure you don't make a mistake when you set up your backtests: e.g. if you specified in
 * {@code AllComparableBacktestSettings} that you always want to exclude stock XYZ, don't set this
 * class to say 'always make XYZ sellable'.
 *
 * @see BehaviorWithValueButNoOverride
 * @see BehaviorWithValueAndOverride
 * @see OverridesApplier
 */
public class Overrides<K, V extends Comparable<? super V>> {

  private final RBMap<K, V> overridesMap;
  private final BehaviorWithValueAndOverride<V> behaviorWithValueAndOverride;
  private final BehaviorWithValueButNoOverride<V> behaviorWithValueButNoOverride;
  private final Optional<V> whenNoValueAndNoOverride;

  private Overrides(
      RBMap<K, V> overridesMap,
      BehaviorWithValueAndOverride<V> behaviorWithValueAndOverride,
      BehaviorWithValueButNoOverride<V> behaviorWithValueButNoOverride,
      Optional<V> whenNoValueAndNoOverride) {
    this.overridesMap = overridesMap;
    this.behaviorWithValueAndOverride = behaviorWithValueAndOverride;
    this.behaviorWithValueButNoOverride = behaviorWithValueButNoOverride;
    this.whenNoValueAndNoOverride = whenNoValueAndNoOverride;
  }

  public static <K, V extends Comparable<? super V>> Overrides<K, V> noOverrides() {
    return OverridesBuilder.<K, V>overridesBuilder()
        .setOverridesMap(emptyRBMap())
        // This is irrelevant in the case where there are no overrides to begin with
        .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue())
        .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing())
        .throwWhenNoValueAndNoOverride()
        .build();
  }

  public static <K, V extends Comparable<? super V>> Overrides<K, V>
  useGlobalOverrideOnlyIfValueMissing(V defaultValue) {
    return OverridesBuilder.<K, V>overridesBuilder()
        .setOverridesMap(emptyRBMap())
        .setBehaviorWithValueAndOverride(alwaysUseExistingValueAndIgnoreOverride())
        .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing()) // vs. e.g. UseFixedValueWhenOverrideMissing
        .useThisWhenNoValueAndNoOverride(defaultValue)
        .build();
  }

  /**
   * This will return true for an Overrides object that essentially means 'no overrides'.
   */
  public boolean impliesNoOverrides() {
    if (!overridesMap.isEmpty()) {
      return false;
    }
    Pointer<Boolean> onlyUsesExistingValue = uninitializedPointer();
    behaviorWithValueButNoOverride.visit(new Visitor<V>() {
      @Override
      public V visitUseExistingValueWhenOverrideMissing() {
        onlyUsesExistingValue.set(true);
        return null;
      }

      @Override
      public V visitUseFixedValueWhenOverrideMissing(V fixedValue) {
        onlyUsesExistingValue.set(false);
        return null;
      }
    });
    if (!onlyUsesExistingValue.getOrThrow()) {
      return false;
    }
    return !whenNoValueAndNoOverride.isPresent();
  }

  public RBMap<K, V> getOverridesMap() {
    return overridesMap;
  }

  public BehaviorWithValueAndOverride<V> getBehaviorWithValueAndOverride() {
    return behaviorWithValueAndOverride;
  }

  public BehaviorWithValueButNoOverride<V> getBehaviorWithValueButNoOverride() {
    return behaviorWithValueButNoOverride;
  }

  @VisibleForTesting
  // This is for the test matcher.
  Optional<V> getOptionalWhenNoValueAndNoOverride() {
    return whenNoValueAndNoOverride;
  }

  public Optional<V> getWhenNoValueAndNoOverride() {
    return whenNoValueAndNoOverride;
  }

  @Override
  public String toString() {
    return Strings.format("[O %s; valueAndOverride: %s ; valueNoOverride: %s ; noValueNoOverride: %s O]",
        overridesMap, behaviorWithValueAndOverride, behaviorWithValueButNoOverride,
        formatOptional(whenNoValueAndNoOverride));
  }


  public static class OverridesBuilder<K, V extends Comparable<? super V>> implements RBBuilder<Overrides<K, V>> {

    private RBMap<K, V> overridesMap;
    private BehaviorWithValueAndOverride<V> behaviorWithValueAndOverride;
    private BehaviorWithValueButNoOverride<V> behaviorWithValueButNoOverride;
    private Optional<V> whenNoValueAndNoOverride;


    public static <K, V extends Comparable<? super V>> OverridesBuilder<K, V> overridesBuilder() {
      return new OverridesBuilder<>();
    }

    public OverridesBuilder<K, V> setOverridesMap(RBMap<K, V> overridesMap) {
      this.overridesMap = checkNotAlreadySet(this.overridesMap, overridesMap);
      return this;
    }

    public OverridesBuilder<K, V> setBehaviorWithValueAndOverride(
        BehaviorWithValueAndOverride<V> behaviorWithValueAndOverride) {
      this.behaviorWithValueAndOverride = checkNotAlreadySet(
          this.behaviorWithValueAndOverride, behaviorWithValueAndOverride);
      return this;
    }

    public OverridesBuilder<K, V> setBehaviorWithValueButNoOverride(
        BehaviorWithValueButNoOverride<V> behaviorWithValueButNoOverride) {
      this.behaviorWithValueButNoOverride = checkNotAlreadySet(
          this.behaviorWithValueButNoOverride, behaviorWithValueButNoOverride);
      return this;
    }

    public OverridesBuilder<K, V> throwWhenNoValueAndNoOverride() {
      this.whenNoValueAndNoOverride = checkNotAlreadySet(
          this.whenNoValueAndNoOverride, Optional.empty());
      return this;
    }

    public OverridesBuilder<K, V> useThisWhenNoValueAndNoOverride(V whenNoValueAndNoOverride) {
      this.whenNoValueAndNoOverride = checkNotAlreadySet(
          this.whenNoValueAndNoOverride, Optional.of(whenNoValueAndNoOverride));
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(overridesMap);
      RBPreconditions.checkNotNull(behaviorWithValueAndOverride);
      RBPreconditions.checkNotNull(behaviorWithValueButNoOverride);
      RBPreconditions.checkNotNull(whenNoValueAndNoOverride);
    }

    @Override
    public Overrides<K, V> buildWithoutPreconditions() {
      return new Overrides<>(
          overridesMap, behaviorWithValueAndOverride, behaviorWithValueButNoOverride, whenNoValueAndNoOverride);
    }

  }

}
