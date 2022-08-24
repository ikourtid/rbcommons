package com.rb.nonbiz.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.LinkedHashMaps.concatenateMapsIntoLinkedHashMap;
import static com.rb.nonbiz.collections.LinkedHashMaps.linkedHashMapOf;
import static com.rb.nonbiz.collections.LinkedHashMaps.toSortedLinkedHashMap;
import static com.rb.nonbiz.collections.LinkedHashMaps.toSortedLinkedHashMapWithTransformedKeys;
import static com.rb.nonbiz.collections.LinkedHashMaps.toSortedLinkedHashMapWithTransformedKeysAndValues;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.linkedHashMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_POSITIVE_INTEGER;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LinkedHashMapsTest {

  @Test
  public void toSortedLinkedHashMap_convertsRbMapsInOrder() {
    assertEquals(
        // The way we construct the map below, it is extremely unlikely that we will end up with the
        // ordering 0123456789 just by chance.
        "0123456789",
        Joiner.on("").join(
            toSortedLinkedHashMap(
                rbMapOf(
                    "0", DUMMY_POSITIVE_INTEGER,
                    "2", DUMMY_POSITIVE_INTEGER,
                    "4", DUMMY_POSITIVE_INTEGER,
                    "6", DUMMY_POSITIVE_INTEGER,
                    "8", DUMMY_POSITIVE_INTEGER,
                    "1", DUMMY_POSITIVE_INTEGER,
                    "3", DUMMY_POSITIVE_INTEGER,
                    "5", DUMMY_POSITIVE_INTEGER,
                    "7", DUMMY_POSITIVE_INTEGER,
                    "9", DUMMY_POSITIVE_INTEGER),
                String::compareTo)
                .keySet()));
  }

  @Test
  public void toSortedLinkedHashMapWithTransformedKeys_convertsRbMapsInOrder() {
    assertEquals(
        // The way we construct the map below, it is extremely unlikely that we will end up with
        // this ordering just by chance.
        "00 11 22 33 44 55 66 77 88 99 ",
        Joiner.on("").join(
            toSortedLinkedHashMapWithTransformedKeys(
                rbMapOf(
                    "0", DUMMY_POSITIVE_INTEGER,
                    "2", DUMMY_POSITIVE_INTEGER,
                    "4", DUMMY_POSITIVE_INTEGER,
                    "6", DUMMY_POSITIVE_INTEGER,
                    "8", DUMMY_POSITIVE_INTEGER,
                    "1", DUMMY_POSITIVE_INTEGER,
                    "3", DUMMY_POSITIVE_INTEGER,
                    "5", DUMMY_POSITIVE_INTEGER,
                    "7", DUMMY_POSITIVE_INTEGER,
                    "9", DUMMY_POSITIVE_INTEGER),
                String::compareTo,
                v -> v + v + " ") // repeat the string and add a space
                .keySet()));
  }

  @Test
  public void toSortedLinkedHashMapWithTransformedKeysAndValues_convertsRbMapsInOrder() {
    LinkedHashMap<String, Integer> result = toSortedLinkedHashMapWithTransformedKeysAndValues(
        rbMapOf(
            "0", 100,
            "2", 102,
            "4", 104,
            "6", 106,
            "8", 108,
            "1", 101,
            "3", 103,
            "5", 105,
            "7", 107,
            "9", 109),
        String::compareTo,
        key -> key + key + " ", // repeat the string and add a space
        value -> value + 200);
    assertEquals(
        // The way we construct the map below, it is extremely unlikely that we will end up with
        // this ordering just by chance.
        "00 11 22 33 44 55 66 77 88 99 ",
        Joiner.on("").join(result.keySet()));
    assertEquals(
        // In this assertion, the space is added by the Joiner; the key transformer is irrelevant here.
        "300 301 302 303 304 305 306 307 308 309",
        Joiner.on(" ").join(result.values()));
  }

  @Test
  public void concatenateMapsIntoLinkedHashMap_keepsInOrderInCaseOfHashMaps() {
    assertThat(
        concatenateMapsIntoLinkedHashMap(
            linkedHashMapOf(
                "a", 1,
                "c", 3,
                "e", 5,
                "g", 7),
            linkedHashMapOf(
                "b", 2,
                "d", 4,
                "f", 6,
                "h", 8)),
        linkedHashMapMatcher(
            // This is as intended; the point is that concatenateMapsIntoLinkedHashMap will not try to
            // order the entries of the resulting map.
            linkedHashMapOf(
                "a", 1,
                "c", 3,
                "e", 5,
                "g", 7,
                "b", 2,
                "d", 4,
                "f", 6,
                "h", 8),
            f -> typeSafeEqualTo(f)));
  }

  @Test
  public void concatenateMapsIntoLinkedHashMap_keepsPartialOrderInCaseOfGeneral() {
    LinkedHashMap<String, Integer> result = concatenateMapsIntoLinkedHashMap(
        ImmutableMap.of(
            "a", 1,
            "c", 3,
            "e", 5,
            "g", 7),
        ImmutableMap.of(
            "b", 2,
            "d", 4,
            "f", 6,
            "h", 8));

    // In the case of maps that do not guarantee their order, all we can assert is that the first 4 items in the
    // resulting map are those of the map that's the first argument, and the last 4 are those of the 2nd.
    List<String> orderedKeysAsList = newArrayList(result.keySet());
    assertEquals(
        rbSetOf("a", "c", "e", "g"),
        // Comparing as set, so that ordering doesn't matter
        newRBSet(orderedKeysAsList.subList(0, 4)));
    assertEquals(
        rbSetOf("b", "d", "f", "h"),
        // Comparing as set, so that ordering doesn't matter
        newRBSet(orderedKeysAsList.subList(4, 8)));
  }

  @Test
  public void simpleConstructors_throwsOnDuplicateKeys() {
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "c", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "c", DUMMY_STRING,
        "d", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "c", DUMMY_STRING,
        "d", DUMMY_STRING,
        "e", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "c", DUMMY_STRING,
        "d", DUMMY_STRING,
        "e", DUMMY_STRING,
        "f", DUMMY_STRING,
        "a", DUMMY_STRING));
    assertIllegalArgumentException( () -> linkedHashMapOf(
        "a", DUMMY_STRING,
        "b", DUMMY_STRING,
        "c", DUMMY_STRING,
        "d", DUMMY_STRING,
        "e", DUMMY_STRING,
        "f", DUMMY_STRING,
        "g", DUMMY_STRING,
        "a", DUMMY_STRING));
  }

}
