package com.rb.nonbiz.functional;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.functional.AllowsMissingValues.Visitor;
import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Function;

import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional;
import static com.rb.nonbiz.collections.RBOptionals.getOrThrow;
import static com.rb.nonbiz.text.Strings.formatMap;
import static com.rb.nonbiz.text.Strings.formatOptional;

/**
 * A function that maps a string to an {@link RBNumeric},
 * and either also allows a 'missing string' case (which maps to a constant RBNumeric),
 * or returns empty if a missing string is encountered.
 *
 * <p> A sample use case for this is ESG string-valued attributes, where an empty
 * string means a special 'missing' value. </p>
 */
public class RBStringToNumericFunctionThatHandlesMissingValues<Y extends RBNumeric<? super Y>>
    implements Function<AllowsMissingValues<String>, Optional<Y>>, HasHumanReadableLabel {

  private final HumanReadableLabel label;
  private final DoubleFunction<Y> instantiator;
  private final RBMap<String, Y> stringToValueMap;
  private final Optional<Y> valueForUnknownString;
  private final Optional<Y> valueForMissingString;

  private RBStringToNumericFunctionThatHandlesMissingValues(
      HumanReadableLabel label,
      DoubleFunction<Y> instantiator,
      RBMap<String, Y> stringToValueMap,
      Optional<Y> valueForUnknownString,
      Optional<Y> valueForMissingString) {
    this.label = label;
    this.instantiator = instantiator;
    this.stringToValueMap = stringToValueMap;
    this.valueForUnknownString = valueForUnknownString;
    this.valueForMissingString = valueForMissingString;
  }

  @Override
  public Optional<Y> apply(AllowsMissingValues<String> x) {
    return x.visit(new Visitor<Optional<Y>, String>() {
      @Override
      public Optional<Y> visitPresentValue(String presentValue) {
        return Optional.of(transformOptional(
            stringToValueMap.getOptional(presentValue),
            v -> instantiator.apply(v.doubleValue()))
            .orElseGet( () -> getOrThrow(valueForUnknownString,
                "%s : intentionally throwing on unknown string %s",
                label, presentValue)));
      }

      @Override
      public Optional<Y> visitMissingValue() {
        return valueForMissingString;
      }
    });
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return label;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  RBMap<String, Y> getStringToValueMap() {
    return stringToValueMap;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  Optional<Y> getValueForUnknownString() {
    return valueForUnknownString;
  }

  @VisibleForTesting // do not use this; it's here to help the test matcher
  Optional<Y> getValueForMissingString() {
    return valueForMissingString;
  }

  @Override
  public String toString() {
<<<<<<< Updated upstream
    return Strings.format("[RBSTNFTHMV %s : %s ; forUnknown = %s ; forMissing= %s RBSTNFTHMV]",
        label, formatMap(stringToValueMap, " ; "),
        formatOptional(valueForUnknownString),
        formatOptional(valueForMissingString));
=======
    return Strings.format("[RBSTNFTHMV %s : %s ; forUnknown= %s ; forMissing= %s RBSTNFTHMV]",
        label, formatMap(stringToValueMap, " ; "), valueForUnknownString, valueForMissingString);
>>>>>>> Stashed changes
  }


  public static class RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y extends RBNumeric<? super Y>>
      implements RBBuilder<RBStringToNumericFunctionThatHandlesMissingValues<Y>> {

    private HumanReadableLabel label;
    private final DoubleFunction<Y> instantiator;
    private RBMap<String, Y> stringToValueMap;
    private Optional<Y> valueForUnknownString;
    private Optional<Y> valueForMissingString;

    private RBStringToNumericFunctionThatHandlesMissingValuesBuilder(DoubleFunction<Y> instantiator) {
      this.instantiator = instantiator;
    }

    // We normally set every field with a setter, but by having the instantiator as an arg in the static constructor,
    // generics get simpler when we instantiate, because we can just use ...builder() instead of ...Builder.<T>...builder().
    public static <Y extends RBNumeric<? super Y>> RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y>
        rbStringToNumericFunctionThatHandlesMissingValuesBuilder(DoubleFunction<Y> instantiator) {
      return new RBStringToNumericFunctionThatHandlesMissingValuesBuilder<>(instantiator);
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> setLabel(HumanReadableLabel label) {
      this.label = checkNotAlreadySet(this.label, label);
      return this;
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> setStringToValueMap(RBMap<String, Y> stringToValueMap) {
      this.stringToValueMap = checkNotAlreadySet(this.stringToValueMap, stringToValueMap);
      return this;
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> useThisForUnknownString(Y valueForUnknownString) {
      this.valueForUnknownString = checkNotAlreadySet(this.valueForUnknownString, Optional.of(valueForUnknownString));
      return this;
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> throwOnUnknownString() {
      this.valueForUnknownString = checkNotAlreadySet(this.valueForUnknownString, Optional.empty());
      return this;
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> useThisForMissingString(Y valueForMissingString) {
      this.valueForMissingString = checkNotAlreadySet(this.valueForMissingString, Optional.of(valueForMissingString));
      return this;
    }

    public RBStringToNumericFunctionThatHandlesMissingValuesBuilder<Y> returnEmptyOnMissingString() {
      this.valueForMissingString = checkNotAlreadySet(this.valueForMissingString, Optional.empty());
      return this;
    }

    @Override
    public void sanityCheckContents() {
      RBPreconditions.checkNotNull(label);
      RBPreconditions.checkNotNull(instantiator); // can't be null but never hurts to check
      RBPreconditions.checkNotNull(stringToValueMap);
      RBPreconditions.checkNotNull(valueForUnknownString);
      RBPreconditions.checkNotNull(valueForMissingString);
    }

    @Override
    public RBStringToNumericFunctionThatHandlesMissingValues<Y> buildWithoutPreconditions() {
      return new RBStringToNumericFunctionThatHandlesMissingValues<>(
          label, instantiator, stringToValueMap, valueForUnknownString, valueForMissingString);
    }
  }

}
