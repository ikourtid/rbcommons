package com.rb.nonbiz.collections;

import org.junit.Test;

import java.util.HashMap;

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
    RBMap singletonRBMap = singletonRBMap("key1", 111);
    assertEquals("key1", singletonRBMap.keySet().toArray()[0]);
    assertEquals(111,    singletonRBMap.values().toArray()[0]);
  }

  @Test
  public void testMultiArgumentConstructors() {
    RBMap rbMap12 = rbMapOf(
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
