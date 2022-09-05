package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSetFromPossibleDuplicates;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSet.singletonRBSet;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RBSetTest {

  @Test
  public void testTransform() {
    assertEquals(
        emptyRBSet(),
        emptyRBSet().transform(v -> v.toString()));
    assertEquals(
        rbSetOf("11", "22"),
        rbSetOf(11, 22).transform(v -> v.toString()));
    // Throws an exception, because the transformed set will have duplicates, since both 7 and 8 map to the same
    // string "xyz".
    assertIllegalArgumentException( () -> rbSetOf(7, 8).transform(i -> "xyz"));
  }

  @Test
  public void testTransform2() {
    assertEquals(
        RBSet.<String>emptyRBSet(),
        RBSet.<String>emptyRBSet().transform(v -> v.toUpperCase()));

    assertEquals(
        rbSetOf("AB", "CD", "EF", "GH", "IJ"),
        rbSetOf("ab", "cd", "EF", "gH", "Ij").transform(v -> v.toUpperCase()));

    // plain 'transform' disallows attempting to put duplicates in the transformed set, so it will throw.
    assertIllegalArgumentException( () -> rbSetOf("abc", "ABC").transform(v -> v.toUpperCase()));
    assertIllegalArgumentException( () -> rbSetOf("abc", "Abc").transform(v -> v.toUpperCase()));
    assertIllegalArgumentException( () -> rbSetOf("AbC", "aBC").transform(v -> v.toUpperCase()));

    // however, transformAllowingDuplicates does not throw
    RBSet<String> doesNotThrow;
    doesNotThrow = rbSetOf("abc", "ABC").transformAllowingDuplicates(v -> v.toUpperCase());
    doesNotThrow = rbSetOf("abc", "Abc").transformAllowingDuplicates(v -> v.toUpperCase());
    doesNotThrow = rbSetOf("AbC", "aBC").transformAllowingDuplicates(v -> v.toUpperCase());
  }

  @Test
  public void testToRBMap() {
    assertEquals(
        emptyRBMap(),
        emptyRBSet().toRBMap(key -> "_" + key));
    assertEquals(
        rbMapOf(
            "a", "_a",
            "b", "_b",
            "c", "_c",
            "d", "_d",
            "e", "_e"),
        rbSetOf("a", "b", "c", "d", "e")
            .toRBMap(key -> "_" + key));
  }

  @Test
  public void testToRBMapWithTransformedKeys_throwsForSameKey() {
    assertIllegalArgumentException( () -> rbSetOf("a", "b")     .toRBMapWithTransformedKeys(key -> "sameKey", v -> v));
    assertIllegalArgumentException( () -> rbSetOf("a", "b")     .toRBMapWithTransformedKeys(key -> "",        v -> v));
    assertIllegalArgumentException( () -> rbSetOf("a", "b", "c").toRBMapWithTransformedKeys(key -> "sameKey", v -> v));
    assertIllegalArgumentException( () -> rbSetOf("a", "b", "c").toRBMapWithTransformedKeys(key -> "",        v -> v));
  }

  @Test
  public void testToRBMapWithFilteredKeys() {
    assertEquals(
        emptyRBMap(),
        emptyRBSet().toRBMapWithFilteredKeys(key -> Optional.of("_" + key)));
    Function<String, Optional<String>> transformer = v -> Character.isUpperCase(v.charAt(0))
        ? Optional.empty()
        : Optional.of(Strings.format("_%s", v));
    assertEquals(
        rbMapOf(
            "a", "_a",
            "c", "_c",
            "e", "_e"),
        rbSetOf("a" , "B", "c", "D", "e")
            .toRBMapWithFilteredKeys(transformer));
    assertEquals(
        emptyRBMap(),
        rbSetOf("A" , "B", "C", "D", "E")
            .toRBMapWithFilteredKeys(transformer));
  }

  @Test
  public void testToRBMapWithTransformedKeys() {
    assertEquals(
        emptyRBMap(),
        emptyRBSet().toRBMapWithTransformedKeys(key -> "K" + key, key -> "_" + key));
    assertEquals(
        rbMapOf(
            "Ka", "_a",
            "Kb", "_b",
            "Kc", "_c",
            "Kd", "_d",
            "Ke", "_e"),
        rbSetOf("a", "b", "c", "d", "e")
            .toRBMapWithTransformedKeys(key -> "K" + key, key -> "_" + key));
  }

  @Test
  public void testOrderedToRBMap() {
    Comparator<String> stringComparator = String::compareTo;
    {
      List<String> items = newArrayList();
      assertEquals(
          emptyRBMap(),
          RBSet.<String>emptyRBSet().orderedToRBMap(
              key -> {
                items.add(key); // intentional side-effect
                return "_" + key;
              },
              stringComparator));
    }
    {
      List<String> items = newArrayList();
      assertEquals(
          rbMapOf(
              "a", "_a",
              "b", "_b",
              "c", "_c",
              "d", "_d",
              "e", "_e"),
          rbSetOf("a", "b", "c", "d", "e")
              .orderedToRBMap(
                  key -> {
                    items.add(key); // intentional side-effect
                    return "_" + key;
                  },
                  stringComparator));
      assertEquals(ImmutableList.of("a", "b", "c", "d", "e"), items);
    }
    {
      List<String> items = newArrayList();
      assertEquals(
          rbMapOf(
              "a", "_a",
              "b", "_b",
              "c", "_c",
              "d", "_d",
              "e", "_e"),
          rbSetOf("a", "b", "c", "d", "e")
              .orderedToRBMap(
                  key -> {
                    items.add(key); // intentional side-effect
                    return "_" + key;
                  },
                  stringComparator.reversed()));
      assertEquals(ImmutableList.of("e", "d", "c", "b", "a"), items);
    }
  }

  @Test
  public void testToSortedList() {
    assertEquals(emptyRBSet().toSortedList(), emptyList());
    assertEquals(singletonRBSet("a").toSortedList(), singletonList("a"));
    assertEquals(rbSetOf("c", "b", "a").toSortedList(), ImmutableList.of("a", "b", "c"));
  }

  @Test
  public void testHandlingOfDuplicatesInIput() {
    assertEquals(rbSetOf("a", "b"), newRBSet("a", "b"));
    List<String> ab = ImmutableList.of("a", "b", "a");
    assertIllegalArgumentException( () -> newRBSet("a", "b", "a"));
    assertIllegalArgumentException( () -> newRBSet(ab));
    assertIllegalArgumentException( () -> newRBSet(ab.iterator()));
    assertIllegalArgumentException( () -> newRBSet(ab.stream()));

    assertEquals(rbSetOf("a", "b"), newRBSetFromPossibleDuplicates("a", "b", "a"));
    assertEquals(rbSetOf("a", "b"), newRBSetFromPossibleDuplicates(ab));
    assertEquals(rbSetOf("a", "b"), newRBSetFromPossibleDuplicates(ab.iterator()));
    assertEquals(rbSetOf("a", "b"), newRBSetFromPossibleDuplicates(ab.stream()));
  }

  @Test
  public void testToRBMapWithTransformedKey() {
    RBSet<String> abc = rbSetOf("a", "b", "c");

    assertEquals(
        rbMapOf(
            "a1", "A",
            "b1", "B",
            "c1", "C"),
        abc.toRBMapWithTransformedKeys(key -> key.concat("1"), value -> value.toUpperCase()));

    // duplicate keys not allowed
    assertIllegalArgumentException( () -> abc.toRBMapWithTransformedKeys(key -> "allKeysTheSame", value -> value));
  }

  @Test
  public void test_transformAllowingDuplicates_usingSuppiedEqualityOperators() {
    class NoComparison {
      private final String stringContents;

      private NoComparison(String stringContents) {
        this.stringContents = stringContents;
      }
    };
    NoComparison obj1 = new NoComparison("abc");
    NoComparison obj2 = new NoComparison("abc");
    assertNotEquals(
        "These objects are not equal per the default #equals and #hashCode that we inherit from Object",
        obj1, obj2);

    assertEquals(
        "Because the objects are not equal, an RBSet will not eliminate one of the two",
        2, rbSetOf(obj1, obj2).size());

    assertEquals(
        "As above - because the objects are not equal, the transformed RBSet will not eliminate one of the two",
        2, rbSetOf(obj1, obj2).transform(v -> v).size());

    assertEquals(
        "As above - because the objects are not equal, the transformAllowingDuplicates RBSet will not eliminate one of the two",
        2, rbSetOf(obj1, obj2).transformAllowingDuplicates(v -> v).size());

    assertEquals(
        1,
        rbSetOf(obj1, obj2)
            .transformAllowingDuplicates(
                v -> v,
                (v1, v2) -> v1.stringContents.equals(v2.stringContents),
                v -> v.stringContents.hashCode())
            .size());
  }

}
