package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.collections.DoubleMap.doubleMap;
import static com.rb.nonbiz.collections.DoubleMap.emptyDoubleMap;
import static com.rb.nonbiz.collections.DoubleMapTest.doubleMapMatcher;
import static com.rb.nonbiz.collections.DoubleMaps.doubleMapOfRBNumeric;
import static com.rb.nonbiz.collections.DoubleMaps.linearlyCombineDoubleMaps;
import static com.rb.nonbiz.collections.DoubleMaps.sumDoubleMaps;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.types.PositiveMultiplier.positiveMultiplier;
import static org.hamcrest.MatcherAssert.assertThat;

public class DoubleMapsTest {

  @Test
  public void testDoubleMapOfRBNumeric() {
    // convert a map of String -> PreciseValue
    assertThat(
        doubleMapOfRBNumeric(rbMapOf(
            "a", money(1.1),
            "b", money(2.2),
            "c", money(3.3))),
        doubleMapMatcher(doubleMap(rbMapOf(
            "a", 1.1,
            "b", 2.2,
            "c", 3.3))));

    // convert a map of String -> ImpreciseValue
    assertThat(
    doubleMapOfRBNumeric(
        rbMapOf(
            "a", positiveMultiplier(1.1),
            "b", positiveMultiplier(2.2),
            "c", positiveMultiplier(3.3))),
        doubleMapMatcher(doubleMap(rbMapOf(
            "a", 1.1,
            "b", 2.2,
            "c", 3.3))));
  }

  @Test
  public void testSum() {
    assertThat(
        "empty + empty = empty",
        sumDoubleMaps(
            emptyDoubleMap(),
            emptyDoubleMap()),
        doubleMapMatcher(emptyDoubleMap()));
    assertThat(
        "non-empty + empty = non-empty",
        sumDoubleMaps(
            doubleMap(rbMapOf(
                "a", 1.11,
                "b", 2.22)),
            emptyDoubleMap()),
        doubleMapMatcher(doubleMap(rbMapOf(
            "a", 1.11,
            "b", 2.22))));
    assertThat(
        "empty + non-empty = empty",
        sumDoubleMaps(
            emptyDoubleMap(),
            doubleMap(rbMapOf(
                "a", 1.11,
                "b", 2.22))),
        doubleMapMatcher(doubleMap(rbMapOf(
            "a", 1.11,
            "b", 2.22))));
    assertThat(
        "non-shared items get copied; shared items get added",
        sumDoubleMaps(
            doubleMap(rbMapOf(
                "a", 100.0,
                "c", 200.0)),
            doubleMap(rbMapOf(
                "a", 1.11,
                "b", 2.22))),
        doubleMapMatcher(doubleMap(rbMapOf(
            "a", doubleExplained(101.11, 100 + 1.11),
            "b", 2.22,
            "c", 200.0))));
  }

  @Test
  public void testLinearCombination() {
    DoubleMap<String> teens = doubleMap(rbMapOf(
        "a1", 10.0,
        "a2", 20.0));
    DoubleMap<String> hundreds = doubleMap(rbMapOf(
        "a1", 100.0,
        "a3", 300.0));
    DoubleMap<String> empty = emptyDoubleMap();
    assertCombinationEquals(empty, empty, 1.0, empty, 2.0);
    assertCombinationEquals(empty, empty, 0.0, empty, 2.0);
    assertCombinationEquals(empty, empty, 0.0, empty, 0.0);

    assertCombinationEquals(teens, teens, 1.0, empty, 0.0);
    assertCombinationEquals(teens, teens, 1.0, empty, 1.0);
    assertCombinationEquals(teens, teens, 1.0, empty, 5.0);

    assertCombinationEquals(
        doubleMap(rbMapOf(
            "a1", doubleExplained(870, 7 * 10.0 + 8 * 100.0),
            "a2", doubleExplained(140, 7 * 20.0),
            "a3", doubleExplained(2400, 8 * 300.0))),
        teens, 7,
        hundreds, 8);
    assertCombinationEquals(teens, teens, 1.0, hundreds, 0.0);
    assertCombinationEquals(teens, hundreds, 0.0, teens, 1.0);
  }

  private void assertCombinationEquals(DoubleMap<String> linearCombination,
                                       DoubleMap<String> map1, double coeff1,
                                       DoubleMap<String> map2, double coeff2) {
    assertThat(
        linearlyCombineDoubleMaps(
            ImmutableList.of(map1, map2),
            ImmutableList.of(coeff1, coeff2)),
        doubleMapMatcher(linearCombination));
  }

}
