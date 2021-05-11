package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.nonbiz.collections.RBSymmetricUnequalPairMap.RBSymmetricUnequalPairMapDynamicBuilder;
import com.rb.nonbiz.collections.RBSymmetricUnequalPairMap.RBSymmetricUnequalPairMapStaticBuilder;
import com.rb.nonbiz.functional.TriConsumer;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Collections;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.RBSymmetricUnequalPairMap.RBSymmetricUnequalPairMapDynamicBuilder.rbSymmetricUnequalPairMapDynamicBuilder;
import static com.rb.nonbiz.collections.RBSymmetricUnequalPairMap.RBSymmetricUnequalPairMapStaticBuilder.rbSymmetricUnequalPairMapStaticBuilder;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.arrayMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

// This class is not generic, but the publicly exposed static matcher is.
public class RBSymmetricUnequalPairMapTest extends RBTestMatcher<RBSymmetricUnequalPairMap<String, Double>> {

  @Test
  public void testGetOptionalIndex() {
    for (RBSymmetricUnequalPairMap<String, Double> pairMap : rbSetOf(
        RBSymmetricUnequalPairMapDynamicBuilder.<String, Double>rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
            .putAssumingAbsent("a", "b", 1.1)
            .putAssumingAbsent("a", "c", 1.2)
            .putAssumingAbsent("a", "d", 1.3)
            .putAssumingAbsent("b", "c", 1.4)
            .putAssumingAbsent("b", "d", 1.5)
            .putAssumingAbsent("c", "d", 1.6)
            .build(),
        rbSymmetricUnequalPairMapStaticBuilder(
            ImmutableList.of("a", "b", "c", "d"), Double[]::new)
            .putAssumingAbsent("a", "b", 1.1)
            .putAssumingAbsent("a", "c", 1.2)
            .putAssumingAbsent("a", "d", 1.3)
            .putAssumingAbsent("b", "c", 1.4)
            .putAssumingAbsent("b", "d", 1.5)
            .putAssumingAbsent("c", "d", 1.6)
            .build())) {

      TriConsumer<Integer, String, String> asserter = (expectedIndex, key1, key2) -> {
        assertEquals(expectedIndex.intValue(), pairMap.getOptionalFlatIndex(key1, key2).getAsInt());
        assertEquals(expectedIndex.intValue(), pairMap.getOptionalFlatIndex(key2, key1).getAsInt());
      };
      //    A  B  C  D
      // A
      // B  x
      // C  x  x
      // D  x  x  x
      asserter.accept(0, "b", "a");
      asserter.accept(1, "c", "a");
      asserter.accept(2, "c", "b");
      asserter.accept(3, "d", "a");
      asserter.accept(4, "d", "b");
      asserter.accept(5, "d", "c");

      assertFalse(pairMap.getOptionalFlatIndex("a", "X").isPresent());
      assertFalse(pairMap.getOptionalFlatIndex("X", "a").isPresent());
      assertFalse(pairMap.getOptionalFlatIndex("X", "Y").isPresent());
    }
  }

  @Test
  public void testGetters() {
    for (RBSymmetricUnequalPairMap<String, Double> pairMap : rbSetOf(
        RBSymmetricUnequalPairMapDynamicBuilder.<String, Double>rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
            .putAssumingAbsent("a", "b", 1.1)
            .putAssumingAbsent("a", "c", 1.2)
            .putAssumingAbsent("a", "d", 1.3)
            .putAssumingAbsent("b", "c", 1.4)
            .putAssumingAbsent("b", "d", 1.5)
            .putAssumingAbsent("c", "d", 1.6)
            .build(),
        rbSymmetricUnequalPairMapStaticBuilder(
            ImmutableList.of("a", "b", "c", "d"), Double[]::new)
            .putAssumingAbsent("a", "b", 1.1)
            .putAssumingAbsent("a", "c", 1.2)
            .putAssumingAbsent("a", "d", 1.3)
            .putAssumingAbsent("b", "c", 1.4)
            .putAssumingAbsent("b", "d", 1.5)
            .putAssumingAbsent("c", "d", 1.6)
            .build())) {
      TriConsumer<Double, String, String> asserter = (value, key1, key2) -> {
        assertEquals(value, pairMap.getOrThrow(key1, key2), 1e-8);
        assertEquals(value, pairMap.getOrThrow(key2, key1), 1e-8);
        assertEquals(value, pairMap.getOptional(key1, key2).get(), 1e-8);
        assertEquals(value, pairMap.getOptional(key2, key1).get(), 1e-8);
      };

      asserter.accept(1.1, "a", "b");
      asserter.accept(1.2, "a", "c");
      asserter.accept(1.3, "a", "d");
      asserter.accept(1.4, "b", "c");
      asserter.accept(1.5, "b", "d");
      asserter.accept(1.6, "c", "d");

      assertIllegalArgumentException( () -> pairMap.getOrThrow("a", "X"));
      assertIllegalArgumentException( () -> pairMap.getOrThrow("X", "a"));
      assertIllegalArgumentException( () -> pairMap.getOrThrow("X", "Y"));

      assertOptionalEmpty(pairMap.getOptional("a", "X"));
      assertOptionalEmpty(pairMap.getOptional("X", "a"));
      assertOptionalEmpty(pairMap.getOptional("X", "Y"));
    }
  }

  @Test
  public void includesPairToItself_throws() {
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "a", DUMMY_DOUBLE));
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapStaticBuilder(singletonList("a"), Double[]::new)
        .putAssumingAbsent("a", "a", DUMMY_DOUBLE));
  }

  @Test
  public void dynamicBuilder_notFullyTriangular_throws() {
    IntFunction<RBSymmetricUnequalPairMap<String, Double>> pairMapMaker = itemToSkip -> {
      RBSymmetricUnequalPairMapDynamicBuilder<String, Double> builder = rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new);
      int itemNumber = 0;
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "b", 1.1);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "c", 1.2);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "d", 1.3);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("b", "c", 1.4);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("b", "d", 1.5);
      }
      if (itemToSkip != itemNumber) {
        builder.putAssumingAbsent("c", "d", 1.6);
      }
      return builder.build();
    };
    IntStream.range(0, 6).forEach(i ->
        assertIllegalArgumentException( () -> pairMapMaker.apply(i)));
    // this doesn't skip any assignments, so it builds the whole object
    RBSymmetricUnequalPairMap<String, Double> doesNotThrow = pairMapMaker.apply(6);
  }

  @Test
  public void staticBuilder_notFullyTriangular_throws() {
    IntFunction<RBSymmetricUnequalPairMap<String, Double>> pairMapMaker = itemToSkip -> {
      RBSymmetricUnequalPairMapStaticBuilder<String, Double> builder =
          rbSymmetricUnequalPairMapStaticBuilder(ImmutableList.of("a", "b", "c", "d"), Double[]::new);
      int itemNumber = 0;
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "b", 1.1);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "c", 1.2);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("a", "d", 1.3);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("b", "c", 1.4);
      }
      if (itemToSkip != itemNumber++) {
        builder.putAssumingAbsent("b", "d", 1.5);
      }
      if (itemToSkip != itemNumber) {
        builder.putAssumingAbsent("c", "d", 1.6);
      }
      return builder.build();
    };
    IntStream.range(0, 6).forEach(i ->
        assertThrowsAnyException( () -> pairMapMaker.apply(i)));
    // this doesn't skip any assignments, so it builds the whole object
    RBSymmetricUnequalPairMap<String, Double> doesNotThrow = pairMapMaker.apply(6);
  }

  @Test
  public void staticBuilder_mustHaveAtLeastTwoKeys_throws() {
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapStaticBuilder(Collections.<String>emptyList(), Double[]::new));
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapStaticBuilder(singletonList(DUMMY_STRING), Double[]::new));
  }

  @Test
  public void dynamicBuilder_putAssumingAbsent_itemsExist_throws() {
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE));
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE)
        .putAssumingAbsent("b", "a", DUMMY_DOUBLE));
  }

  @Test
  public void staticBuilder_putAssumingAbsent_itemsExist_throws() {
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapStaticBuilder(ImmutableList.of("a", "b"), Double[]::new)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE));
    assertIllegalArgumentException( () -> rbSymmetricUnequalPairMapStaticBuilder(ImmutableList.of("a", "b"), Double[]::new)
        .putAssumingAbsent("a", "b", DUMMY_DOUBLE)
        .putAssumingAbsent("b", "a", DUMMY_DOUBLE));
  }

  @Test
  public void test_getValues_getValuesForKey() {
    RBSymmetricUnequalPairMap<String, String> pairMap =
        RBSymmetricUnequalPairMapDynamicBuilder.<String, String>rbSymmetricUnequalPairMapDynamicBuilder(String[]::new)
            .putAssumingAbsent("a", "b", "ab")
            .putAssumingAbsent("a", "c", "ac")
            .putAssumingAbsent("a", "d", "ad")
            .putAssumingAbsent("b", "c", "bc")
            .putAssumingAbsent("b", "d", "bd")
            .putAssumingAbsent("c", "d", "cd")
            .build();

    // The ordering is not that important, but note that it's not in the same order as we insert the items.
    // Instead, it is the order the items appear in the raw flat array.
    assertEquals(pairMap.values(), ImmutableList.of("ab", "ac", "bc", "ad", "bd", "cd"));
    // Same here, although the ordering is different for different reasons.
    assertEquals(ImmutableList.of("ab", "ac", "ad"), pairMap.valuesForPairsWithKey("a"));
    assertEquals(ImmutableList.of("ab", "bc", "bd"), pairMap.valuesForPairsWithKey("b"));
    assertEquals(ImmutableList.of("ac", "bc", "cd"), pairMap.valuesForPairsWithKey("c"));
    assertEquals(ImmutableList.of("ad", "bd", "cd"), pairMap.valuesForPairsWithKey("d"));
  }

  @Test
  public void test_getValues_getValuesForKeys() {
    RBSymmetricUnequalPairMap<String, String> pairMap =
        RBSymmetricUnequalPairMapDynamicBuilder.<String, String>rbSymmetricUnequalPairMapDynamicBuilder(String[]::new)
            .putAssumingAbsent("a", "b", "ab")
            .putAssumingAbsent("a", "c", "ac")
            .putAssumingAbsent("a", "d", "ad")
            .putAssumingAbsent("b", "c", "bc")
            .putAssumingAbsent("b", "d", "bd")
            .putAssumingAbsent("c", "d", "cd")
            .build();

    // The ordering is not that important, but note that it's not in the same order as we insert the items.
    // Instead, it is the order the items appear in the raw flat array.
    assertEquals(
        pairMap.valuesForPairsWithKeys(ImmutableList.of("b", "c", "d")),
        ImmutableList.of("bc", "bd", "cd"));
    assertEquals(
        pairMap.valuesForPairsWithKeys(ImmutableList.of("a", "b", "c")),
        ImmutableList.of("ab", "ac", "bc"));
  }

  @Override
  public RBSymmetricUnequalPairMap<String, Double> makeTrivialObject() {
    return RBSymmetricUnequalPairMapDynamicBuilder.<String, Double>rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "b", 1.0)
        .build();
  }

  @Override
  public RBSymmetricUnequalPairMap<String, Double> makeNontrivialObject() {
    return RBSymmetricUnequalPairMapDynamicBuilder.<String, Double>rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "b", 1.1)
        .putAssumingAbsent("a", "c", 1.2)
        .putAssumingAbsent("a", "d", 1.3)
        .putAssumingAbsent("b", "c", 1.4)
        .putAssumingAbsent("b", "d", 1.5)
        .putAssumingAbsent("c", "d", 1.6)
        .build();
  }

  @Override
  public RBSymmetricUnequalPairMap<String, Double> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return RBSymmetricUnequalPairMapDynamicBuilder.<String, Double>rbSymmetricUnequalPairMapDynamicBuilder(Double[]::new)
        .putAssumingAbsent("a", "b", 1.1 + e)
        .putAssumingAbsent("a", "c", 1.2 + e)
        .putAssumingAbsent("a", "d", 1.3 + e)
        .putAssumingAbsent("b", "c", 1.4 + e)
        .putAssumingAbsent("b", "d", 1.5 + e)
        .putAssumingAbsent("c", "d", 1.6 + e)
        .build();
  }

  @Override
  protected boolean willMatch(RBSymmetricUnequalPairMap<String, Double> expected,
                              RBSymmetricUnequalPairMap<String, Double> actual) {
    return rbSymmetricUnequalPairMapMatcher(expected, k -> typeSafeEqualTo(k), v -> doubleAlmostEqualsMatcher(v, 1e-8))
        .matches(actual);
  }

  public static <K, V> TypeSafeMatcher<RBSymmetricUnequalPairMap<K, V>> rbSymmetricUnequalPairMapMatcher(
      RBSymmetricUnequalPairMap<K, V> expected,
      MatcherGenerator<K> keysMatcherGenerator,
      MatcherGenerator<V> valuesMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getArrayIndexMapping(), f -> arrayIndexMappingMatcher(f, keysMatcherGenerator)),
        match(v -> v.getRawFlatArray(),      f -> arrayMatcher(f, valuesMatcherGenerator)));
  }

}
