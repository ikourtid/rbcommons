package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.Money;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.functional.QuadriFunction;
import com.rb.nonbiz.functional.TriFunction;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.text.Strings;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A1;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A2;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A3;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapMergers.mergeIidMapsAllowingOverlapOnSimilarItemsOnly;
import static com.rb.nonbiz.collections.IidMapMergers.mergeIidMapsByTransformedValue;
import static com.rb.nonbiz.collections.IidMapMergers.mergeIidMapsInOrderAllowingOverwriting;
import static com.rb.nonbiz.collections.IidMapMergers.mergeThreeIidMapsByTransformedEntry;
import static com.rb.nonbiz.collections.IidMapMergers.mergeThreeIidMapsByTransformedValue;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.singletonIidMap;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.iidMapPreciseValueMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

// note: the methods in IidMapMergers have their tests in IidMaps for now; we should move them here.
public class IidMapMergersTest {

  @Test
  public void test_mergeIidMapsAllowingOverlapOnSimilarItemsOnly() {
    BiConsumer<Iterator<IidMap<Money>>, IidMap<Money>> asserter = (iidMapIterator, expectedResult) ->
        assertThat(
            mergeIidMapsAllowingOverlapOnSimilarItemsOnly(
                iidMapIterator,
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8)),
            iidMapPreciseValueMatcher(expectedResult, DEFAULT_EPSILON_1e_8));

    asserter.accept(emptyIterator(), emptyIidMap());
    asserter.accept(singletonIterator(emptyIidMap()), emptyIidMap());
    asserter.accept(ImmutableList.<IidMap<Money>>of(emptyIidMap(), emptyIidMap()).iterator(), emptyIidMap());

    // Does not complain if there is overlap but the values are similar
    rbSetOf(-1e-9, 0.0, 1e-9).forEach(epsilon -> {
      asserter.accept(
          ImmutableList.of(
                  iidMapOf(
                      STOCK_A1, money(100),
                      STOCK_A2, money(200)),
                  iidMapOf(
                      STOCK_A2, money(200 + epsilon),
                      STOCK_A3, money(300)))
              .iterator(),
          iidMapOf(
              STOCK_A1, money(100),
              STOCK_A2, money(200),
              STOCK_A3, money(300)));
      asserter.accept(
          ImmutableList.of(
                  iidMapOf(
                      STOCK_A1, money(100),
                      // This value gets processed first, so it ends up in the return value - NOT 200 below
                      STOCK_A2, money(200 + epsilon)),
                  iidMapOf(
                      STOCK_A2, money(200),
                      STOCK_A3, money(300)))
              .iterator(),
          iidMapOf(
              STOCK_A1, money(100),
              STOCK_A2, money(200 + epsilon),
              STOCK_A3, money(300)));
    });

    rbSetOf(-999.0, -1.0, -1e-7, 1e-7, 1.0, 999.0).forEach(largeEpsilon ->
        assertIllegalArgumentException( () ->
            mergeIidMapsAllowingOverlapOnSimilarItemsOnly(
                ImmutableList.of(
                        iidMapOf(
                            STOCK_A1, money(100),
                            STOCK_A2, money(200)),
                        iidMapOf(
                            STOCK_A2, money(200 + largeEpsilon),
                            STOCK_A3, money(300)))
                    .iterator(),
                (v1, v2) -> v1.almostEquals(v2, DEFAULT_EPSILON_1e_8))));
  }

  @Test
  public void testMergeIidMapsByTransformedValue() {
    BiConsumer<List<IidMap<String>>, IidMap<String>> asserter = (listOfIidMaps, expectedMergedIidMap) ->
        assertThat(
            mergeIidMapsByTransformedValue(
                (iid, listV1) -> Strings.format("%s=%s", iid.asLong(), StringUtils.join(listV1, ":")),
                listOfIidMaps),
            iidMapEqualityMatcher(expectedMergedIidMap));

    // use the following (instead of the usual STOCK_A, etc) in order to have simple numerical values
    InstrumentId STOCK_1 = instrumentId(1);
    InstrumentId STOCK_2 = instrumentId(2);
    InstrumentId STOCK_3 = instrumentId(3);

    // merging an empty list of maps gives an empty map
    asserter.accept(
        emptyList(),
        emptyIidMap());

    // merging empty maps gives an empty map
    asserter.accept(
        ImmutableList.of(
            emptyIidMap(),
            emptyIidMap(),
            emptyIidMap()),
        emptyIidMap());

    // "merging" a single map
    asserter.accept(
        singletonList(
            iidMapOf(
                STOCK_1, "A",
                STOCK_2, "B")),
        iidMapOf(
            STOCK_1, "1=A",
            STOCK_2, "2=B"));

    // merging multiple overlapping maps
    asserter.accept(
        ImmutableList.of(
            iidMapOf(
                STOCK_1, "A",
                STOCK_2, "B"),
            iidMapOf(
                STOCK_2, "C",
                STOCK_3, "D"),
            emptyIidMap()),     // contributes nothing to the merged result
        iidMapOf(
            STOCK_1, "1=A",
            STOCK_2, "2=B:C",
            STOCK_3, "3=D"));
  }

  @Test
  public void testMergeThreeIidMapsByTransformedEntry() {
    // use the following (instead of the usual STOCK_A, etc) in order to have simple numerical values
    InstrumentId STOCK_1 = instrumentId(1);
    InstrumentId STOCK_2 = instrumentId(2);
    InstrumentId STOCK_3 = instrumentId(3);

    QuadriFunction<InstrumentId, Optional<Integer>, Optional<Boolean>, Optional<TestEnumXYZ>, String> merger =
        (instrumentId, maybeInt, maybeBoolean, maybeTestEnumXYZ) -> Strings.format("%s_%s_%s_%s",
            instrumentId.asLong(),
            maybeInt.map(v -> v.toString()).orElse("*"),
            maybeBoolean.map(v -> v.toString()).orElse("*"),
            maybeTestEnumXYZ.map(v -> v.toString()).orElse("*"));
    assertThat(
        mergeThreeIidMapsByTransformedEntry(
            merger,
            iidMapOf(
                STOCK_1, 11,
                STOCK_2, 22,
                STOCK_3, 33),
            iidMapOf(
                STOCK_1, true,
                STOCK_2, false),
            singletonIidMap(
                STOCK_1, TestEnumXYZ.X)),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_1, "1_11_true_X",
                STOCK_2, "2_22_false_*",
                STOCK_3, "3_33_*_*")));
  }

  @Test
  public void testMergeThreeIidMapsByTransformedValue() {
    // use the following (instead of the usual STOCK_A, etc) in order to have simple numerical values
    InstrumentId STOCK_1 = instrumentId(1);
    InstrumentId STOCK_2 = instrumentId(2);
    InstrumentId STOCK_3 = instrumentId(3);

    TriFunction<Optional<Integer>, Optional<Boolean>, Optional<TestEnumXYZ>, String> merger =
        (maybeInt, maybeBoolean, maybeTestEnumXYZ) -> Strings.format("%s_%s_%s",
            maybeInt.map(v -> v.toString()).orElse("*"),
            maybeBoolean.map(v -> v.toString()).orElse("*"),
            maybeTestEnumXYZ.map(v -> v.toString()).orElse("*"));
    assertThat(
        mergeThreeIidMapsByTransformedValue(
            merger,
            iidMapOf(
                STOCK_1, 11,
                STOCK_2, 22,
                STOCK_3, 33),
            iidMapOf(
                STOCK_1, true,
                STOCK_2, false),
            singletonIidMap(
                STOCK_1, TestEnumXYZ.X)),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_1, "11_true_X",
                STOCK_2, "22_false_*",
                STOCK_3, "33_*_*")));
  }

  @Test
  public void testMergeIidMapsInOrderAllowingOverwriting() {
    assertThat(
        mergeIidMapsInOrderAllowingOverwriting(emptyIidMap(), emptyIidMap()),
        iidMapEqualityMatcher(emptyIidMap()));

    assertThat(
        mergeIidMapsInOrderAllowingOverwriting(emptyIidMap(), emptyIidMap(), emptyIidMap()),
        iidMapEqualityMatcher(emptyIidMap()));

    assertThat(
        mergeIidMapsInOrderAllowingOverwriting(
            iidMapOf(
                STOCK_A1, "1x",
                STOCK_A2, "2x",
                STOCK_A3, "3x"),
            iidMapOf(
                STOCK_A1, "1y",
                STOCK_A2, "2y")),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "1y",
                STOCK_A2, "2y",
                STOCK_A3, "3x")));

    assertThat(
        mergeIidMapsInOrderAllowingOverwriting(
            iidMapOf(
                STOCK_A1, "1x",
                STOCK_A2, "2x",
                STOCK_A3, "3x"),
            iidMapOf(
                STOCK_A1, "1y",
                STOCK_A2, "2y"),
            singletonIidMap(
                STOCK_A1, "1z")),
        iidMapEqualityMatcher(
            iidMapOf(
                STOCK_A1, "1z",
                STOCK_A2, "2y",
                STOCK_A3, "3x")));
  }

}
