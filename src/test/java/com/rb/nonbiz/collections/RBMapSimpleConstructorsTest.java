package com.rb.nonbiz.collections;

import org.junit.Test;

import java.util.HashMap;
import java.util.function.Function;

import static com.rb.nonbiz.collections.MutableRBMap.newMutableRBMap;
import static com.rb.nonbiz.collections.RBMapMergers.mergeRBMapsDisallowingOverlap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.newRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static org.junit.Assert.assertEquals;

public class RBMapSimpleConstructorsTest {

  @Test
  public void testEmptyConstructors() {
    assertEquals(0, emptyRBMap().size());

    assertEquals(0, new HashMap<String, Integer>().size());

    MutableRBMap<String, Integer> emptyMutableRBMap = newMutableRBMap();
    assertEquals(0, newRBMap(emptyMutableRBMap).size());
  }

  @Test
  public void testSingletonConstructor() {
    RBMap<String, Integer> singletonRBMap = singletonRBMap("key1", 111);
    assertEquals("key1", singletonRBMap.keySet().toArray()[0]);
    assertEquals(111,    singletonRBMap.values().toArray()[0]);
  }

  @Test
  public void testMultiArgumentConstructors() {
    RBMap<String, Integer> rbMap12 = rbMapOf(
        "k1", 1,
        "k2", 2,
        "k3", 3,
        "k4", 4,
        "k5", 5,
        "k6", 6,
        "k7", 7,
        "k8", 8,
        "k9", 9 ,
        "k10", 10,
        "k11", 11,
        "k12", 12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            singletonRBMap("k1", 1),
            rbMapOf("k2", 2, "k3", 3, "k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12)),
        rbMap12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2),
            rbMapOf("k3", 3, "k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10, "k11", 11,
                "k12", 12)),
        rbMap12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3),
            rbMapOf("k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10, "k11", 11, "k12", 12)),
        rbMap12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4),
            rbMapOf("k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10, "k11", 11, "k12", 12)),
        rbMap12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4, "k5", 5),
            rbMapOf("k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10, "k11", 11, "k12", 12)),
        rbMap12);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4, "k5", 5, "k6", 6),
            rbMapOf("k7", 7, "k8", 8, "k9", 9, "k10", 10, "k11", 11, "k12", 12)),
        rbMap12);
  }

  // More tests after constructors with more arguments were added.
  @Test
  public void testMultiArgumentConstructors_part2() {
    RBMap<String, Integer> rbMap20 = rbMapOf(
        "k1", 1,
        "k2", 2,
        "k3", 3,
        "k4", 4,
        "k5", 5,
        "k6", 6,
        "k7", 7,
        "k8", 8,
        "k9", 9,
        "k10", 10,
        "k11", 11,
        "k12", 12,
        "k13", 13,
        "k14", 14,
        "k15", 15,
        "k16", 16,
        "k17", 17,
        "k18", 18,
        "k19", 19,
        "k20", 20);

    assertEquals(
        mergeRBMapsDisallowingOverlap(
            singletonRBMap("k1", 1),
            rbMapOf("k2", 2, "k3", 3, "k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2),
            rbMapOf("k3", 3, "k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3),
            rbMapOf("k4", 4, "k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4),
            rbMapOf("k5", 5, "k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4, "k5", 5),
            rbMapOf("k6", 6, "k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4, "k5", 5, "k6", 6),
            rbMapOf("k7", 7, "k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
    assertEquals(
        mergeRBMapsDisallowingOverlap(
            rbMapOf("k1", 1, "k2", 2, "k3", 3, "k4", 4, "k5", 5, "k6", 6, "k7", 7),
            rbMapOf("k8", 8, "k9", 9, "k10", 10,
                "k11", 11, "k12", 12, "k13", 13, "k14", 14, "k15", 15, "k16", 16, "k17", 17, "k18", 18,
                "k19", 19, "k20", 20)),
        rbMap20);
  }

  @Test
  public void testDuplicateKeys_throws() {
    Function<String, RBMap<String, Integer>> maker = key1 ->
        rbMapOf(
            key1, 1,
            "B", 2,
            "C", 3);

    RBMap<String, Integer> doesNotThrow;
    doesNotThrow = maker.apply("A");
    doesNotThrow = maker.apply("X");
    doesNotThrow = maker.apply("Y");

    // the following inserts duplicate keys
    assertIllegalArgumentException( () -> maker.apply("B"));
    assertIllegalArgumentException( () -> maker.apply("C"));
  }

  @Test
  public void rejectsDuplicateKeys() {
    assertIllegalArgumentException( () -> rbMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> rbMapOf(
        "a", DUMMY_STRING,
        "a", DUMMY_STRING));
  }

}
