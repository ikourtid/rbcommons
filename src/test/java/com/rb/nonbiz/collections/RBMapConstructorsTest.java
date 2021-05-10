package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromCollection;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromCollectionOfHasInstrumentId;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromIterator;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStream;
import static com.rb.nonbiz.collections.RBMapConstructors.rbMapFromStreamOfOptionals;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.singletonRBMap;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RBMapConstructorsTest {

  @Test
  public void testRBMapFromIterator() {
    assertEquals(
        rbMapFromIterator(
            ImmutableList.of("1a", "2b").iterator(),
            v -> Integer.parseInt(v.substring(0, 1)),
            v -> v.substring(1, 2)),
        rbMapOf(
            1, "a",
            2, "b"));
    assertEquals(
        rbMapFromIterator(
            Collections.<String>emptyIterator(),
            v -> Integer.parseInt(v.substring(0, 1)),
            v -> v.substring(1, 2)),
        emptyRBMap());
  }

  @Test
  public void testRBMapFromCollection() {
    assertEquals(
        rbMapFromCollection(
            ImmutableList.of("1a", "2b"),
            v -> Integer.parseInt(v.substring(0, 1)),
            v -> v.substring(1, 2)),
        rbMapOf(
            1, "a",
            2, "b"));
    assertEquals(
        rbMapFromCollection(
            Collections.<String>emptyList(),
            v -> Integer.parseInt(v.substring(0, 1)),
            v -> v.substring(1, 2)),
        emptyRBMap());
  }

  @Test
  public void testRbMapFromList() {
    assertThat(
        rbMapFromCollection(
            Collections.<Pair<String, Integer>>emptyList(),
            v -> v.getLeft()),
        rbMapMatcher(
            emptyRBMap(),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromCollection(
            singletonList(pair("a", 1)),
            v -> v.getLeft()),
        rbMapMatcher(
            singletonRBMap(
                "a", pair("a", 1)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromCollection(
            singletonList(pair("a", 1)),
            v -> v.getRight()),
        rbMapMatcher(
            singletonRBMap(
                1, pair("a", 1)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromCollection(
            ImmutableList.of(pair("a", 1), pair("b", 2)),
            v -> v.getLeft()),
        rbMapMatcher(
            rbMapOf(
                "a", pair("a", 1),
                "b", pair("b", 2)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromCollection(
            ImmutableList.of(pair("a", 1), pair("b", 2)),
            v -> v.getRight()),
        rbMapMatcher(
            rbMapOf(
                1, pair("a", 1),
                2, pair("b", 2)),
            f -> pairEqualityMatcher(f)));
    assertIllegalArgumentException( () -> rbMapFromCollection(
        ImmutableList.of(pair("a", 1), pair("a", 2)),
        v -> v.getLeft()));
    assertIllegalArgumentException( () -> rbMapFromCollection(
        ImmutableList.of(pair("a", 1), pair("b", 1)),
        v -> v.getRight()));
  }

  @Test
  public void testRbMapFromStream_keyExtractorOnly() {
    assertThat(
        rbMapFromStream(
            Stream.<Pair<String, Integer>>empty(),
            v -> v.getLeft()),
        rbMapMatcher(
            emptyRBMap(),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromStream(
            Stream.of(pair("a", 1)),
            v -> v.getLeft()),
        rbMapMatcher(
            singletonRBMap(
                "a", pair("a", 1)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromStream(
            Stream.of(pair("a", 1)),
            v -> v.getRight()),
        rbMapMatcher(
            singletonRBMap(
                1, pair("a", 1)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromStream(
            Stream.of(pair("a", 1), pair("b", 2)),
            v -> v.getLeft()),
        rbMapMatcher(
            rbMapOf(
                "a", pair("a", 1),
                "b", pair("b", 2)),
            f -> pairEqualityMatcher(f)));
    assertThat(
        rbMapFromStream(
            Stream.of(pair("a", 1), pair("b", 2)),
            v -> v.getRight()),
        rbMapMatcher(
            rbMapOf(
                1, pair("a", 1),
                2, pair("b", 2)),
            f -> pairEqualityMatcher(f)));
    assertIllegalArgumentException( () -> rbMapFromStream(
        Stream.of(pair("a", 1), pair("a", 2)),
        v -> v.getLeft()));
    assertIllegalArgumentException( () -> rbMapFromStream(
        Stream.of(pair("a", 1), pair("b", 1)),
        v -> v.getRight()));
  }

  @Test
  public void testRbMapFromStream_separateKeyAndValueExtractors() {
    BiConsumer<Stream<Pair<String, Integer>>, RBMap<String, String>> asserter = (stream, expectedResult) -> assertThat(
        rbMapFromStream(
            stream,
            v -> Strings.format("%s%sk", v.getLeft(), v.getRight()),
            v -> Strings.format("%s%sv", v.getLeft(), v.getRight())),
        rbMapMatcher(
            expectedResult,
            f -> typeSafeEqualTo(f)));
    asserter.accept(Stream.empty(), emptyRBMap());
    asserter.accept(
        Stream.of(pair("a", 1)),
        singletonRBMap(
            "a1k", "a1v"));
    asserter.accept(
        Stream.of(pair("a", 1), pair("b", 2)),
        rbMapOf(
            "a1k", "a1v",
            "b2k", "b2v"));
  }

  @Test
  public void testRbMapFromStreamOfOptionals_separateKeyAndValueExtractors() {
    BiConsumer<Stream<InstrumentId>, RBMap<Long, String>> asserter = (stream, expectedResult) ->
        assertThat(
            rbMapFromStreamOfOptionals(
                stream,
                v -> v.asLong(),
                asLong -> asLong % 2 == 0 ? Optional.of("even_" + asLong) : Optional.empty()),
            rbMapMatcher(
                expectedResult,
                f -> typeSafeEqualTo(f)));

    asserter.accept(Stream.empty(), emptyRBMap());
    asserter.accept(
        Stream.of(instrumentId(101)),
        emptyRBMap());
    asserter.accept(
        Stream.of(instrumentId(100), instrumentId(101), instrumentId(102), instrumentId(103)),
        rbMapOf(
            100L, "even_100",
            102L, "even_102"));
  }

  @Test
  public void testRbMapFromCollectionOfHasInstrumentId() {
    assertTrue(rbMapFromCollectionOfHasInstrumentId(emptyList()).isEmpty());
    assertThat(
        rbMapFromCollectionOfHasInstrumentId(singletonList(testHasUniqueId(uniqueId("a"), unitFraction(0.11)))),
        rbMapMatcher(
            singletonRBMap(
                uniqueId("a"), testHasUniqueId(uniqueId("a"), unitFraction(0.11))),
            f -> testHasUniqueIdMatcher(f)));
    assertThat(
        rbMapFromCollectionOfHasInstrumentId(ImmutableList.of(
            testHasUniqueId(uniqueId("a"), unitFraction(0.11)),
            testHasUniqueId(uniqueId("b"), unitFraction(0.22)))),
        rbMapMatcher(
            rbMapOf(
                uniqueId("a"), testHasUniqueId(uniqueId("a"), unitFraction(0.11)),
                uniqueId("b"), testHasUniqueId(uniqueId("b"), unitFraction(0.22))),
            f -> testHasUniqueIdMatcher(f)));
  }

}
