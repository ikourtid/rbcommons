package com.rb.nonbiz.collections;

import com.rb.nonbiz.collections.RBMapVisitors.PairOfRBSetAndRBMapVisitor;
import com.rb.nonbiz.collections.RBMapVisitors.TwoRBMapsVisitor;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.collections.RBMapVisitors.visitItemsOfRBSetAndRBMap;
import static com.rb.nonbiz.collections.RBMapVisitors.visitItemsOfTwoRBMaps;
import static com.rb.nonbiz.collections.RBMapVisitors.visitRBMapsExpectingSameKeys;
import static com.rb.nonbiz.collections.RBMapVisitors.visitSharedItemsOfTwoRBMaps;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static org.junit.Assert.assertEquals;

public class RBMapVisitorsTest {

  @Test
  public void testTwoRBMapsVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    visitItemsOfTwoRBMaps(
        rbMapOf(
            10, "a",
            11, "b",
            12, "c",
            13, "d"),
        rbMapOf(
            12, 200L,
            13, 300L,
            14, 400L,
            15, 500L),
        new TwoRBMapsVisitor<Integer, String, Long>() {
          @Override
          public void visitItemInLeftMapOnly(Integer keyInLeftMapOnly, String valueInLeftMapOnly) {
            mutableSet.add(Strings.format("L_%s_%s", keyInLeftMapOnly, valueInLeftMapOnly));
          }

          @Override
          public void visitItemInRightMapOnly(Integer keyInRightMapOnly, Long valueInRightMapOnly) {
            mutableSet.add(Strings.format("R_%s_%s", keyInRightMapOnly, valueInRightMapOnly));
          }

          @Override
          public void visitItemInBothMaps(Integer keyInBothMaps, String valueInLeftMap, Long valueInRightMap) {
            mutableSet.add(Strings.format("B_%s_%s_%s", keyInBothMaps, valueInLeftMap, valueInRightMap));
          }
        });
    assertEquals(
        newRBSet(mutableSet),
        rbSetOf(
            "L_10_a", "L_11_b",
            "B_12_c_200", "B_13_d_300",
            "R_14_400", "R_15_500"));
  }

  @Test
  public void testPairOfRBSetAndRBMapVisitor() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    visitItemsOfRBSetAndRBMap(
        rbSetOf(10, 11, 12, 13),
        rbMapOf(
            12, "a",
            13, "b",
            14, "c",
            15, "d"),
        new PairOfRBSetAndRBMapVisitor<Integer, String>() {
          @Override
          public void visitItemInSetOnly(Integer keyInSetOnly) {
            mutableSet.add(Strings.format("S_%s", keyInSetOnly));
          }

          @Override
          public void visitItemInMapOnly(Integer keyInMapOnly, String value) {
            mutableSet.add(Strings.format("M_%s_%s", keyInMapOnly, value));
          }

          @Override
          public void visitItemInBothSetAndMap(Integer keyInBothSetAndMap, String value) {
            mutableSet.add(Strings.format("B_%s_%s", keyInBothSetAndMap, value));
          }
        });

    assertEquals(
        newRBSet(mutableSet),
        rbSetOf(
            "S_10", "S_11",       // in set only
            "M_14_c", "M_15_d",   // in map only
            "B_12_a", "B_13_b")); // in both set and map
  }

  @Test
  public void testVisitSharedItemsOfTwoRBMaps_overloadWithoutKey() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    visitSharedItemsOfTwoRBMaps(
        rbMapOf(
            1, "A",
            2, "B",
            3, "C"),
        rbMapOf(
            2, 22,
            3, 33,
            4, 44),
        (stringVal, intVal) -> mutableSet.add(Strings.format("%s_%s", stringVal, intVal)));
    assertEquals(
        rbSetOf("B_22", "C_33"),
        newRBSet(mutableSet));
  }

  @Test
  public void testVisitSharedItemsOfTwoRBMaps_overloadWithKey() {
    MutableRBSet<String> mutableSet = newMutableRBSet();
    visitSharedItemsOfTwoRBMaps(
        rbMapOf(
            1, "A",
            2, "B",
            3, "C"),
        rbMapOf(
            2, 22,
            3, 33,
            4, 44),
        (key, stringVal, intVal) -> mutableSet.add(
            Strings.format("%s.%s_%s", key, stringVal, intVal)));
    assertEquals(
        rbSetOf("2.B_22", "3.C_33"),
        newRBSet(mutableSet));
  }

  @Test
  public void testVisitRBMapsExpectingSameKeys() {
    RBMap<Integer, String> stringRBMap = rbMapOf(
        1, "A",
        2, "B",
        3, "C");
    RBMap<Integer, Double> doubleRBMap = rbMapOf(
        1, 1.1,
        2, 2.2,
        3, 3.3);

    MutableRBSet<String> mutableSet = newMutableRBSet();
    TriConsumer<Integer, String, Double> mapUpdater = (key, stringVal, doubleVal) ->
        mutableSet.add(Strings.format("%s:%s_%s", key, stringVal, doubleVal));

    visitRBMapsExpectingSameKeys(
        stringRBMap,
        doubleRBMap,
        mapUpdater);
    assertEquals(
        rbSetOf("1:A_1.1", "2:B_2.2", "3:C_3.3"),
        newRBSet(mutableSet));

    // if the number of keys don't match, an exception will be thrown
    assertIllegalArgumentException( () -> visitRBMapsExpectingSameKeys(
        stringRBMap,
        singletonRBMap(1, 1.1),   // wrong number of entries
        mapUpdater));

    // if the keys don't match, an exception will be thrown
    assertIllegalArgumentException( () -> visitRBMapsExpectingSameKeys(
        stringRBMap,
        rbMapOf(
            2, 2.2,
            3, 3.3),   // right number of entries, but the keys do not match
        mapUpdater));
  }

}
