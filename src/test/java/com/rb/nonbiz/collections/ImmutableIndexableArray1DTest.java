package com.rb.nonbiz.collections;

import com.google.common.collect.ImmutableList;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.testmatchers.RBMatchers.MatcherGenerator;
import com.rb.nonbiz.testutils.RBTestMatcher;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.types.UnitFraction;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.emptyImmutableIndexableArray1D;
import static com.rb.nonbiz.collections.ImmutableIndexableArray1D.immutableIndexableArray1D;
import static com.rb.nonbiz.collections.MutableIndexableArray1D.emptyMutableIndexableArray1D;
import static com.rb.nonbiz.collections.MutableIndexableArray1D.mutableIndexableArray1D;
import static com.rb.nonbiz.collections.MutableIndexableArray1DTest.mutableIndexableArray1DIgnoringOrderMatcher;
import static com.rb.nonbiz.collections.MutableIndexableArray1DTest.mutableIndexableArray1DMatcher;
import static com.rb.nonbiz.collections.RBLists.concatenateFirstSecondAndRest;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.preciseValueListMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * The test is not generic, but publicly exposed static matcher is.
 */
public class ImmutableIndexableArray1DTest extends RBTestMatcher<ImmutableIndexableArray1D<InstrumentId, UnitFraction>> {

  public static <K, V> ImmutableIndexableArray1D<K, V> singletonImmutableIndexableArray1D(K key, V value) {
    SimpleArrayIndexMapping<K> arrayIndexMapping = simpleArrayIndexMapping(singletonList(key));
    //noinspection unchecked
    V[] array1 = (V[]) new Object[] { value };
    return ImmutableIndexableArray1D.<K, V>immutableIndexableArray1D(mutableIndexableArray1D(arrayIndexMapping, array1));
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> testImmutableIndexableArray1D(
      K key1, V value1,
      K key2, V value2) {
    //noinspection unchecked
    return ImmutableIndexableArray1D.<K, V>immutableIndexableArray1D(
        simpleArrayIndexMapping(key1, key2),
        (V[]) new Object[] { value1, value2 });
  }

  public static <K, V> ImmutableIndexableArray1D<K, V> testImmutableIndexableArray1D(
      K key1, V value1,
      K key2, V value2,
      K key3, V value3) {
    //noinspection unchecked
    return ImmutableIndexableArray1D.<K, V>immutableIndexableArray1D(
        simpleArrayIndexMapping(key1, key2, key3),
        (V[]) new Object[] { value1, value2, value3 });
  }

  @SafeVarargs
  public static <K, V> ImmutableIndexableArray1D<K, V> testImmutableIndexableArray1D(
      Function<V, K> keyExtractor, V first, V second, V ... rest) {
    return testImmutableIndexableArray1D(keyExtractor, concatenateFirstSecondAndRest(first, second, rest));
  }

  /**
   * unfortunately we need the above because we want an array of V
   * */
  public static <K, V> ImmutableIndexableArray1D<K, V> testImmutableIndexableArray1D(
      Function<V, K> keyExtractor, List<V> values) {
    //noinspection unchecked
    return immutableIndexableArray1D(
        simpleArrayIndexMapping(values.stream().map(keyExtractor).collect(Collectors.toList())),
        (V[]) values.toArray());
  }

  @Test
  public void happyPath() {
    ImmutableIndexableArray1D<InstrumentId, UnitFraction> array = testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1),
        STOCK_B, unitFraction(0.2),
        STOCK_C, unitFraction(0.3));

    assertEquals(3, array.size());
    assertEquals(unitFraction(0.1), array.getByIndex(0));
    assertEquals(unitFraction(0.2), array.getByIndex(1));
    assertEquals(unitFraction(0.3), array.getByIndex(2));
    assertEquals(unitFraction(0.1), array.get(STOCK_A));
    assertEquals(unitFraction(0.2), array.get(STOCK_B));
    assertEquals(unitFraction(0.3), array.get(STOCK_C));
    array.set(STOCK_B, unitFraction(0.4));
    assertEquals(unitFraction(0.4), array.get(STOCK_B));

    assertEquals(STOCK_A, array.getKey(0));
    assertEquals(STOCK_B, array.getKey(1));
    assertEquals(STOCK_C, array.getKey(2));
  }

  @Test
  public void nothingInMapping_throws() {
    assertIllegalArgumentException( () -> immutableIndexableArray1D(
        simpleArrayIndexMapping(emptyList()),
        new UnitFraction[] { } ));
  }

  @Test
  public void tooManyKeys_throws() {
    assertIllegalArgumentException( () -> immutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2)
        }));
  }

  @Test
  public void tooFewKeys_throws() {
    assertIllegalArgumentException( () -> immutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        }));
  }

  @Test
  public void unknownObject_throws() {
    ImmutableIndexableArray1D<InstrumentId, UnitFraction> array = testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1),
        STOCK_B, unitFraction(0.2),
        STOCK_C, unitFraction(0.3));
    assertIllegalArgumentException( () -> array.get(STOCK_D));
  }

  @Test
  public void badIndexInGetOrSet_throws() {
    ImmutableIndexableArray1D<InstrumentId, UnitFraction> array = testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1),
        STOCK_B, unitFraction(0.2),
        STOCK_C, unitFraction(0.3));
    assertThrows(IllegalArgumentException.class, () -> array.getByIndex(-1));
    assertThrows(IllegalArgumentException.class, () -> array.getByIndex(3));
    assertThrows(IllegalArgumentException.class, () -> array.set(-1, unitFraction(0.123)));
    assertThrows(IllegalArgumentException.class, () -> array.set(3, unitFraction(0.123)));
  }

  @Test
  public void testValuesStream() {
    ImmutableIndexableArray1D<InstrumentId, UnitFraction> array = testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1),
        STOCK_B, unitFraction(0.2),
        STOCK_C, unitFraction(0.3));
    assertThat(
        array.valuesStream().collect(Collectors.toList()),
        preciseValueListMatcher(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.3)), DEFAULT_EPSILON_1e_8));
    assertFalse(emptyMutableIndexableArray1D().valuesStream().iterator().hasNext());
  }

  @Test
  public void testForEachEntry() {
    ImmutableIndexableArray1D<InstrumentId, UnitFraction> array = testImmutableIndexableArray1D(
        instrumentId(11), unitFraction(0.1),
        instrumentId(22), unitFraction(0.2),
        instrumentId(33), unitFraction(0.3));
    List<String> strings = newArrayList();
    array.forEachEntry( (instrumentId, unitFraction) ->
        strings.add(Strings.format("%s_%s", instrumentId.asLong(), unitFraction.toPercentString(0))));
    assertEquals(
        ImmutableList.of("11_10 %", "22_20 %", "33_30 %"),
        strings);
  }

  @Test
  public void testFilterKeys() {
    ImmutableIndexableArray1D<String, Integer> empty = emptyImmutableIndexableArray1D(new Integer[] {});
    ImmutableIndexableArray1D<String, Integer> onlyA;
    ImmutableIndexableArray1D<String, Integer> onlyB;
    ImmutableIndexableArray1D<String, Integer> ab;
    onlyA = singletonImmutableIndexableArray1D("a", 11);
    onlyB = singletonImmutableIndexableArray1D("b", 22);
    ab    = testImmutableIndexableArray1D("a", 11, "b", 22);

    BiConsumer<ImmutableIndexableArray1D<String, Integer>, ImmutableIndexableArray1D<String, Integer>> asserter =
        (expectedFilteredResult, actualFiltered) ->
            assertThat(
                actualFiltered,
                immutableIndexableArray1DMatcher(
                    expectedFilteredResult, k -> typeSafeEqualTo(k), v -> typeSafeEqualTo(v)));
    IntFunction<Integer[]> arrayInstantiator = size -> new Integer[size];
    asserter.accept(empty, empty.filterKeys(arrayInstantiator, k -> k.equals("a")));
    asserter.accept(empty, empty.filterKeys(arrayInstantiator, k -> false));
    asserter.accept(empty, empty.filterKeys(arrayInstantiator, k -> true));

    asserter.accept(onlyA, onlyA.filterKeys(arrayInstantiator, k -> k.equals("a")));
    asserter.accept(empty, onlyA.filterKeys(arrayInstantiator, k -> k.equals("b")));
    asserter.accept(empty, onlyA.filterKeys(arrayInstantiator, k -> false));
    asserter.accept(onlyA, onlyA.filterKeys(arrayInstantiator, k -> true));

    asserter.accept(onlyA, ab.filterKeys(arrayInstantiator, k -> k.equals("a")));
    asserter.accept(onlyB, ab.filterKeys(arrayInstantiator, k -> k.equals("b")));
    asserter.accept(empty, ab.filterKeys(arrayInstantiator, k -> false));
    asserter.accept(ab,    ab.filterKeys(arrayInstantiator, k -> true));
  }

  @Test
  public void testCopyWithEntriesTransformed() {
    assertThat(
        immutableIndexableArray1D(
            simpleArrayIndexMapping(ImmutableList.of(77, 88, 99)),
            new UnitFraction[] {
                unitFraction(0.1),
                unitFraction(0.2),
                unitFraction(0.3)
            })
            .copyWithEntriesTransformed(
                (index, key, value) -> Strings.format("%s_%s_%s", index, key, value.toString(2, 2))),
        immutableIndexableArray1DMatcher(
            immutableIndexableArray1D(
                simpleArrayIndexMapping(77, 88, 99),
                new String[] { "0_77_0.10", "1_88_0.20", "2_99_0.30" }),
            stringKey    -> typeSafeEqualTo(stringKey),
            stringValue  -> typeSafeEqualTo(stringValue)));
  }

  @Test
  public void testCopyWithValuesTransformed() {
    assertThat(
        immutableIndexableArray1D(
            simpleArrayIndexMapping(ImmutableList.of(77, 88, 99)),
            new UnitFraction[] {
                unitFraction(0.1),
                unitFraction(0.2),
                unitFraction(0.3)
            })
            .copyWithValuesTransformed(
                value -> Strings.format("_%s", value.toString(2, 2))),
        immutableIndexableArray1DMatcher(
            immutableIndexableArray1D(
                simpleArrayIndexMapping(77, 88, 99),
                new String[] { "_0.10", "_0.20", "_0.30" }),
            stringKey    -> typeSafeEqualTo(stringKey),
            stringValue  -> typeSafeEqualTo(stringValue)));
  }

  @Override
  public ImmutableIndexableArray1D<InstrumentId, UnitFraction> makeTrivialObject() {
    return singletonImmutableIndexableArray1D(STOCK_A, UNIT_FRACTION_0);
  }

  @Override
  public ImmutableIndexableArray1D<InstrumentId, UnitFraction> makeNontrivialObject() {
    return testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1),
        STOCK_B, unitFraction(0.2),
        STOCK_C, unitFraction(0.3));
  }

  @Override
  public ImmutableIndexableArray1D<InstrumentId, UnitFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return testImmutableIndexableArray1D(
        STOCK_A, unitFraction(0.1 + e),
        STOCK_B, unitFraction(0.2 + e),
        STOCK_C, unitFraction(0.3 + e));
  }

  @Override
  protected boolean willMatch(ImmutableIndexableArray1D<InstrumentId, UnitFraction> expected,
                              ImmutableIndexableArray1D<InstrumentId, UnitFraction> actual) {
    return immutableIndexableArray1DMatcher(expected, i -> typeSafeEqualTo(i), f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8))
        .matches(actual);
  }

  public static <K, V> TypeSafeMatcher<ImmutableIndexableArray1D<K, V>> immutableIndexableArray1DMatcher(
      ImmutableIndexableArray1D<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawMutableArray1D(), f -> mutableIndexableArray1DMatcher(f, keyMatcherGenerator, valueMatcherGenerator)));
  }

  // Sometimes we want to match 2 arrays with unordered semantics. For example, with SingleInstrumentTaxLotsByTaxLotId,
  // we store stuff in an ImmutableIndexableArray1D so as to fix the ordering for determinism purposes,
  // but if items are out of order, the two objects should still count as matching.
  public static <K, V> TypeSafeMatcher<ImmutableIndexableArray1D<K, V>> immutableIndexableArray1DIgnoringOrderMatcher(
      ImmutableIndexableArray1D<K, V> expected,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawMutableArray1D(), f -> mutableIndexableArray1DIgnoringOrderMatcher(f, valueMatcherGenerator)));
  }

  public static <K, V> TypeSafeMatcher<ImmutableIndexableArray1D<K, V>> immutableIndexableArray1DIgnoringOrderMatcher(
      ImmutableIndexableArray1D<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      Comparator<K> keyComparator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawMutableArray1D(), f -> mutableIndexableArray1DIgnoringOrderMatcher(f,
            keyMatcherGenerator, keyComparator, valueMatcherGenerator)));
  }

}
