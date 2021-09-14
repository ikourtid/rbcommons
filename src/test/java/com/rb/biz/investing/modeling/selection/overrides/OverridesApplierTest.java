package com.rb.biz.investing.modeling.selection.overrides;

import com.rb.biz.investing.modeling.selection.overrides.Overrides.OverridesBuilder;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testutils.RBTest;
import org.junit.Test;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.AlwaysUseOverrideAndIgnoreExistingValue.alwaysUseOverrideAndIgnoreExistingValue;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueAndOverride.OnlyUseOverrideToFurtherReduceExistingValue.onlyUseOverrideToFurtherReduceExistingValue;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseExistingValueWhenOverrideMissing.useExistingValueWhenOverrideMissing;
import static com.rb.biz.investing.modeling.selection.overrides.BehaviorWithValueButNoOverride.UseFixedValueWhenOverrideMissing.useFixedValueWhenOverrideMissing;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class OverridesApplierTest extends RBTest<OverridesApplier> {

  @Test
  public void regularRBMap_bothValueAndOverrideExist() {
    Function<BehaviorWithValueAndOverride<Double>, Overrides<String, Double>> overridesMaker =
        behavior -> OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(singletonRBMap("a", 1.1))
            .setBehaviorWithValueAndOverride(behavior)
            .setBehaviorWithValueButNoOverride(useFixedValueWhenOverrideMissing(DUMMY_DOUBLE))
            .useThisWhenNoValueAndNoOverride(DUMMY_DOUBLE)
            .build();

    Overrides<String, Double> overridesReduce  = overridesMaker.apply(onlyUseOverrideToFurtherReduceExistingValue());
    Overrides<String, Double> overridesReplace = overridesMaker.apply(alwaysUseOverrideAndIgnoreExistingValue());

    assertEquals(1.0, makeTestObject().getValue("a", singletonRBMap("a", 1.0), overridesReduce), 1e-8);
    assertEquals(1.1, makeTestObject().getValue("a", singletonRBMap("a", 1.1), overridesReduce), 1e-8);
    assertEquals(1.1, makeTestObject().getValue("a", singletonRBMap("a", 1.2), overridesReduce), 1e-8);

    assertEquals(1.1, makeTestObject().getValue("a", singletonRBMap("a", 1.0), overridesReplace), 1e-8);
    assertEquals(1.1, makeTestObject().getValue("a", singletonRBMap("a", 1.1), overridesReplace), 1e-8);
    assertEquals(1.1, makeTestObject().getValue("a", singletonRBMap("a", 1.2), overridesReplace), 1e-8);
  }

  @Test
  public void instrumentIdMap_bothValueAndOverrideExist() {
    Function<BehaviorWithValueAndOverride<Double>, Overrides<InstrumentId, Double>> overridesMaker =
        behavior -> OverridesBuilder.<InstrumentId, Double>overridesBuilder()
            .setOverridesMap(singletonRBMap(STOCK_A, 1.1))
            .setBehaviorWithValueAndOverride(behavior)
            .setBehaviorWithValueButNoOverride(useFixedValueWhenOverrideMissing(DUMMY_DOUBLE))
            .useThisWhenNoValueAndNoOverride(DUMMY_DOUBLE)
            .build();

    Overrides<InstrumentId, Double> overridesReduce  = overridesMaker.apply(onlyUseOverrideToFurtherReduceExistingValue());
    Overrides<InstrumentId, Double> overridesReplace = overridesMaker.apply(alwaysUseOverrideAndIgnoreExistingValue());

    assertEquals(1.0, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.0), overridesReduce), 1e-8);
    assertEquals(1.1, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.1), overridesReduce), 1e-8);
    assertEquals(1.1, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.2), overridesReduce), 1e-8);

    assertEquals(1.1, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.0), overridesReplace), 1e-8);
    assertEquals(1.1, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.1), overridesReplace), 1e-8);
    assertEquals(1.1, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 1.2), overridesReplace), 1e-8);
  }

  @Test
  public void regularRBMap_valueExists_noOverrideExists() {
    Function<BehaviorWithValueButNoOverride<Double>, Overrides<String, Double>> overridesMaker =
        behavior -> OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue()) // dummy; unused
            .setBehaviorWithValueButNoOverride(behavior)
            .useThisWhenNoValueAndNoOverride(DUMMY_DOUBLE)
            .build();

    Overrides<String, Double> overridesUseExisting = overridesMaker.apply(useExistingValueWhenOverrideMissing());
    Overrides<String, Double> overridesUseDefault  = overridesMaker.apply(useFixedValueWhenOverrideMissing(7.7));

    assertEquals(7.6, makeTestObject().getValue("a", singletonRBMap("a", 7.6), overridesUseExisting), 1e-8);
    assertEquals(7.7, makeTestObject().getValue("a", singletonRBMap("a", 7.7), overridesUseExisting), 1e-8);
    assertEquals(7.8, makeTestObject().getValue("a", singletonRBMap("a", 7.8), overridesUseExisting), 1e-8);

    assertEquals(7.7, makeTestObject().getValue("a", singletonRBMap("a", 7.6), overridesUseDefault), 1e-8);
    assertEquals(7.7, makeTestObject().getValue("a", singletonRBMap("a", 7.7), overridesUseDefault), 1e-8);
    assertEquals(7.7, makeTestObject().getValue("a", singletonRBMap("a", 7.8), overridesUseDefault), 1e-8);
  }

  @Test
  public void instrumentIdMap_valueExists_noOverrideExists() {
    Function<BehaviorWithValueButNoOverride<Double>, Overrides<InstrumentId, Double>> overridesMaker =
        behavior -> OverridesBuilder.<InstrumentId, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue()) // dummy; unused
            .setBehaviorWithValueButNoOverride(behavior)
            .useThisWhenNoValueAndNoOverride(DUMMY_DOUBLE)
            .build();

    Overrides<InstrumentId, Double> overridesUseExisting = overridesMaker.apply(useExistingValueWhenOverrideMissing());
    Overrides<InstrumentId, Double> overridesUseDefault  = overridesMaker.apply(useFixedValueWhenOverrideMissing(7.7));

    assertEquals(7.6, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.6), overridesUseExisting), 1e-8);
    assertEquals(7.7, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.7), overridesUseExisting), 1e-8);
    assertEquals(7.8, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.8), overridesUseExisting), 1e-8);

    assertEquals(7.7, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.6), overridesUseDefault), 1e-8);
    assertEquals(7.7, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.7), overridesUseDefault), 1e-8);
    assertEquals(7.7, makeTestObject().getValue(STOCK_A, singletonRBMap(STOCK_A, 7.8), overridesUseDefault), 1e-8);
  }

  @Test
  public void regularRBMap_noValueOrOverride() {
    Supplier<OverridesBuilder<String, Double>> builderSupplier = () ->
        OverridesBuilder.<String, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue()) // dummy; unused
            .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing());

    Overrides<String, Double> overridesWithDefault     = builderSupplier.get().useThisWhenNoValueAndNoOverride(7.7).build();
    Overrides<String, Double> overridesWithoutDefault  = builderSupplier.get().throwWhenNoValueAndNoOverride().build();

    assertEquals(7.7, makeTestObject().getValue("a", emptyRBMap(), overridesWithDefault), 1e-8);
    assertIllegalArgumentException( () -> makeTestObject().getValue("a", emptyRBMap(), overridesWithoutDefault));
  }

  @Test
  public void instrumentIdMap_noValueOrOverride() {
    Supplier<OverridesBuilder<InstrumentId, Double>> builderSupplier = () ->
        OverridesBuilder.<InstrumentId, Double>overridesBuilder()
            .setOverridesMap(emptyRBMap())
            .setBehaviorWithValueAndOverride(alwaysUseOverrideAndIgnoreExistingValue()) // dummy; unused
            .setBehaviorWithValueButNoOverride(useExistingValueWhenOverrideMissing());

    Overrides<InstrumentId, Double> overridesWithDefault     = builderSupplier.get().useThisWhenNoValueAndNoOverride(7.7).build();
    Overrides<InstrumentId, Double> overridesWithoutDefault  = builderSupplier.get().throwWhenNoValueAndNoOverride().build();

    assertEquals(7.7, makeTestObject().getValue(STOCK_A, emptyRBMap(), overridesWithDefault), 1e-8);
    assertIllegalArgumentException( () -> makeTestObject().getValue(STOCK_A, emptyRBMap(), overridesWithoutDefault));
  }

  @Override
  protected OverridesApplier makeTestObject() {
    return new OverridesApplier();
  }

}
