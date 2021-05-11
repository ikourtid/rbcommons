package com.rb.nonbiz.math.stats;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.testutils.RBTest;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class PercentilesCalculatorTest extends RBTest<PercentilesCalculator> {

  private final List<String> FIVE_SORTED_ITEMS = ImmutableList.of("a", "b", "c", "d", "e");

  @Test
  public void noPercentilesPassed_throws() {
    for (List<String> list : rbSetOf(FIVE_SORTED_ITEMS, singletonList("a"), Collections.<String>emptyList())) {
      assertIllegalArgumentException( () -> makeTestObject().getApproximatePercentiles(list, emptyList()));
    }
  }

  @Test
  public void noItemsPassed_throws() {
    for (UnitFraction unitFraction : rbSetOf(UNIT_FRACTION_0, unitFraction(0.5), UNIT_FRACTION_1)) {
      assertIllegalArgumentException( () -> makeTestObject().getApproximatePercentiles(
          emptyList(), singletonList(unitFraction)));
    }
  }

  @Test
  public void hundredthPercentile_throws() {
    assertIllegalArgumentException( () ->
        makeTestObject().getApproximatePercentiles(FIVE_SORTED_ITEMS, singletonList(UNIT_FRACTION_1)));
  }

  @Test
  public void singleItem_allPercentilesSame() {
    assertEquals(
        ImmutableList.of("a", "a", "a"),
        makeTestObject().getApproximatePercentiles(singletonList("a"), ImmutableList.of(UNIT_FRACTION_0, unitFraction(0.01), unitFraction(0.99))));
  }

  @Test
  public void happyPath_generalCase() {
    assertEquals(
        ImmutableList.of("a", "a", "b", "b", "c", "c", "d", "d", "e", "e"),
        makeTestObject().getApproximatePercentiles(
            FIVE_SORTED_ITEMS,
            ImmutableList.of(
                UNIT_FRACTION_0, unitFraction(0.1999),
                unitFraction(0.2), unitFraction(0.2199),
                unitFraction(0.4), unitFraction(0.5999),
                unitFraction(0.6), unitFraction(0.7999),
                unitFraction(0.8), unitFraction(0.9999))));

  }

  @Override
  protected PercentilesCalculator makeTestObject() {
    return new PercentilesCalculator();
  }

}
