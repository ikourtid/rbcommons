package com.rb.nonbiz.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.rb.biz.marketdata.instrumentmaster.InstrumentMaster;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import org.junit.Test;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;

import static com.rb.biz.marketdata.FakeInstruments.STOCK_A;
import static com.rb.biz.marketdata.FakeInstruments.STOCK_B;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedAllowingEmptyInstrumentMaster.hardCodedAllowingEmptyInstrumentMaster;
import static com.rb.biz.marketdata.instrumentmaster.HardCodedInstrumentMaster.singletonHardCodedInstrumentMaster;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapConstructors.iidMapFromRBMap;
import static com.rb.nonbiz.collections.IidPartition.iidPartition;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.date.RBDates.UNUSED_DATE;
import static com.rb.nonbiz.json.JsonElementType.JSON_STRING;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DATE;
import static com.rb.nonbiz.text.Strings.*;
import static com.rb.nonbiz.types.PreciseValue.formatWithoutCommas;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static java.util.Collections.emptyList;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringsTest {

  @Test
  public void testFormatOptionalDouble() {
    assertEquals("(n/a)", formatOptionalDouble(OptionalDouble.empty()));
    assertEquals("1.23", formatOptionalDouble(OptionalDouble.of(1.23)));
  }

  @Test
  public void toBasisPoints() {
    assertEquals("1234.0 bps", Strings.toBasisPoints(0.1234, 1));
    assertEquals("10000.0 bps", Strings.toBasisPoints(1, 1));
    assertEquals("2000.0 bps", Strings.toBasisPoints(0.2, 1));
    assertEquals("0.0 bps", Strings.toBasisPoints(0, 1));
    assertEquals("3333.3 bps", Strings.toBasisPoints(1 / 3.0, 1));

    assertEquals("1234.0", Strings.toBasisPoints(0.1234, 1, false));
    assertEquals("10000.0", Strings.toBasisPoints(1, 1, false));
    assertEquals("2000.0", Strings.toBasisPoints(0.2, 1, false));
    assertEquals("0.0", Strings.toBasisPoints(0, 1, false));
    assertEquals("3333.3", Strings.toBasisPoints(1 / 3.0, 1, false));
  }

  @Test
  public void testJoinWithHarvardComma() {
    assertEquals("",               joinWithHarvardComma(emptyList()));
    assertEquals("X",              joinWithHarvardComma(singletonList("X")));
    assertEquals("X and Y",        joinWithHarvardComma(ImmutableList.of("X", "Y")));
    assertEquals("X, Y, and Z",    joinWithHarvardComma(ImmutableList.of("X", "Y", "Z")));
    assertEquals("X, Y, Z, and W", joinWithHarvardComma(ImmutableList.of("X", "Y", "Z", "W")));
  }

  @Test
  public void testToStandaloneSentence() {
    assertEquals("We rule.", toTrimmedStandaloneSentence("we rule"));
    assertEquals("We rule.", toTrimmedStandaloneSentence("we rule."));
    assertEquals("We rule.", toTrimmedStandaloneSentence("We rule"));
    assertEquals("We rule.", toTrimmedStandaloneSentence("We rule."));
    assertEquals("We rule.", toTrimmedStandaloneSentence(" we rule "));
    assertEquals("We rule.", toTrimmedStandaloneSentence(" we rule. "));
    assertEquals("We rule.", toTrimmedStandaloneSentence(" We rule "));
    assertEquals("We rule.", toTrimmedStandaloneSentence(" We rule. "));
  }

  @Test
  public void testFormatRange() {
    BiConsumer<String, Range<Integer>> asserter = (expectedResult, range) ->
        assertEquals(
            expectedResult,
            Strings.formatRange(range, v -> Strings.format("_%s_", v)));
    asserter.accept("(-∞…∞)", Range.all());
    asserter.accept("[_1_…∞)", Range.atLeast(1));
    asserter.accept("(_1_…∞)", Range.greaterThan(1));
    asserter.accept("(-∞…_1_)", Range.lessThan(1));
    asserter.accept("(-∞…_1_]", Range.atMost(1));
    asserter.accept("[_1_…_2_]", Range.closed(1, 2));
    asserter.accept("[_1_…_2_)", Range.closedOpen(1, 2));
    asserter.accept("(_1_…_2_]", Range.openClosed(1, 2));
    asserter.accept("(_1_…_2_)", Range.open(1, 2));
    asserter.accept("[_1_…_1_]", Range.singleton(1));
  }

  @Test
  public void testFormatCollectionInOrderWithSize() {
    RBSet<String> rbSet = rbSetOf("0", "5", "1", "6", "2", "7", "3", "8", "4", "9");

    assertEquals(
        "10 : 0 1 2 3 4 5 6 7 8 9",
        Strings.formatCollectionInOrder(rbSet.asSet()));
    assertEquals(
        "10 : 0 1 2 3 4 5 6 7 8 9",
        formatCollectionInOrder(rbSet.asSet(), String::compareTo));

    // Not printing size when <= 1 currently (see Strings#sizePrefix)
    assertEquals("", Strings.formatCollectionInOrder(RBSet.<String>emptyRBSet().asSet()));
    assertEquals("", formatCollectionInOrder(RBSet.<String>emptyRBSet().asSet(), String::compareTo));
  }

  @Test
  public void testFormatListInExistingOrder() {
    assertEquals(
        "10 : 0 5 1 6 2 7 3 8 4 9",
        formatListInExistingOrder(ImmutableList.of("0", "5", "1", "6", "2", "7", "3", "8", "4", "9")));
  }

  @Test
  public void testFormatListInExistingOrderWithTransformer() {
    assertEquals(
        "10 : 0x 5x 1x 6x 2x 7x 3x 8x 4x 9x",
        formatListInExistingOrder(
            ImmutableList.of("0", "5", "1", "6", "2", "7", "3", "8", "4", "9"),
            v -> v + "x"));
  }

  @Test
  public void testFormatMapInKeyOrder() {
    RBMap<String, Integer> rbMap = rbMapOf(
        "D", 1,
        "C", 2,
        "B", 3,
        "A", 4);
    assertEquals("4 : A = 4 : B = 3 : C = 2 : D = 1", formatMapInKeyOrder(rbMap, String::compareTo, " : "));
    assertEquals("4 : D = 1 : C = 2 : B = 3 : A = 4", formatMapInKeyOrder(rbMap, reverseOrder(String::compareTo), " : "));
    assertEquals("4 : A = 4|||B = 3|||C = 2|||D = 1", formatMapInKeyOrder(rbMap, String::compareTo, "|||"));
  }

  @Test
  public void testFormatMapInValueOrder() {
    RBMap<String, Integer> rbMap = rbMapOf(
        "D", 1,
        "C", 2,
        "B", 3,
        "A", 4);
    assertEquals("4 : D = 1 : C = 2 : B = 3 : A = 4", Strings.formatMapInValueOrder(rbMap, Integer::compareTo, " : "));
    assertEquals("4 : A = 4 : B = 3 : C = 2 : D = 1", Strings.formatMapInValueOrder(rbMap, reverseOrder(Integer::compareTo), " : "));
    assertEquals("4 : D = 1|||C = 2|||B = 3|||A = 4", Strings.formatMapInValueOrder(rbMap, Integer::compareTo, "|||"));
  }

  @Test
  public void testFormatMapInTransformedValueOrder() {
    RBMap<String, Double> rbMap = rbMapOf(
        "D", 1.1,
        "C", 2.2000000000001,  // we want to format the output to avoid tiny offsets
        "B", 3.2999999999999,
        "A", 4.4);
    assertEquals(
        "4 : D = 1.1 : C = 2.2 : B = 3.3 : A = 4.4",
        Strings.formatMapInValueOrder(rbMap, Double::compareTo, " : ", v -> formatWithoutCommas(8).format(v)));
    assertEquals(
        "4 : A = 4.4 : B = 3.3 : C = 2.2 : D = 1.1",
        Strings.formatMapInValueOrder(rbMap, reverseOrder(Double::compareTo), " : ", v -> formatWithoutCommas(8).format(v)));
    assertEquals(
        "4 : D = 1.1|||C = 2.2|||B = 3.3|||A = 4.4",
        Strings.formatMapInValueOrder(rbMap, Double::compareTo, "|||", v -> formatWithoutCommas(8).format(v)));
  }

  @Test
  public void testFormatOptional() {
    assertEquals("abc", Strings.formatOptional(Optional.of("abc")));
    assertEquals("(none)", Strings.formatOptional(Optional.empty()));
  }

  @Test
  public void testFormatOptionalTransformValue() {
    assertEquals("abc_XYZ", Strings.formatOptional(Optional.of("abc"), v -> v + "_XYZ"));
    assertEquals("(none)",  Strings.formatOptional(Optional.empty(),   v -> v + "_XYZ"));
  }

  @Test
  public void testFormatOptionalPrintsInstrument() {
    InstrumentMaster instrumentMaster = singletonHardCodedInstrumentMaster(instrumentId(1_234L), "IBM");

    assertEquals("IBM (iid 1_234 )", formatOptionalPrintsInstruments(
        Optional.of(instrumentId(1_234L)),instrumentMaster, UNUSED_DATE));

    assertEquals("(none)", formatOptionalPrintsInstruments(
        Optional.<InstrumentId>empty(), instrumentMaster, UNUSED_DATE));
  }

  @Test
  public void testAsSingleLine() {
    // Simple cases, where we don't need to add a space between lines

    // You would think that we should disallow this by changing the method signature to take 2 non-varargs arguments
    // (String first, String second, String ... rest), but that doesn't work, because there are places where we
    // don't know for sure if we have more than 1 string.
    assertEquals("a",      asSingleLine("a"));

    assertEquals("a b",    asSingleLine("a ", "b"));
    assertEquals("a\tb",   asSingleLine("a\t", "b"));
    assertEquals("a b",    asSingleLine("a", " b"));
    assertEquals("a\tb",   asSingleLine("a", "\tb"));
    assertEquals("a  b",   asSingleLine("a ", " b"));
    assertEquals("a\t\tb", asSingleLine("a\t", "\tb"));

    assertEquals("a b c",    asSingleLine("a ", "b", " c"));
    assertEquals("a\tb c",   asSingleLine("a\t", "b", " c"));
    assertEquals("a b c",    asSingleLine("a", " b", " c"));
    assertEquals("a\tb c",   asSingleLine("a", "\tb", " c"));
    assertEquals("a  b c",   asSingleLine("a ", " b", " c"));
    assertEquals("a\t\tb c", asSingleLine("a\t", "\tb", " c"));

    // In the following cases, the method adds a space, in order to avoid concatenating two words into a single one.
    assertEquals("a b", asSingleLine("a", "b"));
    assertEquals("a b c", asSingleLine("a", "b", "c"));
  }

  @Test
  public void testAsSingleLineWithNewlines() {
    assertEquals("a\nb\n", asSingleLineWithNewlines("a", "b"));
    assertEquals("a\nb\nc\n", asSingleLineWithNewlines("a", "b", "c"));
  }

  @Test
  public void testFirstCharacterIsWhitespace() {
    rbSetOf("", " ", "!", "\t", "9", " x", "!x", "\tx", "9x")
        .forEach(v -> assertFalse("Problem with " + v, firstCharacterIsAlphabetic(v)));

    rbSetOf("x", "x ", "x!", "xy")
        .forEach(v -> assertTrue("Problem with " + v, firstCharacterIsAlphabetic(v)));
  }

  @Test
  public void testLastCharacterIsWhitespace() {
    rbSetOf("", " ", "!", "\t", "9", "x ", "x!", "x\t", "x9")
        .forEach(v -> assertFalse("Problem with " + v, lastCharacterIsAlphabetic(v)));

    rbSetOf("x", " x", "!x", "xy")
        .forEach(v -> assertTrue("Problem with " + v, lastCharacterIsAlphabetic(v)));
  }

  @Test
  public void testFormatEnumMapWhereValuesPrintInstruments() {
    InstrumentMaster instrumentMaster = hardCodedAllowingEmptyInstrumentMaster(
        STOCK_A, "STOCK_A",
        STOCK_B, "STOCK_B");
    assertThat(
        // The mapping here, from JsonElementType to partition, isn't useful for practical reasons, but it
        // serves to test the formatEnumMap... function because partitions print instruments.
        formatRBEnumMapWhereValuesPrintInstruments(
            singletonRBEnumMap(JSON_STRING, iidPartition(iidMapFromRBMap(rbMapOf(
                STOCK_A, unitFraction(0.5),
                STOCK_B, unitFraction(0.5))))),
            instrumentMaster,
            DUMMY_DATE),
        stringMatcher("JSON_STRING = 50 % STOCK_B ; 50 % STOCK_A"));
  }

}
