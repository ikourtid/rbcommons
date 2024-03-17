package com.rb.biz.investing.modeling.selection.overrides;

import com.rb.nonbiz.collections.HasLongMap;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.types.HasLongRepresentation;

import java.util.Optional;

import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;

/**
 * Possibly applies an override to a value retrieved from a map.
 *
 * <p> Retrieves a value for a key out of a map, subject to the {@link Overrides} specified
 * (i.e. what to do if a value is missing, what values to override even if they are there, etc.) </p>
 *
 * @see Overrides
 */
public class OverridesApplier {

  public <K, V extends Comparable<? super V>> V getValue(K key, RBMap<K, V> originalMap, Overrides<K, V> overrides) {
    return getValueForSingleItem(key, originalMap.getOptional(key), overrides);
  }

  public <K extends HasLongRepresentation, V extends Comparable<? super V>> V getValue(
      K key, HasLongMap<K, V> originalMap, Overrides<K, V> overrides) {
    return getValueForSingleItem(key, originalMap.getOptional(key), overrides);
  }

  public <K, V extends Comparable<? super V>> V getValueForSingleItem(
      K key, Optional<V> maybeExisting, Overrides<K, V> overrides) {
    Optional<V> maybeOverride = overrides.getOverridesMap().getOptional(key);
    if (!maybeExisting.isPresent()) {
      return maybeOverride.isPresent()
          ? maybeOverride.get() // No value, has override
          : getOrThrow( // No value, no override
          overrides.getWhenNoValueAndNoOverride(),
          key + " : we were told to throw if there is no value and no override");
    }
    V existing = maybeExisting.get();
    if (!maybeOverride.isPresent()) {
      return overrides.getBehaviorWithValueButNoOverride().visit(new BehaviorWithValueButNoOverride.Visitor<V>() {
        @Override
        public V visitUseExistingValueWhenOverrideMissing() {
          return existing;
        }

        @Override
        public V visitUseFixedValueWhenOverrideMissing(V fixedValue) {
          return fixedValue;
        }
      });
    }

    V override = maybeOverride.get();
    return overrides.getBehaviorWithValueAndOverride().visit(new BehaviorWithValueAndOverride.Visitor<V>() {
      @Override
      public V visitAlwaysUseOverrideAndIgnoreExistingValue() {
        return override;
      }

      @Override
      public V visitAlwaysUseExistingValueAndIgnoreOverride() {
        return existing;
      }

      @Override
      public V visitOnlyUseOverrideToFurtherReduceExistingValue() {
        return existing.compareTo(override) < 0 ? existing : override;
      }
    });
  }

}
