package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rb.nonbiz.collections.IidMap;
import com.rb.nonbiz.collections.Pair;
import com.rb.nonbiz.collections.RBMap;
import com.rb.nonbiz.collections.RBSet;
import com.rb.nonbiz.text.Strings;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rb.biz.types.asset.InstrumentId.instrumentId;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.emptyIidMap;
import static com.rb.nonbiz.collections.IidMapSimpleConstructors.iidMapOf;
import static com.rb.nonbiz.collections.IidMapTest.iidMapEqualityMatcher;
import static com.rb.nonbiz.collections.Pair.pair;
import static com.rb.nonbiz.collections.PairTest.pairEqualityMatcher;
import static com.rb.nonbiz.collections.RBMapSimpleConstructors.rbMapOf;
import static com.rb.nonbiz.collections.RBSet.emptyRBSet;
import static com.rb.nonbiz.collections.RBSet.rbSetOf;
import static com.rb.nonbiz.json.RBGson.jsonBoolean;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.*;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonLongOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectGetters.getJsonStringOrThrow;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.doubleArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBArrayMatchers.intArrayMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.orderedListMatcher;
import static com.rb.nonbiz.testmatchers.RBCollectionMatchers.rbSetEqualsMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBOptionalMatchers.nonEmptyOptionalMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalNonEmpty;
import static com.rb.nonbiz.types.Correlation.correlation;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static com.rb.nonbiz.types.UnitFraction.UNIT_FRACTION_0;
import static com.rb.nonbiz.types.UnitFraction.unitFraction;
import static java.util.Collections.emptyList;
import static java.util.Collections.nCopies;
import static java.util.Map.Entry.comparingByKey;
import static java.util.Map.Entry.comparingByValue;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBJsonArraysTest {

  @Test
  public void test_newJsonArrayWithExpectedSize() {
    Function<Integer, JsonArray> maker = expectedSize -> {
      JsonArray jsonArray = newJsonArrayWithExpectedSize(expectedSize);
      jsonArray.add("abc");
      jsonArray.add(123);
      return jsonArray;
    };

    // the expected size alters the capacity but not the contents
    rbSetOf(0, 1, 2, 3)
        .forEach(expectedSize ->
            assertThat(
                maker.apply(expectedSize),
                jsonArrayExactMatcher(jsonArray(jsonString("abc"), jsonInteger(123)))));
  }

  @Test
  public void testConstructorFromCollection() {
    assertThat(
        jsonArray(ImmutableList.of(jsonString("abc"), jsonDouble(1.23), jsonBoolean(false))),
        jsonArrayExactMatcher(jsonArray(jsonString("abc"), jsonDouble(1.23), jsonBoolean(false)))
    );
  }

  @Test
  public void testSpecializedConstructors() {
    // strings
    assertThat(
        singletonJsonStringArray("abc"),
        jsonArrayExactMatcher(singletonJsonArray(jsonString("abc"))));
    assertThat(
        jsonStringArray("abc", "def", "ghi"),
        jsonArrayExactMatcher(jsonArray(jsonString("abc"), jsonString("def"), jsonString("ghi"))));

    // doubles
    assertThat(
        singletonJsonDoubleArray(1.23),
        jsonArrayExactMatcher(singletonJsonArray(jsonDouble(1.23))));
    assertThat(
        jsonDoubleArray(1.23, 4.56, 7.89),
        jsonArrayExactMatcher(jsonArray(jsonDouble(1.23), jsonDouble(4.56), jsonDouble(7.89))));

    // booleans
    assertThat(
        singletonJsonBooleanArray(true),
        jsonArrayExactMatcher(singletonJsonArray(jsonBoolean(true))));
    assertThat(
        jsonBooleanArray(true, false, true),
        jsonArrayExactMatcher(jsonArray(jsonBoolean(true), jsonBoolean(false), jsonBoolean(true))));

    // integers
    assertThat(
        singletonJsonIntegerArray(123),
        jsonArrayExactMatcher(singletonJsonArray(jsonInteger(123))));
    assertThat(
        jsonIntegerArray(123, 456, 789),
        jsonArrayExactMatcher(jsonArray(jsonInteger(123), jsonInteger(456), jsonInteger(789))));

    // longs
    assertThat(
        singletonJsonLongArray(123L),
        jsonArrayExactMatcher(singletonJsonArray(jsonLong(123L))));
    assertThat(
        jsonLongArray(123L, 456L, 789L),
        jsonArrayExactMatcher(jsonArray(jsonLong(123L), jsonLong(456L), jsonLong(789L))));

    // general JSON elements
    assertThat(
        singletonJsonElementArray(jsonDouble(3.14)),
        jsonArrayExactMatcher(singletonJsonArray(jsonDouble(3.14))));
    assertThat(
        jsonElementArray(jsonDouble(3.14), jsonString("pi"), jsonBoolean(true)),
        jsonArrayExactMatcher(jsonArray(jsonDouble(3.14), jsonString("pi"), jsonBoolean(true))));
  }

  @Test
  public void testListToJsonArray_and_streamToJsonArray() {
    BiConsumer<List<Pair<Integer, String>>, JsonArray> asserter = (javaList, jsonArray) -> {
      assertThat(
          listToJsonArray(
              javaList,
              pair -> jsonString(Strings.format("%s %s", pair.getLeft(), pair.getRight()))),
          jsonArrayExactMatcher(jsonArray));
      assertThat(
          streamToJsonArray(
              javaList.size(),
              javaList.stream(),
              pair -> jsonString(Strings.format("%s %s", pair.getLeft(), pair.getRight()))),
          jsonArrayExactMatcher(jsonArray));
      assertThat(
          jsonArrayToStream(jsonArray, v -> pair(
              Integer.parseInt(v.getAsString().substring(0, 1)), // e.g.   1 in "1 a"
              v.getAsString().substring(2, 3)))                  // e.g. "a" in "1 a"
              .collect(Collectors.toList()),
          orderedListMatcher(javaList, f -> pairEqualityMatcher(f)));
    };
    asserter.accept(
        ImmutableList.of(
            pair(1, "a"),
            pair(2, "b")),
        jsonArray(
            jsonString("1 a"),
            jsonString("2 b")));
    asserter.accept(
        emptyList(),
        emptyJsonArray());
  }

  @Test
  public void testListOfOptionalsToJsonArray() {
    BiConsumer<List<Optional<String>>, JsonArray> asserter = (javaList, expectedJsonArray) ->
      assertThat(
          listOfOptionalsToJsonArray(
              javaList,
              string -> jsonString(string)),
          jsonArrayExactMatcher(expectedJsonArray));

    asserter.accept(
        emptyList(),
        emptyJsonArray());

    // Optional.empty()'s are removed
    asserter.accept(
        ImmutableList.of(
            Optional.empty(),
            Optional.empty()),
        emptyJsonArray());

    asserter.accept(
        ImmutableList.of(
            Optional.of("a"),
            Optional.of("b")),
        jsonArray(jsonString("a"), jsonString("b")));

    asserter.accept(
        ImmutableList.of(
            Optional.of("a"),
            Optional.empty(),
            Optional.of("b")),
        jsonArray(jsonString("a"), jsonString("b")));

    // duplicates are allowed
    asserter.accept(
        ImmutableList.of(
            Optional.empty(),
            Optional.of("a"),
            Optional.empty(),
            Optional.of("a")),
        jsonArray(jsonString("a"), jsonString("a")));
  }

  @Test
  public void testJsonArrayToList() {
    BiConsumer<JsonArray, List<Pair<Integer, String>>> asserter = (jsonArray, javaList) -> assertThat(
        jsonArrayToList(
            jsonArray,
            jsonElement -> pair(
                Integer.parseInt(jsonElement.getAsString().substring(0, 1)),
                jsonElement.getAsString().substring(2, 3))),
        orderedListMatcher(
            javaList, f -> pairEqualityMatcher(f)));
    asserter.accept(
        jsonArray(
            jsonString("1 a"),
            jsonString("2 b")),
        ImmutableList.of(
            pair(1, "a"),
            pair(2, "b")));
    asserter.accept(
        emptyJsonArray(),
        emptyList());
  }

  @Test
  public void testJsonArrayToDoubleArray() {
    assertThat(
        jsonArrayToDoubleArray(
            jsonArray(jsonDouble(1.23), jsonDouble(4.56), jsonDouble(7.89)),
            v -> v.getAsDouble()),
        doubleArrayMatcher(
            new double[] { 1.23, 4.56, 7.89 },
            DEFAULT_EPSILON_1e_8));
  }

  @Test
  public void testJsonArrayToIntArray() {
    assertThat(
        jsonArrayToIntArray(
            jsonArray(jsonInteger(123), jsonInteger(456), jsonInteger(789)),
            v -> v.getAsInt()),
        intArrayMatcher(
            new int[] { 123, 456, 789 }));
  }

  @Test
  public void testJsonArrayToRBSet_and_rbSetToJsonArray() {
    BiConsumer<JsonArray, RBSet<String>> asserter = (jsonArray, rbSet) -> {
      assertThat(
          jsonArrayToRBSet(
              jsonArray,
              jsonElement -> jsonElement.getAsString().substring(2, 3)),
          rbSetEqualsMatcher(
              rbSet));
      assertThat(
          rbSetToJsonArray(
              rbSet,
              v -> jsonString(Strings.format("_ %s", v))),
          jsonArrayExactMatcher(jsonArray));
    };
    asserter.accept(
        jsonArray(
            jsonString("_ a"),
            jsonString("_ b")),
        rbSetOf("a", "b"));
    asserter.accept(
        emptyJsonArray(),
        emptyRBSet());
  }

  @Test
  public void rbSetToJsonArray_sortedVersion() {
    BiConsumer<JsonArray, RBSet<String>> asserter = (jsonArray, rbSet) -> {
      assertThat(
          jsonArrayToRBSet(
              jsonArray,
              jsonElement -> jsonElement.getAsString().substring(2, 3)),
          rbSetEqualsMatcher(
              rbSet));
      assertThat(
          rbSetToJsonArray(
              rbSet,
              String::compareTo,
              v -> jsonString(Strings.format("_ %s", v))),
          jsonArrayExactMatcher(jsonArray));
    };
    asserter.accept(
        jsonArray(
            jsonString("_ 1"),
            jsonString("_ 2"),
            jsonString("_ 3"),
            jsonString("_ 4"),
            jsonString("_ 5"),
            jsonString("_ 6"),
            jsonString("_ 7"),
            jsonString("_ 8"),
            jsonString("_ 9")),
        // I'm rearranging the strings a bit so they're not sorted, so that it's very unlikely that the array
        // ends up in sorted order due to iterating over the rbset in an unspecifiedorder.
        rbSetOf("1", "3", "5", "7", "9", "2", "4", "6", "8"));
    asserter.accept(
        emptyJsonArray(),
        emptyRBSet());
  }

  @Test
  public void test_rbMapToJsonArray() {
    RBMap<String, Integer> rbMap = rbMapOf(
        "a", 4,
        "b", 3,
        "c", 2,
        "d", 1);

    BiConsumer<Comparator<Entry<String, Integer>>, JsonArray> asserter = (comparator, expectedArray) ->
        assertThat(
            rbMapToJsonArray(
                rbMap,
                comparator,
                (key, value) -> jsonString(String.format("%s_%s", key, value))),
            jsonArrayExactMatcher(
                expectedArray));

    // sort by key
    asserter.accept(
        comparingByKey(),
        jsonArray(
            jsonString("a_4"),
            jsonString("b_3"),
            jsonString("c_2"),
            jsonString("d_1")));

    // sort by value
    asserter.accept(
        comparingByValue(),
        jsonArray(
            jsonString("d_1"),
            jsonString("c_2"),
            jsonString("b_3"),
            jsonString("a_4")));
  }

  @Test
  public void test_ifHasJsonArrayProperty() {
    StringBuilder sb = new StringBuilder();
    JsonObject jsonObject = singletonJsonObject("a", jsonStringArray("111", "222"));
    Consumer<JsonArray> appender = jsonArray ->
        sb.append(Strings.format("%s_%s", jsonArray.get(0).getAsString(), jsonArray.get(1).getAsString()));
    ifHasJsonArrayProperty(jsonObject, "b", appender);
    assertEquals("", sb.toString());
    ifHasJsonArrayProperty(jsonObject, "a", appender);
    assertEquals("111_222", sb.toString());
  }

  @Test
  public void testToJsonArrayIfNotAllZeros_returnsEmptyWhenAllAreZero() {
    for (int n = 1; n <= 3; n++) {
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, 0L)));
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, 0)));
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, 0.0)));
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, BigDecimal.ZERO)));
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, UNIT_FRACTION_0))); // PreciseValue
      assertOptionalEmpty(toJsonArrayIfNotAllZeros(nCopies(n, correlation(0)))); // ImpreciseValue

      assertOptionalNonEmpty(toJsonArrayIfNotAllZeros(nCopies(n, "0"))); // Strings not converted to numbers
      assertOptionalNonEmpty(toJsonArrayIfNotAllZeros(nCopies(n, "0.00")));
    }
  }

  @Test
  public void testToJsonArrayIfNotAllZeros_returnsNonEmptyWhenAtLeastOneIsNonZero() {
    assertSingle(ImmutableList.of(0l,  100l),  jsonArray(jsonInteger(0), jsonInteger(100)));
    assertSingle(ImmutableList.of(0,   100),   jsonArray(jsonInteger(0), jsonInteger(100)));
    assertSingle(ImmutableList.of(0.0, 100.0), jsonArray(jsonDouble(0),  jsonDouble(100.0)));
    assertSingle(
        ImmutableList.of(BigDecimal.ZERO, BigDecimal.valueOf(0.2)),
        jsonArray(jsonDouble(0), jsonDouble(0.2)));
    assertSingle(
        ImmutableList.of(UNIT_FRACTION_0, unitFraction(0.2)),
        jsonArray(jsonDouble(0), jsonDouble(0.2))); // PreciseValue
    assertSingle(
        ImmutableList.of(correlation(0), correlation(0.2)),
        jsonArray(jsonDouble(0), jsonDouble(0.2))); // ImpreciseValue
  }

  @Test
  public void testJsonArrayOfObjectsToIidMap() {
    BiConsumer<JsonArray, IidMap<String>> asserter = (jsonArray, expectedIidMap) -> assertThat(
        jsonArrayOfObjectsToIidMap(
            jsonArray,
            jsonObject -> instrumentId(getJsonLongOrThrow(jsonObject, "a")),
            jsonObject -> getJsonStringOrThrow(jsonObject, "b")),
        iidMapEqualityMatcher(expectedIidMap));

    asserter.accept(
        emptyJsonArray(),
        emptyIidMap());

    asserter.accept(
        jsonArray(
            jsonObject(
                "a", jsonLong(11),
                "b", jsonString("item1"),
                "c", jsonString("ignored 1")),
            jsonObject(
                "a", jsonLong(22),
                "b", jsonString("item2"),
                "c", jsonString("ignored 2"))),
        iidMapOf(
            instrumentId(11), "item1",
            instrumentId(22), "item2"));
  }

  // We'd normally create a lambda inside a test function and use that, but we can't use generics with lambdas I think.
  private <T> void assertSingle(List<T> list, JsonArray jsonArray) {
    assertThat(
        toJsonArrayIfNotAllZeros(list),
        nonEmptyOptionalMatcher(jsonArrayExactMatcher(jsonArray)));
  }

  // 'exact' means that numbers are not subject to an epsilon equality.
  public static TypeSafeMatcher<JsonArray> jsonArrayExactMatcher(JsonArray expected) {
    // Typically we don't use #equals, since we avoid implementing it.
    // However, JsonObject is a 3rd party class, and it does implement equals, so we might as well use it.
    return makeMatcher(expected, actual ->
        expected.equals(actual));
  }

}
