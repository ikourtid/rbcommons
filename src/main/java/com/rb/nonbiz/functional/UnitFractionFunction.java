package com.rb.nonbiz.functional;

import com.google.common.annotations.VisibleForTesting;
import com.rb.nonbiz.text.HasHumanReadableLabel;
import com.rb.nonbiz.text.HumanReadableLabel;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.RBPreconditions;

import java.util.function.Function;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInBps;
import static com.rb.nonbiz.types.UnitFraction.unitFractionInPct;

/**
 * A {@link Function} from {@link UnitFraction} to any arbitrary {@link RBNumeric} type.
 *
 * <p> There are additional semantics that function must produce values for the entire domain of {@link UnitFraction},
 * from {@link UnitFraction#UNIT_FRACTION_0} to {@link UnitFraction#UNIT_FRACTION_1}.
 * There's a precondition that checks for that, although it's a 'best efforts' check. </p>
 */
public class UnitFractionFunction<Y extends RBNumeric<? super Y>>
    implements Function<UnitFraction, Y>, HasHumanReadableLabel {

  private final RBNumericFunction<UnitFraction, Y> rawRbNumericFunction;

  private UnitFractionFunction(RBNumericFunction<UnitFraction, Y> rawRbNumericFunction) {
    this.rawRbNumericFunction = rawRbNumericFunction;
  }

  public static <Y extends RBNumeric<? super Y>> UnitFractionFunction<Y> unitFractionFunction(
      RBNumericFunction<UnitFraction, Y> rawRbNumericFunction) {
    rbSetOf(
        UNIT_FRACTION_0,
        unitFractionInBps(1),
        unitFractionInBps(10),
        unitFractionInPct(1),
        unitFractionInPct(10),
        UNIT_FRACTION_1)
        .forEach(validX ->
            RBPreconditions.checkDoesNotThrowException(
                () -> rawRbNumericFunction.apply(validX),
                "All valid target fractions from 0 to 1 must map to a valid 'horizontal stretching multiplier': %s",
                validX));
    return new UnitFractionFunction<>(rawRbNumericFunction);
  }

  @Override
  public Y apply(UnitFraction x) {
    return rawRbNumericFunction.apply(x);
  }

  // do not use this; it's here to help the test matcher
  @VisibleForTesting
  RBNumericFunction<UnitFraction, Y> getRawRbNumericFunction() {
    return rawRbNumericFunction;
  }

  @Override
  public HumanReadableLabel getHumanReadableLabel() {
    return rawRbNumericFunction.getHumanReadableLabel();
  }

  @Override
  public String toString() {
    return Strings.format("[UFF %s UFF]", rawRbNumericFunction);
  }

}
