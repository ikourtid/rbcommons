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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_C;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_D;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ArrayIndexMappingTest.arrayIndexMappingMatcher;
import static com.rb.nonbiz.collections.MutableIndexableArray1D.emptyMutableIndexableArray1D;
import static com.rb.nonbiz.collections.MutableIndexableArray1D.mutableIndexableArray1D;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.arrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.preciseValueListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.typeSafeEqualTo;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertThrows;
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
public class MutableIndexableArray1DTest extends RBTestMatcher<MutableIndexableArray1D<InstrumentId, UnitFraction>> {

  @Test
  public void happyPath() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });

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
    assertIllegalArgumentException( () -> mutableIndexableArray1D(
        simpleArrayIndexMapping(emptyList()),
        new UnitFraction[] {}));
  }

  @Test
  public void tooManyKeys_throws() {
    assertIllegalArgumentException( () -> mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2)
        }));
  }

  @Test
  public void tooFewKeys_throws() {
    assertIllegalArgumentException( () -> mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        }));
  }

  @Test
  public void unknownObject_throws() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });
    assertIllegalArgumentException( () -> array.get(STOCK_D));
  }

  @Test
  public void badIndexInGetOrSet_throws() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });
    assertThrows(IllegalArgumentException.class, () -> array.getByIndex(-1));
    assertThrows(IllegalArgumentException.class, () -> array.getByIndex(3));
    assertThrows(IllegalArgumentException.class, () -> array.set(-1, unitFraction(0.123)));
    assertThrows(IllegalArgumentException.class, () -> array.set(3, unitFraction(0.123)));
  }

  @Test
  public void testValuesStream() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });
    assertThat(
        array.valuesStream().collect(Collectors.toList()),
        preciseValueListMatcher(ImmutableList.of(unitFraction(0.1), unitFraction(0.2), unitFraction(0.3)), 1e-8));
    assertFalse(emptyMutableIndexableArray1D().valuesStream().iterator().hasNext());
  }

  @Test
  public void testForEachEntry() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(instrumentId(11), instrumentId(22), instrumentId(33))),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });
    List<String> strings = newArrayList();
    array.forEachEntry((instrumentId, unitFraction) ->
        strings.add(Strings.format("%s_%s", instrumentId.asLong(), unitFraction.toPercentString(0))));
    assertEquals(
        ImmutableList.of("11_10 %", "22_20 %", "33_30 %"),
        strings);
  }

  @Test
  public void testCopyWithValuesReplaced() {
    MutableIndexableArray1D<InstrumentId, UnitFraction> array = mutableIndexableArray1D(
        simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });

    assertThat(
        array.copyWithValuesReplaced(new String[] { "A", "B", "C" }),
        mutableIndexableArray1DMatcher(
            mutableIndexableArray1D(
                simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
                new String[] { "A", "B", "C" }),
            instrumentId -> typeSafeEqualTo(instrumentId),
            stringValue  -> typeSafeEqualTo(stringValue)));
  }

  @Override
  public MutableIndexableArray1D<InstrumentId, UnitFraction> makeTrivialObject() {
    return mutableIndexableArray1D(
        simpleArrayIndexMapping(singletonList(STOCK_A)),
        new UnitFraction[] { UNIT_FRACTION_0 });
  }

  @Override
  public MutableIndexableArray1D<InstrumentId, UnitFraction> makeNontrivialObject() {
    return mutableIndexableArray1D(simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1),
            unitFraction(0.2),
            unitFraction(0.3)
        });
  }

  @Override
  public MutableIndexableArray1D<InstrumentId, UnitFraction> makeMatchingNontrivialObject() {
    double e = 1e-9; // epsilon
    return mutableIndexableArray1D(simpleArrayIndexMapping(ImmutableList.of(STOCK_A, STOCK_B, STOCK_C)),
        new UnitFraction[] {
            unitFraction(0.1 + e),
            unitFraction(0.2 + e),
            unitFraction(0.3 + e)
        });
  }

  @Override
  protected boolean willMatch(MutableIndexableArray1D<InstrumentId, UnitFraction> expected,
                              MutableIndexableArray1D<InstrumentId, UnitFraction> actual) {
    return mutableIndexableArray1DMatcher(expected, i -> typeSafeEqualTo(i), f -> preciseValueMatcher(f, 1e-8))
        .matches(actual);
  }

  public static <K, V> TypeSafeMatcher<MutableIndexableArray1D<K, V>> mutableIndexableArray1DMatcher(
      MutableIndexableArray1D<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected,
        match(v -> v.getRawArrayIndexMapping(), f -> arrayIndexMappingMatcher(f, keyMatcherGenerator)),
        match(v -> v.getRawArrayUnsafe(), f -> arrayMatcher(f, valueMatcherGenerator)));
  }

  public static <K, V> TypeSafeMatcher<MutableIndexableArray1D<K, V>> mutableIndexableArray1DIgnoringOrderMatcher(
      MutableIndexableArray1D<K, V> expected,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual -> {
      RBSet<K> expectedKeysAsSet = expected.getKeysRBSet();
      RBSet<K> actualKeysAsSet = actual.getKeysRBSet();
      if (!rbSetEqualsMatcher(expectedKeysAsSet).matches(actualKeysAsSet)) {
        return false; // keys (regardless of order) in the array index mappings are not the same; objects aren't matching
      }
      return expectedKeysAsSet // or actualKeysAsSet; they're the same
          .stream()
          .allMatch(key -> valueMatcherGenerator.apply(expected.get(key)).matches(actual.get(key)));
    });
  }

  public static <K, V> TypeSafeMatcher<MutableIndexableArray1D<K, V>> mutableIndexableArray1DIgnoringOrderMatcher(
      MutableIndexableArray1D<K, V> expected,
      MatcherGenerator<K> keyMatcherGenerator,
      Comparator<K> keyComparator,
      MatcherGenerator<V> valueMatcherGenerator) {
    return makeMatcher(expected, actual -> {
      List<K> expectedKeysOrdered = expected.getKeysRBSet().stream().sorted(keyComparator).collect(Collectors.toList());
      List<K> actualKeysOrdered   =   actual.getKeysRBSet().stream().sorted(keyComparator).collect(Collectors.toList());
      if (!orderedListMatcher(expectedKeysOrdered, keyMatcherGenerator).matches(actualKeysOrdered)) {
        return false;
      }
      return IntStream.range(0, expectedKeysOrdered.size())
          .allMatch(i -> {
            K keyInExpected = expectedKeysOrdered.get(i);
            K keyInActual = actualKeysOrdered.get(i);
            V valueInExpected = expected.get(keyInExpected);
            V valueInActual = actual.get(keyInActual);
            return valueMatcherGenerator.apply(valueInExpected).matches(valueInActual);
          });
    });
  }

}
