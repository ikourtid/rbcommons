package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.rb.biz.jsonapi.JsonTickerMap;
import com.rb.biz.types.Money;
import com.rb.biz.types.asset.InstrumentId;
import com.rb.nonbiz.collections.ArrayIndexMapping;
import com.rb.nonbiz.collections.CaseInsensitiveStringFilter;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.MutableRBSet;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.collections.SimpleArrayIndexMapping;
import com.rb.nonbiz.jsonapi.RangeJsonApiConverter;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.text.RBSetOfHasUniqueId;
import com.rb.nonbiz.text.Strings;
import com.rb.nonbiz.text.TestHasUniqueId;
import com.rb.nonbiz.text.UniqueId;
import com.rb.nonbiz.types.Pointer;
import com.rb.nonbiz.types.UnitFraction;
import org.junit.Test;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static com.rb.biz.jsonapi.JsonTickerMapImplTest.jsonTickerMap;
import static com.rb.biz.types.Money.money;
import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.ClosedRange.closedRange;
import static com.rb.nonbiz.collections.ClosedRangeTest.closedRangeMatcher;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.IidMapTest.iidMapMatcher;
import static com.rb.nonbiz.collections.MutableRBSet.newMutableRBSet;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.emptyRBMap;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.newRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.collections.SimpleArrayIndexMapping.simpleArrayIndexMapping;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonPercentage;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.json.RBJsonObjects.*;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonArrayEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.orderedJsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbEnumMapMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBMapMatchers.rbMapMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.preciseValueRangeMatcher;
import static com.rb.nonbiz.testmatchers.RBRangeMatchers.rangeEqualityMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.preciseValueMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.stringMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.doubleExplained;
import static com.rb.nonbiz.testutils.RBCommonsIntegrationTest.makeRealObject;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.text.RBSetOfHasUniqueId.rbSetOfHasUniqueId;
import static com.rb.nonbiz.text.RBSetOfHasUniqueIdTest.rbSetOfHasUniqueIdMatcher;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueId;
import static com.rb.nonbiz.text.TestHasUniqueId.testHasUniqueIdMatcher;
import static com.rb.nonbiz.text.UniqueId.uniqueId;
import static com.rb.nonbiz.text.UniqueIdTest.uniqueIdMatcher;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.Pointer.uninitializedPointer;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_1;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.rbEnumMapOf;
import static com.rb.nonbiz.util.RBEnumMapSimpleConstructors.singletonRBEnumMap;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class RBJsonObjectsTest {

  private final JsonTickerMap TICKER_MAP = jsonTickerMap(iidMapOf(
      instrumentId(1), "S1",
      instrumentId(2), "S2"));

  @Test
  public void testClosedRangeToJsonObject() {
    BiConsumer<Range<UnitFraction>, JsonObject> asserter = (range, expectedJsonObject) ->
        assertThat(
            closedRangeToJsonObject(range, v -> jsonPercentage(v)),
            jsonObjectEpsilonMatcher(expectedJsonObject));
    asserter.accept(
        Range.closed(UNIT_FRACTION_0, UNIT_FRACTION_1),
        jsonObject(
            "min", jsonDouble(0.0),
            "max", jsonDouble(100.0)));
    asserter.accept(
        Range.atLeast(UNIT_FRACTION_0),
        singletonJsonObject("min", jsonDouble(0.0)));
    asserter.accept(
        Range.atMost(UNIT_FRACTION_1),
        singletonJsonObject("max", jsonDouble(100.0)));
    asserter.accept(Range.all(),
        emptyJsonObject());

    // confirm that 0 and 1 aren't special:
    asserter.accept(
        Range.closed(unitFraction(0.123), unitFraction(0.789)),
        jsonObject(
            "min", jsonDouble(12.3),
            "max", jsonDouble(78.9)));
    asserter.accept(Range.atLeast(unitFraction(0.123)), singletonJsonObject("min", jsonDouble(12.3)));
    asserter.accept(Range.atMost(unitFraction(0.789)), singletonJsonObject("max", jsonDouble(78.9)));

    // Note: it is possible for the serializer to create invalid ranges.
    // In the following, the serializer changes the sign of the range endpoints.
    assertThat(
        closedRangeToJsonObject(Range.closed(UNIT_FRACTION_0, UNIT_FRACTION_1), v -> jsonDouble(-v.doubleValue())),
        jsonObjectEpsilonMatcher(jsonObject(
            "min", jsonDouble(0.0),
            "max", jsonDouble(-1.0))));   // 'max' is less than 'min'
    assertThat(
        closedRangeToJsonObject(Range.closed(unitFraction(0.123), unitFraction(0.789)), v -> jsonDouble(-v.doubleValue())),
        jsonObjectEpsilonMatcher(jsonObject(
            "min", jsonDouble(-0.123),
            "max", jsonDouble(-0.789))));  // 'max' is less than 'min'

    // range must be closed
    assertIllegalArgumentException( () -> closedRangeToJsonObject(Range.greaterThan(UNIT_FRACTION_0), v -> jsonDouble(v)));
    assertIllegalArgumentException( () -> closedRangeToJsonObject(Range.lessThan(UNIT_FRACTION_0), v -> jsonDouble(v)));
    assertIllegalArgumentException( () -> closedRangeToJsonObject(
        Range.open(UNIT_FRACTION_0, UNIT_FRACTION_1),
        v -> jsonDouble(v)));
  }

  @Test
  public void testEnumMapToJsonObject() {
    assertThat(
        // Create single enumMap to a String.
        rbEnumMapToJsonObject(singletonRBEnumMap(TestEnumXYZ.X, "String1"), v -> jsonString(v)),
        // Match with an object built from scratch.
        jsonObjectEpsilonMatcher(
            // "_X" is TestEnumXYZ.X.toUniqueStableString().
            singletonJsonObject("_X", jsonString("String1"))));

    assertThat(
        // Create enumMap with 2 elements, each to a String.
        rbEnumMapToJsonObject(
            rbEnumMapOf(
                TestEnumXYZ.X, "String1",
                TestEnumXYZ.Z, "String3"),
            v -> jsonString(v)),
        // Match with an object built from scratch.
        jsonObjectEpsilonMatcher(
            jsonObject(
                // "_X" and "_Z" are TestEnumXYZ.X.toUniqueStableString() and TestEnumXYZ.Z.toUniqueStableString().
                "_X", jsonString("String1"),
                "_Z", jsonString("String3"))));

    RangeJsonApiConverter converter = makeRealObject(RangeJsonApiConverter.class);
    assertThat(
        // Create enumMap with 3 elements, each to a Range.
        // We use Range in this test because it requires its own JSON API converter.
        rbEnumMapToJsonObject(
            rbEnumMapOf(
                TestEnumXYZ.X, Range.atLeast(money(111)),
                TestEnumXYZ.Y, Range.atMost( money(222)),
                TestEnumXYZ.Z, Range.closed( money(100), money(200))),
            v -> converter.toJsonObject(v, v2 -> jsonDouble(v2.doubleValue()))),
        jsonObjectEpsilonMatcher(
            jsonObject(
                // "_X", "_Y", and "_Z" are the toUniqueStableString() representations of TestEnumXYZ.
                "_X", singletonJsonObject("min", jsonDouble(111.0)),
                "_Y", singletonJsonObject("max", jsonDouble(222.0)),
                "_Z", jsonObject(
                    "min", jsonDouble(100.0),
                    "max", jsonDouble(200.0)))));
  }

  @Test
  public void testJsonObjectToEnumMap() {
    // Create json object for a single enumMap to a String.
    assertThat(
        jsonObjectToRBEnumMap(
            // "_X" is TestEnumXYZ.X.toUniqueStableString().
            singletonJsonObject("_X", jsonString("String1")),
            TestEnumXYZ.class,
            k -> TestEnumXYZ.fromUniqueStableString(k),
            v -> v.getAsString()),
        rbEnumMapMatcher(
            singletonRBEnumMap(
                TestEnumXYZ.X, "String1"), f -> stringMatcher(f)));

    // Create json object for enumMap with 2 elements, each to a String.
    assertThat(
        jsonObjectToRBEnumMap(
            jsonObject(
                // "_X" and "_Z" are TestEnumXYZ.X.toUniqueStableString() and TestEnumXYZ.Z.toUniqueStableString().
                "_X", jsonString("String1"),
                "_Z", jsonString("String3")),
            TestEnumXYZ.class,
            k -> TestEnumXYZ.fromUniqueStableString(k),
            v -> v.getAsString()),
        rbEnumMapMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, "String1",
                TestEnumXYZ.Z, "String3"),
            f -> stringMatcher(f)));

    // Create json object for an enumMap with 3 elements, each to a Range.
    // We use Range in this test because it requires its own JSON API converter.
    RangeJsonApiConverter converter = makeRealObject(RangeJsonApiConverter.class);
    assertThat(
        jsonObjectToRBEnumMap(jsonObject(
                // "_X", "_Y", and "_Z" are the toUniqueStableString() representations of TestEnumXYZ.
                "_X", singletonJsonObject("min", jsonDouble(111.0)),
                "_Y", singletonJsonObject("max", jsonDouble(222.0)),
                "_Z", jsonObject(
                    "min", jsonDouble(100.0),
                    "max", jsonDouble(200.0))),
            TestEnumXYZ.class,
            k -> TestEnumXYZ.fromUniqueStableString(k),
            v -> converter.fromJsonObject((JsonObject) v, jsonPrimitive -> money(jsonPrimitive.getAsDouble()))),
        rbEnumMapMatcher(
            rbEnumMapOf(
                TestEnumXYZ.X, Range.atLeast(money(111)),
                TestEnumXYZ.Y, Range.atMost( money(222)),
                TestEnumXYZ.Z, Range.closed( money(100), money(200))),
            // For this test we're OK to do exact value matching.
            v -> rangeEqualityMatcher(v)));
  }

  @Test
  public void testJsonArrayToSimpleArrayMapping() {
    JsonArray jsonArray = jsonArray(jsonString("A"), jsonString("B"), jsonString("C"));
    ArrayIndexMapping<String> arrayIndexMapping =
        jsonArrayToSimpleArrayIndexMapping(jsonArray, jsonElement -> jsonElement.getAsString());
    assertEquals("A", arrayIndexMapping.getKey(0));
    assertEquals("B", arrayIndexMapping.getKey(1));
    assertEquals("C", arrayIndexMapping.getKey(2));
  }

  @Test
  public void testJsonObjectToRange() {
    BiConsumer<JsonObject, Range<UnitFraction>> asserter = (jsonObject, expectedRange) ->
        assertThat(
            jsonObjectToRange(jsonObject, v -> unitFraction(v.getAsDouble())),
            preciseValueRangeMatcher(expectedRange, DEFAULT_EPSILON_1e_8));

    // the 'min' and 'max' properties are both optional:
    asserter.accept(
        emptyJsonObject(),
        Range.all());
    asserter.accept(
        singletonJsonObject("min", jsonDouble(0.0)),
        Range.atLeast(UNIT_FRACTION_0));
    asserter.accept(
        singletonJsonObject("max", jsonDouble(1.0)),
        Range.atMost(UNIT_FRACTION_1));
    asserter.accept(
        jsonObject(
            "min", jsonDouble(0.0),
            "max", jsonDouble(1.0)),
        Range.closed(UNIT_FRACTION_0, UNIT_FRACTION_1));

    // it is possible for the JSON object to specify an invalid range:
    assertIllegalArgumentException( () -> RBJsonObjects.<UnitFraction>jsonObjectToRange(
        jsonObject(
            "min", jsonDouble(1.0),
            "max", jsonDouble(0.0)),   // invalid: max is less than min
        v -> unitFraction(v.getAsDouble())));

    // a typo in an optional JSON property (as opposed to simply omitting it)
    // will cause the conversion to fail:
    assertIllegalArgumentException( () -> RBJsonObjects.<UnitFraction>jsonObjectToRange(
        jsonObject(
            "mni", jsonDouble(0.5),     // TYPO: "min" -> "mni"
            "max", jsonDouble(1.0)),
        v -> unitFraction(v.getAsDouble())));
  }

  @Test
  public void testJsonObjectToClosedRange() {
    // the 'min' and 'max' properties must both exist for a ClosedRange.
    assertThat(
        jsonObjectToClosedRange(
            jsonObject(
                "min", jsonDouble(0.1),
                "max", jsonDouble(0.9)),
            v -> unitFraction(v.getAsDouble())),
        closedRangeMatcher(
            closedRange(unitFraction(0.1), unitFraction(0.9)),
            f -> preciseValueMatcher(f, DEFAULT_EPSILON_1e_8)));

    Consumer<JsonObject> assertThrows = invalidJsonObject -> assertIllegalArgumentException( () ->
        RBJsonObjects.<UnitFraction>jsonObjectToClosedRange(
            invalidJsonObject,
            v -> unitFraction(v.getAsDouble())));
    assertThrows.accept(emptyJsonObject());
    assertThrows.accept(
        singletonJsonObject("min", jsonDouble(0.1)));
    assertThrows.accept(
        singletonJsonObject("max", jsonDouble(0.9)));

    // it is possible for the JSON object to specify an invalid range:
    assertThrows.accept(
        jsonObject(
            "min", jsonDouble(0.8),
            "max", jsonDouble(0.2)));

    // a typo in an optional JSON property (as opposed to simply omitting it)
    // will cause the conversion to fail:
    assertThrows.accept(
        jsonObject(
            "mni", jsonDouble(0.1),
            "max", jsonDouble(0.9)));

    // extraneous field:
    assertThrows.accept(
        jsonObject(
            "XYZ", jsonDouble(0.12345),
            "min", jsonDouble(0.1),
            "max", jsonDouble(0.9)));
  }

  @Test
  public void testRbMapToJsonObject() {
    BiConsumer<RBMap<InstrumentId, Money>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            rbMapToJsonObject(map, v -> Strings.format("S%s", Long.toString(v.asLong())), v -> jsonString(v.toString(2))),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        rbMapOf(
            instrumentId(1), money(1.1),
            instrumentId(2), money(2.2)),
        // In reality, we store maps of PreciseValue by storing the numeric value.
        // In this test, we intentionally rely on Money#toString (with precision 2) to make the conversion less trivial.
        jsonObject(
            "S1", jsonString("$ 1.10"),
            "S2", jsonString("$ 2.20")));
    asserter.accept(
        emptyRBMap(),
        emptyJsonObject());
  }

  @Test
  public void testRBMapToJsonObject_mostGeneralOverloadWithMapEntryTransformer() {
    BiConsumer<RBMap<String, Integer>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            rbMapToJsonObject(
                map,
                (stringKey, intValue) -> pair(
                    Strings.format("%s%s_prop", stringKey, 70 + intValue),
                    jsonString(Strings.format("%s%s_val", stringKey, 80 + intValue)))),
            jsonObjectEpsilonMatcher(
                expectedJsonObject));

    asserter.accept(
        rbMapOf(
            "a", 1,
            "b", 2),
        jsonObject(
            "a71_prop", jsonString("a81_val"),
            "b72_prop", jsonString("b82_val")));

    asserter.accept(
        emptyRBMap(),
        emptyJsonObject());
  }

  @Test
  public void testOrderedRBMapToJsonObject() {
    BiConsumer<RBMap<InstrumentId, Money>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            orderedRBMapToJsonObject(
                map,
                v -> Strings.format("S%s", Long.toString(v.asLong())),
                v -> jsonString(v.toString(2)),
                InstrumentId::compareTo),
            orderedJsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        // These are 1 through 9, but constructed out of order. The semantics won't change, but it means that the
        // RBMap's internal storage is very unlikely to be such that the returned entries are in the order 1, 2, ... 9.
        rbMapOf(
            instrumentId(1), money(1.1),
            instrumentId(9), money(9.9),
            instrumentId(2), money(2.2),
            instrumentId(7), money(7.7),
            instrumentId(4), money(4.4),
            instrumentId(3), money(3.3),
            instrumentId(6), money(6.6),
            instrumentId(5), money(5.5),
            instrumentId(8), money(8.8)),
        // In reality, we store maps of PreciseValue by storing the numeric value.
        // In this test, we intentionally rely on Money#toString (with precision 2) to make the conversion less trivial.
        jsonObject(
            "S1", jsonString("$ 1.10"),
            "S2", jsonString("$ 2.20"),
            "S3", jsonString("$ 3.30"),
            "S4", jsonString("$ 4.40"),
            "S5", jsonString("$ 5.50"),
            "S6", jsonString("$ 6.60"),
            "S7", jsonString("$ 7.70"),
            "S8", jsonString("$ 8.80"),
            "S9", jsonString("$ 9.90")));
    asserter.accept(
        emptyRBMap(),
        emptyJsonObject());

  }

  @Test
  public void rbMapToJsonObject_serializationCreatesSameKey_throws() {
    RBMap<InstrumentId, Money> map = rbMapOf(
        instrumentId(1), money(1.1),
        instrumentId(2), money(2.2));
    assertIllegalArgumentException( () ->
        rbMapToJsonObject(map, v -> "fixed_value", v -> jsonString(v.toString())));
  }

  @Test
  public void testRbSetToJsonObject() {
    BiConsumer<RBSet<String>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            rbSetToJsonObject(map, v -> v.substring(0, 2), v -> jsonString(v.substring(2))),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        rbSetOf(
            "abcde",
            "fghij"),
        jsonObject(
            "ab", jsonString("cde"),
            "fg", jsonString("hij")));
    asserter.accept(
        emptyRBSet(),
        emptyJsonObject());
  }

  @Test
  public void rbSetToJsonObject_serializationCreatesSameKey_throws() {
    RBMap<InstrumentId, Money> doesNotThrow = rbMapOf(
        instrumentId(1), money(1.1),
        instrumentId(2), money(2.2));
    assertIllegalArgumentException( () ->
        rbSetToJsonObject(
            rbSetOf(
                "ZZcde",
                "ZZhij"),
            v -> v.substring(0, 2),
            v -> jsonString(v.substring(2))));
  }

  @Test
  public void testIidMapToJsonObject() {
    BiConsumer<IidMap<Money>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            iidMapToJsonObject(map, TICKER_MAP, v -> jsonString(v.toString(2))),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        iidMapOf(
            instrumentId(1), money(1.1),
            instrumentId(2), money(2.2)),
        // In reality, we store maps of PreciseValue by storing the numeric value.
        // In this test, we intentionally rely on Money#toString (with precision 2) to make the conversion less trivial.
        jsonObject(
            "S1", jsonString("$ 1.10"),
            "S2", jsonString("$ 2.20")));
    asserter.accept(
        emptyIidMap(),
        emptyJsonObject());
  }

  @Test
  public void testIidMapToJsonObject_biFunctionTransformation() {
    BiConsumer<IidMap<Money>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            iidMapToJsonObject(
                map,
                TICKER_MAP,
                (iid, v) -> jsonString(Strings.format("iid %s : %s", iid.asLong(), v.toString(2)))),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        iidMapOf(
            instrumentId(1), money(1.1),
            instrumentId(2), money(2.2)),
        // In reality, we store maps of PreciseValue by storing the numeric value.
        // In this test, we intentionally rely on Money#toString (with precision 2) to make the conversion less trivial.
        jsonObject(
            "S1", jsonString("iid 1 : $ 1.10"),
            "S2", jsonString("iid 2 : $ 2.20")));
    asserter.accept(
        emptyIidMap(),
        emptyJsonObject());
  }

  @Test
  public void testIidMapToFilteredJsonObject_biFunctionTransformation() {
    BiConsumer<IidMap<Money>, JsonObject> asserter = (map, expectedJsonObject) ->
        assertThat(
            iidMapToFilteredJsonObject(
                map,
                TICKER_MAP,
                (iid, v) -> jsonString(Strings.format("iid %s : %s", iid.asLong(), v.toString(2))),
                jsonElement -> jsonElement.getAsString().contains("iid 2")),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(
        iidMapOf(
            instrumentId(1), money(1.1),
            instrumentId(2), money(2.2)),
        // In reality, we store maps of PreciseValue by storing the numeric value.
        // In this test, we intentionally rely on Money#toString (with precision 2) to make the conversion less trivial.
        singletonJsonObject(
            // "S1" is filtered out
            "S2", jsonString("iid 2 : $ 2.20")));

    asserter.accept(
        emptyIidMap(),
        emptyJsonObject());
  }

  @Test
  public void testRBSetOfHasUniqueIdToJsonObject() {
    RBSetOfHasUniqueId<TestHasUniqueId> rbSetOfHasUniqueId2 = rbSetOfHasUniqueId(rbMapOf(
        uniqueId("id1"), testHasUniqueId(uniqueId("id1"), unitFraction(0.11)),
        uniqueId("id2"), testHasUniqueId(uniqueId("id2"), unitFraction(0.22))));

    assertThat(
        rbSetOfHasUniqueIdToJsonObject(
            rbSetOfHasUniqueId2,
            testHasUniqueId -> jsonObject(rbMapOf(
                "id",       jsonString(testHasUniqueId.getUniqueId().getStringId()),
                "fraction", jsonDouble(testHasUniqueId.getValue())))),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "id1", jsonObject(
                    "id",       jsonString("id1"),
                    "fraction", jsonDouble(0.11)),
                "id2", jsonObject(
                    "id",       jsonString("id2"),
                    "fraction", jsonDouble(0.22)))));
  }

  @Test
  public void testStreamOfHasUniqueIdToJsonObject() {
    assertThat(
        streamOfHasUniqueIdToJsonObject(
            Stream.of(
                testHasUniqueId(uniqueId("id1"), unitFraction(0.11)),
                testHasUniqueId(uniqueId("id2"), unitFraction(0.22))),
            testHasUniqueId -> jsonObject(
                "uniqueId", jsonString(testHasUniqueId.getUniqueId().getStringId()),
                "value",    jsonDouble(testHasUniqueId.getValue()))),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "id1", jsonObject(
                    "uniqueId", jsonString("id1"),
                    "value",    jsonDouble(0.11)),
                "id2", jsonObject(
                    "uniqueId", jsonString("id2"),
                    "value",    jsonDouble(0.22)))));
  }

  @Test
  public void testStreamToJsonObject() {
    BiConsumer<Stream<Pair<String, Integer>>, JsonObject> asserter = (stream, expectedJsonObject) ->
        assertThat(
            streamToJsonObject(
                stream,
                pair -> pair.getLeft() + "_",
                pair -> jsonString(pair.getLeft() + ":" + (700 + pair.getRight()))),
            jsonObjectEpsilonMatcher(
                expectedJsonObject));

    asserter.accept(
        Stream.of(
            pair("a", 1),
            pair("b", 2)),
        jsonObject(
            "a_", jsonString("a:701"),
            "b_", jsonString("b:702")));

    asserter.accept(
        Stream.empty(),
        emptyJsonObject());
  }

  @Test
  public void testJsonObjectToRBSetOfHasUniqueId() {
    assertThat(
        jsonObjectToRBSetOfHasUniqueId(
            jsonObject(
                "subAccount1", jsonString("subAccount1"),
                "subAccount2", jsonString("subAccount2")),
            jsonElement -> testHasUniqueId(uniqueId(jsonElement.getAsString()), unitFraction(0.123))),
        rbSetOfHasUniqueIdMatcher(rbSetOfHasUniqueId(rbMapOf(
                uniqueId("subAccount1"), testHasUniqueId(uniqueId("subAccount1"), unitFraction(0.123)),
                uniqueId("subAccount2"), testHasUniqueId(uniqueId("subAccount2"), unitFraction(0.123)))),
            f -> testHasUniqueIdMatcher(f)));
  }

  @Test
  public void testArrayIndexMappingToJsonArray() {
    SimpleArrayIndexMapping<String> simpleArrayIndexMapping = simpleArrayIndexMapping(ImmutableList.of("A", "B", "C"));
    assertThat(
        arrayIndexMappingToJsonArray(simpleArrayIndexMapping, v -> jsonString(v)),
        jsonArrayEpsilonMatcher(
            jsonArray(jsonString("A"), jsonString("B"), jsonString("C"))));
  }

  @Test
  public void testJsonObjectToRBSet() {
    BiConsumer<JsonObject, RBSet<String>> asserter = (jsonObject, expectedSet) ->
        assertThat(
            jsonObjectToRBSet(
                jsonObject,
                (key, value) -> Strings.format("%s_%s", key, value.getAsString())),
            rbSetEqualsMatcher(expectedSet));

    asserter.accept(
        jsonObject(
            "1", jsonString("AA"),
            "2", jsonString("BB")),
        rbSetOf("1_AA", "2_BB"));
    asserter.accept(
        emptyJsonObject(),
        emptyRBSet());
  }

  @Test
  public void testJsonObjectToStream() {
    BiConsumer<JsonObject, RBSet<String>> asserter = (jsonObject, expectedSet) ->
        assertThat(
            rbSet(jsonObjectToStream(
                jsonObject,
                (key, value) -> Strings.format("%s_%s", key, value.getAsString()))
                .collect(Collectors.toSet())),
            rbSetEqualsMatcher(expectedSet));

    asserter.accept(
        jsonObject(
            "abc", jsonString("123"),
            "def", jsonString("456")),
        rbSetOf("abc_123", "def_456"));
    asserter.accept(
        emptyJsonObject(),
        emptyRBSet());
  }

  @Test
  public void testJsonObjectToRBSet_rbSetValuesNotUnique_throws() {
    assertIllegalArgumentException( () -> jsonObjectToRBSet(
        jsonObject(
            "abc", jsonString("123"),
            "def", jsonString("456")),
        (key, value) -> "same value"));
  }

  @Test
  public void testJsonObjectToRBMap_overloadWithoutKey() {
    // CaseInsensitiveStringFilter is a dummy type; we need to use SOMETHING inside the UniqueId generic placeholder.
    BiConsumer<JsonObject, RBMap<InstrumentId, UniqueId<CaseInsensitiveStringFilter>>> asserter =
        (jsonObject, expectedMap) ->
            assertThat(
                jsonObjectToRBMap(
                    jsonObject,
                    key -> instrumentId(Long.parseLong(key)),
                    v -> uniqueId(v.getAsString())),
                rbMapMatcher(expectedMap, f -> uniqueIdMatcher(f)));

    asserter.accept(
        jsonObject(
            "1", jsonString("AA"),
            "2", jsonString("BB")),
        rbMapOf(
            instrumentId(1), uniqueId("AA"),
            instrumentId(2), uniqueId("BB")));
    asserter.accept(
        emptyJsonObject(),
        emptyRBMap());
  }

  @Test
  public void testJsonObjectToRBMap_overloadWithKey() {
    // CaseInsensitiveStringFilter is a dummy type; we need to use SOMETHING inside the UniqueId generic placeholder.
    BiConsumer<JsonObject, RBMap<InstrumentId, UniqueId<CaseInsensitiveStringFilter>>> asserter =
        (jsonObject, expectedMap) ->
            assertThat(
                jsonObjectToRBMap(
                    jsonObject,
                    key -> instrumentId(Long.parseLong(key)),
                    (key, v) -> uniqueId(key.asLong() + v.getAsString())),
                rbMapMatcher(expectedMap, f -> uniqueIdMatcher(f)));

    asserter.accept(
        jsonObject(
            "1", jsonString("AA"),
            "2", jsonString("BB")),
        rbMapOf(
            instrumentId(1), uniqueId("1AA"),
            instrumentId(2), uniqueId("2BB")));
    asserter.accept(
        emptyJsonObject(),
        emptyRBMap());
  }

  @Test
  public void testJsonObjectToRBMap_mostGeneralOverloadWithMapEntryTransformer() {
    BiConsumer<JsonObject, RBMap<String, String>> asserter = (jsonObject, expectedMap) ->
        assertThat(
            jsonObjectToRBMap(
                jsonObject,
                (stringKey, jsonElement) -> {
                  int intValue = jsonElement.getAsInt();
                  return pair(
                      String.join("", Collections.nCopies(intValue, stringKey)),
                      Strings.format("%s_%s", stringKey, intValue + 10));
                }),
            rbMapEqualityMatcher(
                expectedMap));

    asserter.accept(
        jsonObject(
            "p2", jsonInteger(2),
            "p3", jsonInteger(3)),
        rbMapOf(
            "p2p2", "p2_12",
            "p3p3p3", "p3_13"));

    asserter.accept(
        emptyJsonObject(),
        emptyRBMap());
  }

  @Test
  public void testJsonObjectToIidMap_overloadWithoutInstrumentId() {
    BiConsumer<JsonObject, IidMap<UniqueId<CaseInsensitiveStringFilter>>> asserter =
        (jsonObject, expectedMap) ->
            assertThat(
                jsonObjectToIidMap(
                    jsonObject,
                    TICKER_MAP,
                    v -> uniqueId(v.getAsString())),
                iidMapMatcher(expectedMap, f -> uniqueIdMatcher(f)));

    asserter.accept(
        jsonObject(
            "S1", jsonString("AA"),
            "S2", jsonString("BB")),
        iidMapOf(
            instrumentId(1), uniqueId("AA"),
            instrumentId(2), uniqueId("BB")));
    asserter.accept(
        emptyJsonObject(),
        emptyIidMap());
  }

  @Test
  public void testJsonObjectToIidMap_overloadWithInstrumentId() {
    BiConsumer<JsonObject, IidMap<String>> asserter = (jsonObject, expectedMap) ->
        assertThat(
            jsonObjectToIidMap(
                jsonObject,
                TICKER_MAP,
                (instrumentId, jsonString) -> Strings.format("%s %s", instrumentId.asLong(), jsonString.getAsString())),
            iidMapEqualityMatcher(expectedMap));

    asserter.accept(
        jsonObject(
            "S1", jsonString("AA"),
            "S2", jsonString("BB")),
        iidMapOf(
            instrumentId(1), "1 AA",
            instrumentId(2), "2 BB"));
    asserter.accept(
        emptyJsonObject(),
        emptyIidMap());
  }

  @Test
  public void test_ifHasJsonObjectProperty() {
    StringBuilder sb = new StringBuilder();
    JsonObject jsonObject = singletonJsonObject("a", singletonJsonObject("xyz", jsonString("123")));
    ifHasJsonObjectProperty(jsonObject, "b", v -> sb.append(v.getAsJsonPrimitive("xyz").getAsString()));
    assertEquals("", sb.toString());
    ifHasJsonObjectProperty(jsonObject, "a", v -> sb.append(v.getAsJsonPrimitive("xyz").getAsString()));
    assertEquals("123", sb.toString());
  }

  @Test
  public void testIfHasJsonPropertyElse() {
    StringBuilder sb = new StringBuilder();
    JsonObject jsonObject = singletonJsonObject("a", singletonJsonObject("xyz", jsonString("123")));
    ifHasJsonObjectPropertyElse(
        jsonObject,
        "b",
        v -> sb.append(v.getAsJsonPrimitive("xyz").getAsString()),
        () -> sb.append("missing_"));
    assertEquals("missing_", sb.toString());
    ifHasJsonObjectPropertyElse(
        jsonObject,
        "a",
        v -> sb.append(v.getAsJsonPrimitive("xyz").getAsString()),
        () -> sb.append("missing_"));
    assertEquals("missing_123", sb.toString());  // sb was 'missing_', then '123' was appended
  }

  @Test
  public void test_ifHasDoublePropertyElse() {
    Pointer<Double> pointer = uninitializedPointer();
    JsonObject jsonObject = jsonObject(
        "a", jsonDouble(1.1),
        "notANumber", jsonString("xyz"),
        "subObject", singletonJsonObject("subKey", jsonDouble(7.89)));

    ifHasDoublePropertyElse(jsonObject, "b", v -> pointer.set(v * 100), () -> pointer.set(7.7));
    assertEquals(7.7, pointer.getOrThrow(), 1e-8);
    ifHasDoublePropertyElse(jsonObject, "a", v -> pointer.set(v * 100), () -> pointer.set(7.7));
    assertEquals(doubleExplained(110, 1.1 * 100), pointer.getOrThrow(), 1e-8);

    assertIllegalArgumentException( () -> ifHasDoublePropertyElse(
        jsonObject,
        "notANumber",
        v -> pointer.set(DUMMY_DOUBLE),
        () -> pointer.set(DUMMY_DOUBLE)));
    assertIllegalArgumentException( () -> ifHasDoublePropertyElse(
        jsonObject,
        "subObject",
        v -> pointer.set(DUMMY_DOUBLE),
        () -> pointer.set(DUMMY_DOUBLE)));
  }

  @Test
  public void retainsOrderInPractice() {
    // This method is called 'in practice' because there is no guarantee.
    // Let's first create a map in relatively random order, which means it's very unlikely that iterating over its
    // entries will be in increasing order.
    // Quite frankly, although this test passes, it wasn't failing before we changed the rbMapToJsonObject code to
    // be explicit about ordering. However, it seems to fix some non-determinism elsewhere, so I'll keep the test.
    RBMap<String, Integer> map = rbMapOf(
        "_9", 9,
        "_1", 1,
        "_8", 8,
        "_2", 2,
        "_7", 7,
        "_3", 3,
        "_6", 6,
        "_4", 4,
        "_5", 5);
    assertNotEquals(
        ImmutableList.of("_1", "_2", "_3", "_4", "_5", "_6", "_7", "_8", "_9"),
        newArrayList(map.keySet()));

    JsonObject jsonObject = rbMapToJsonObject(
        map,
        key -> key.substring(1), // drop the leading underscore
        value -> jsonInteger(value * 10));

    assertEquals(
        ImmutableList.of("1", "2", "3", "4", "5", "6", "7", "8", "9"),
        newArrayList(jsonObject.keySet().iterator()));
  }

  @Test
  public void testFilterEntries() {
    BiConsumer<JsonObject, JsonObject> asserter = (input, expectedOutput) ->
        assertThat(
            filterEntries(input, (key, value) -> !(key.equals("badKey") || value.isJsonNull())),
            jsonObjectEpsilonMatcher(expectedOutput));
    asserter.accept(emptyJsonObject(), emptyJsonObject());
    asserter.accept(
        jsonObject(
            "key1", jsonString("value1"),
            "key2", JsonNull.INSTANCE,
            "badKey", jsonString("value3")),
        singletonJsonObject(
            "key1", jsonString("value1")));
  }

  @Test
  public void testFilterKeys() {
    BiConsumer<JsonObject, JsonObject> asserter = (input, expectedOutput) ->
        assertThat(
            filterKeys(input, key -> !key.equals("badKey")),
            jsonObjectEpsilonMatcher(expectedOutput));
    asserter.accept(emptyJsonObject(), emptyJsonObject());
    asserter.accept(
        jsonObject(
            "key1", jsonString("value1"),
            "key2", JsonNull.INSTANCE,
            "badKey", jsonString("value3")),
        jsonObject(
            "key1", jsonString("value1"),
            "key2", JsonNull.INSTANCE));
  }

  @Test
  public void testForEachJsonObjectEntry() {
    MutableRBSet<String> items = newMutableRBSet();

    forEachJsonObjectEntry(
        emptyJsonObject(),
        (property, value) -> items.addAssumingAbsent(Strings.format("%s_%s", property, value)));
    assertTrue(items.isEmpty());

    forEachJsonObjectEntry(
        jsonObject(
            "key1", jsonString("value1"),
            "key2", jsonInteger(22)),
        (property, value) -> items.addAssumingAbsent(Strings.format("%s_%s", property, value.getAsString())));

    assertThat(
        newRBSet(items),
        rbSetEqualsMatcher(rbSetOf(
            "key1_value1",
            "key2_22")));
  }

}
