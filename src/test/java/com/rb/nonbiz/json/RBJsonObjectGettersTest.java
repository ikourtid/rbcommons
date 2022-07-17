package com.rb.nonbiz.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.rb.nonbiz.testutils.TestEnumXYZ;
import com.rb.nonbiz.text.Strings;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.rb.biz.jsonapi.JsonSerializedEnumStringMapImpl.jsonSerializedEnumStringMap;
import static com.rb.biz.types.Price.price;
import static com.rb.nonbiz.json.RBGson.jsonBoolean;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArraysTest.jsonArrayExactMatcher;
import static com.rb.nonbiz.json.RBJsonObjectGetters.*;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalDoubleAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalDoubleEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalIntEquals;
import static com.rb.nonbiz.testutils.Asserters.assertThrowsAnyException;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_PRICE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBJsonObjectGettersTest {

  @Test
  public void test_getJsonElementOrThrow() {
    JsonObject jsonObject = jsonObject(
        "n",    jsonInteger(10),
        "x",    jsonDouble(1.23),
        "text", jsonString("abc"));

    assertEquals(
        10,
        getJsonElementOrThrow(jsonObject, "n").getAsInt());
    assertEquals(
        1.23,
        getJsonElementOrThrow(jsonObject, "x").getAsDouble());
    assertEquals(
        "abc",
        getJsonElementOrThrow(jsonObject, "text").getAsString());

    assertIllegalArgumentException( () -> getJsonElementOrThrow(jsonObject, "missingProperty"));
  }

  @Test
  public void test_getOptionalJsonElement() {
    JsonObject jsonObject = jsonObject(
        "x", jsonDouble(1.23),
        "y", jsonString("abc"));
    assertOptionalEmpty(getOptionalJsonElement(jsonObject, ""));
    assertOptionalEmpty(getOptionalJsonElement(jsonObject, "z"));
    assertOptionalEquals(
        jsonDouble(1.23),
        getOptionalJsonElement(jsonObject, "x"));
    assertOptionalEquals(
        jsonString("abc"),
        getOptionalJsonElement(jsonObject, "y"));
  }

  @Test
  public void test_getOptionalJsonPrimitive() {
    JsonObject jsonObject = jsonObject(
        "x", jsonDouble(1.23),
        "y", jsonString("abc"),
        "obj", jsonObject(
            "key1", jsonBoolean(true),
            "key2", jsonInteger(3)));
    assertOptionalEmpty(getOptionalJsonPrimitive(jsonObject, ""));
    assertOptionalEmpty(getOptionalJsonPrimitive(jsonObject, "z"));
    assertOptionalEquals(
        jsonDouble(1.23),
        getOptionalJsonPrimitive(jsonObject, "x"));
    assertOptionalEquals(
        jsonString("abc"),
        getOptionalJsonPrimitive(jsonObject, "y"));

    // can't use this to retrieve a JSON sub-object; JsonPrimitives only
    assertThrowsAnyException( () -> getOptionalJsonPrimitive(jsonObject, "obj"));
  }

  @Test
  public void test_getOptionalJsonObject() {
    JsonObject jsonObject = jsonObject(
        "x", jsonDouble(1.23),
        "y", jsonString("abc"),
        "obj", jsonObject(
            "key1", jsonBoolean(true),
            "key2", jsonInteger(3)));
    assertOptionalEmpty(getOptionalJsonObject(jsonObject, ""));
    assertOptionalEmpty(getOptionalJsonObject(jsonObject, "z"));
    assertThat(
        getOptionalJsonObject(jsonObject, "obj").get(),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "key1", jsonBoolean(true),
                "key2", jsonInteger(3))));

    // can't use this to retrieve a JsonPrimitive
    assertThrowsAnyException( () -> getOptionalJsonObject(jsonObject, "x"));
    assertThrowsAnyException( () -> getOptionalJsonObject(jsonObject, "y"));
  }

  @Test
  public void testGetOptionalJsonSubObject() {
    JsonObject jsonObject = jsonObject(
        "notSubobject", jsonDouble(1.23),
        "subObject", singletonJsonObject(
            "subObjKey", jsonInteger(111)));

    Function<String, Optional<String>> converter = key ->
        getOptionalJsonSubObject(jsonObject, key, jsonSubObject -> String.format("%s_%s",
            getOnlyElement(jsonSubObject.keySet()),
            jsonSubObject.get(getOnlyElement(jsonSubObject.keySet()))));
    assertOptionalEmpty(converter.apply("unknown_key"));
    assertIllegalArgumentException( () -> converter.apply("notSubobject"));
    assertOptionalEquals("subObjKey_111", converter.apply("subObject"));
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a string.
   * If missing, or present but with a value of JsonNull, return empty optional.
   * If present, but not a string, throw an exception.
   * Otherwise, return it as a non-empty optional String.
   */
  @Test
  public void test_getOptionalJsonString() {
    JsonObject jsonObject = jsonObject(
        "x", jsonDouble(1.23),
        "y", jsonString("abc"),
        "z", JsonNull.INSTANCE);
    assertOptionalEmpty(getOptionalJsonString(jsonObject, ""));
    assertOptionalEmpty(getOptionalJsonString(jsonObject, "z"));
    assertOptionalEmpty(getOptionalJsonString(jsonObject, "foo"));
    assertIllegalArgumentException( () -> getOptionalJsonString(jsonObject, "x")); // not a string
    assertOptionalEquals("abc", getOptionalJsonString(jsonObject, "y"));
  }

  @Test
  public void test_getOptionalJsonDouble() {
    JsonObject jsonObject = jsonObject(
        "y", jsonString("abc"),
        "z", JsonNull.INSTANCE,
        "w1", jsonDouble(1.1),
        "w2", jsonDouble(22));
    assertOptionalDoubleEmpty(getOptionalJsonDouble(jsonObject, ""));
    assertOptionalDoubleEmpty(getOptionalJsonDouble(jsonObject, "z"));
    assertOptionalDoubleEmpty(getOptionalJsonDouble(jsonObject, "foo"));
    assertIllegalArgumentException( () -> getOptionalJsonDouble(jsonObject, "y")); // not a double; it's a string
    assertOptionalDoubleAlmostEquals(1.1, getOptionalJsonDouble(jsonObject, "w1"), 1e-8);
    assertOptionalDoubleAlmostEquals(22,  getOptionalJsonDouble(jsonObject, "w2"), 1e-8); // not a double, but it's OK
  }

  @Test
  public void test_getOptionalJsonInt() {
    JsonObject jsonObject = jsonObject(
        "x", jsonDouble(1.23),
        "y", jsonString("abc"),
        "z", JsonNull.INSTANCE,
        "w1", jsonDouble(11),
        "w2", jsonDouble(22 - 1e-13),
        "w3", jsonDouble(33 + 1e-13),
        "w4", jsonDouble(33.0001),
        "w5", jsonDouble(43.9999));
    assertOptionalIntEmpty(getOptionalJsonInt(jsonObject, ""));
    assertOptionalIntEmpty(getOptionalJsonInt(jsonObject, "z"));
    assertOptionalIntEmpty(getOptionalJsonInt(jsonObject, "foo"));
    assertIllegalArgumentException( () -> getOptionalJsonInt(jsonObject, "x")); // not an int; it's a non-round number
    assertIllegalArgumentException( () -> getOptionalJsonInt(jsonObject, "y")); // not an int; it's a string
    assertIllegalArgumentException( () -> getOptionalJsonInt(jsonObject, "w4")); // not an int; it's a non-round number
    assertIllegalArgumentException( () -> getOptionalJsonInt(jsonObject, "w5")); // not an int; it's a non-round number
    assertOptionalIntEquals(11, getOptionalJsonInt(jsonObject, "w1"));
    assertOptionalIntEquals(22, getOptionalJsonInt(jsonObject, "w2"));
    assertOptionalIntEquals(33, getOptionalJsonInt(jsonObject, "w3"));
  }

  @Test
  public void test_getEnumFromJsonOrThrow() {
    Function<String, TestEnumXYZ> getter = property -> getEnumFromJsonOrThrow(
        jsonObject(
            "hasValueX", jsonString("value_x"),
            "hasValueY", jsonString("value_y"),
            "hasValueZ", jsonString("value_z")),
        property,
        jsonSerializedEnumStringMap(
            TestEnumXYZ.class,
            TestEnumXYZ.X, "value_x",
            TestEnumXYZ.Y, "value_y",
            TestEnumXYZ.Z, "garbage"));

    assertEquals(TestEnumXYZ.X, getter.apply("hasValueX"));
    assertEquals(TestEnumXYZ.Y, getter.apply("hasValueY"));
    assertIllegalArgumentException( () -> getter.apply("hasValueZ"));
    assertIllegalArgumentException( () -> getter.apply("missing property"));
  }

  @Test
  public void test_getEnumFromJsonOrDefault() {
    Function<String, TestEnumXYZ> getter = property -> getEnumFromJsonOrDefault(
        jsonObject(
            "hasValueX", jsonString("value_x"),
            "hasValueY", jsonString("value_y"),
            "hasValueZ", jsonString("value_z")),
        property,
        jsonSerializedEnumStringMap(
            TestEnumXYZ.class,
            TestEnumXYZ.X, "value_x",
            TestEnumXYZ.Y, "value_y",
            TestEnumXYZ.Z, "garbage"),
        TestEnumXYZ.Y);

    assertEquals(TestEnumXYZ.X, getter.apply("hasValueX"));
    assertEquals(TestEnumXYZ.Y, getter.apply("hasValueY"));
    assertIllegalArgumentException( () -> getter.apply("hasValueZ"));
    assertEquals(TestEnumXYZ.Y, getter.apply("missing property takes default value of Y"));
  }

  @Test
  public void test_getJsonNumberOrThrow() {
    JsonObject jsonObject = jsonObject(
        "notANumber", jsonString("xyz"),
        "nLong",      jsonLong(123_456_789_000L),
        "n",          jsonInteger(10),
        "x",          jsonDouble(1.23),
        "y",          jsonDouble(123),
        "z",          jsonDouble(-1.23456e5));

    // "n" : 10 can be converted to int, long, BigDecimal, or double
    assertEquals(
        10,
        getJsonIntOrThrow(jsonObject, "n"));
    assertEquals(
        10,
        getJsonLongOrThrow(jsonObject, "n"));
    assertEquals(
        BigDecimal.valueOf(10),
        getJsonBigDecimalOrThrow(jsonObject, "n"));
    assertEquals(
        10.0,
        getJsonDoubleOrThrow(jsonObject, "n"));
    assertEquals(
        0.0123,
        getJsonDoubleFromPercentageOrThrow(jsonObject, "x"));
    assertEquals(
        BigDecimal.valueOf(0.0123),
        getJsonBigDecimalFromPercentageOrThrow(jsonObject, "x"));
    assertEquals(
        0.0123,
        getJsonDoubleFromPercentageOrDefault(jsonObject, "x", 7.89));
    assertEquals(
        7.89,
        getJsonDoubleFromPercentageOrDefault(jsonObject, "MISSING_KEY", 7.89));

    // "nLong" : 123_456_789_000L can be converted to long, BigDecimal, or double
    assertEquals(
        123_456_789_000L,
        getJsonLongOrThrow(jsonObject, "nLong"));
    assertEquals(
        BigDecimal.valueOf(123_456_789_000L),
        getJsonBigDecimalOrThrow(jsonObject, "nLong"));
    assertEquals(
        123_456_789_000.0,
        getJsonDoubleOrThrow(jsonObject, "nLong"));

    // "x" : 1.23 can be converted to a BigDecimal or double
    assertEquals(
        1.23,
        getJsonDoubleOrThrow(jsonObject, "x"));
    assertEquals(
        BigDecimal.valueOf(1.23),
        getJsonBigDecimalOrThrow(jsonObject, "x"));
    assertEquals(
        1.23,
        getJsonNumberElementOrThrow(jsonObject, "x").getAsDouble());

    // y is an integer stored as a double
    assertEquals(
        123,
        getJsonIntOrThrow(jsonObject, "y"));

    // z is an integer stored as a double
    assertEquals(
        -123456,
        getJsonIntOrThrow(jsonObject, "z"));

    // property "notANumber" maps to a string
    assertIllegalArgumentException( () -> getJsonIntOrThrow(          jsonObject, "notANumber"));
    assertIllegalArgumentException( () -> getJsonLongOrThrow(         jsonObject, "notANumber"));
    assertIllegalArgumentException( () -> getJsonDoubleOrThrow(       jsonObject, "notANumber"));
    assertIllegalArgumentException( () -> getJsonBigDecimalOrThrow(   jsonObject, "notANumber"));
    assertIllegalArgumentException( () -> getJsonNumberElementOrThrow(jsonObject, "notANumber"));

    // property "missingProperty" is not present
    assertIllegalArgumentException( () -> getJsonIntOrThrow(          jsonObject, "missingProperty"));
    assertIllegalArgumentException( () -> getJsonLongOrThrow(         jsonObject, "missingProperty"));
    assertIllegalArgumentException( () -> getJsonDoubleOrThrow(       jsonObject, "missingProperty"));
    assertIllegalArgumentException( () -> getJsonBigDecimalOrThrow(   jsonObject, "missingProperty"));
    assertIllegalArgumentException( () -> getJsonNumberElementOrThrow(jsonObject, "missingProperty"));

    // "nLong" is not an int
    assertIllegalArgumentException( () -> getJsonIntOrThrow(jsonObject, "nLong"));
    // "x" : 1.23 is not an integer
    assertIllegalArgumentException( () -> getJsonIntOrThrow( jsonObject, "x"));
    assertIllegalArgumentException( () -> getJsonLongOrThrow(jsonObject, "x"));
  }

  @Test
  public void test_getJsonAsNumber_numberAsString_throws() {
    JsonObject jsonObject = jsonObject(
        "n",          jsonInteger(123),
        "nAsString",  jsonString("123"),
        "x",          jsonDouble( 123.456),
        "xAsString",  jsonString("123.456"),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    int    doesNotThrowInt    = getJsonIntOrThrow(   jsonObject, "n");
    double doesNotThrowDouble = getJsonDoubleOrThrow(jsonObject, "x");

    assertIllegalArgumentException( () -> getJsonIntOrThrow(   jsonObject, "numberAsString"));
    assertIllegalArgumentException( () -> getJsonLongOrThrow(  jsonObject, "numberAsString"));
    assertIllegalArgumentException( () -> getJsonDoubleOrThrow(jsonObject, "floatAsString"));
    assertIllegalArgumentException( () -> getJsonDoubleOrThrow(jsonObject, "subObject"));
  }

  @Test
  public void test_getJsonStringOrThrow() {
    JsonObject jsonObject = jsonObject(
        "notAString", jsonDouble(1.23),
        "abc",        jsonString("ABC"),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    assertEquals(
        "ABC",
        getJsonStringOrThrow(jsonObject, "abc"));

    // property "notAString" maps to a double
    assertIllegalArgumentException( () -> getJsonStringOrThrow(jsonObject, "notAString"));
    // property "missingProperty" is not present
    assertIllegalArgumentException( () -> getJsonStringOrThrow(jsonObject, "missingProperty"));
    // property "subObject" is not a primitive
    assertIllegalArgumentException( () -> getJsonStringOrThrow(jsonObject, "subObject"));
  }

  @Test
  public void test_getJsonBooleanOrThrow() {
    JsonObject jsonObject = jsonObject(
        "notABool",    jsonDouble(1.23),
        "aTrueBool",   jsonBoolean(true),
        "aFalseBool",  jsonBoolean(false),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    assertTrue( getJsonBooleanOrThrow(jsonObject, "aTrueBool" ));
    assertFalse(getJsonBooleanOrThrow(jsonObject, "aFalseBool"));

    // property "notABool" maps to a double
    assertIllegalArgumentException( () -> getJsonBooleanOrThrow(jsonObject, "notABool"));
    // property "missingProperty" is not present
    assertIllegalArgumentException( () -> getJsonBooleanOrThrow(jsonObject, "missingProperty"));
    // property "subObject" is not a primitive
    assertIllegalArgumentException( () -> getJsonBooleanOrThrow(jsonObject, "subObject"));
  }

  @Test
  public void test_getJsonDateOrThrow() {
    JsonObject jsonObject = jsonObject(
        "date", jsonString("2014-04-04"),
        "badDateFormat1", jsonString("20140404"),
        "badDateFormat2", jsonString("April 4, 2014"),
        "badDateFormat3", jsonString("04/04/2014"),
        "invalidDate",    jsonString("2014-02-31"),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    assertEquals(LocalDate.of(2014, 4, 4),  getJsonDateOrThrow(jsonObject, "date"));

    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "badDateFormat1"));
    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "badDateFormat2"));
    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "badDateFormat3"));
    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "invalidDate"));
    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "missingProperty"));
    assertIllegalArgumentException( () -> getJsonDateOrThrow(jsonObject, "subObject"));
  }

  @Test
  public void test_getJsonObjectOrThrow() {
    JsonObject subObject = jsonObject(
        "n",    jsonInteger(10),
        "x",    jsonDouble(1.23),
        "text", jsonString("abc"));
    JsonObject jsonObject = jsonObject(
        "anObject", subObject,
        "aDouble",  jsonDouble(4.56),
        "aBool",    jsonBoolean(true),
        "anArray",  jsonArray(jsonString("abc"), jsonString("def")));

    assertThat(
        getJsonObjectOrThrow(jsonObject, "anObject"),
        jsonObjectEpsilonMatcher(subObject));

    assertIllegalArgumentException( () -> getJsonObjectOrThrow(jsonObject, "aDouble"));
    assertIllegalArgumentException( () -> getJsonObjectOrThrow(jsonObject, "aBool"));
    assertIllegalArgumentException( () -> getJsonObjectOrThrow(jsonObject, "anArray"));

    assertIllegalArgumentException( () -> getJsonObjectOrThrow(jsonObject, "missingProperty"));
  }

  @Test
  public void test_getJsonObjectOrEmpty() {
    JsonObject jsonObject1 = jsonObject(
        "n1", jsonInteger(1),
        "text1", jsonString("level1"));
    JsonObject jsonObject0 = singletonJsonObject(
        "key1", jsonObject1);

    assertThat(
        getJsonObjectOrEmpty(jsonObject0, "key1"),
        jsonObjectEpsilonMatcher(jsonObject1));
    // if the property doesn't exist (or if the jsonObject is empty), an empty JsonObject will be returned
    assertThat(
        getJsonObjectOrEmpty(emptyJsonObject(), "emptyJsonHasNoProperties"),
        jsonObjectEpsilonMatcher(emptyJsonObject()));
    assertThat(
        getJsonObjectOrEmpty(jsonObject0, "missingProperty"),
        jsonObjectEpsilonMatcher(emptyJsonObject()));

    // if the value corresponding to the specified property is not a JsonObject, getJsonObjectOrEmpty() will throw
    assertIllegalArgumentException( () -> getJsonObjectOrEmpty(jsonObject1, "n1"));
    assertIllegalArgumentException( () -> getJsonObjectOrEmpty(jsonObject1, "text1"));
  }

  @Test
  public void test_getJsonArrayOrThrow() {
    JsonArray jsonArray1 = jsonArray(ImmutableList.of(
        jsonString("element1"),
        jsonString("element2"),
        jsonString("element3")));
    JsonObject jsonObject0 = jsonObject(
        "key1", jsonArray1,
        "key2", jsonString("notAnArray"),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    assertThat(
        getJsonArrayOrThrow(jsonObject0, "key1"),
        jsonArrayExactMatcher(jsonArray1));

    // if the property doesn't exist (or if the jsonObject is empty), an exception will be thrown
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(emptyJsonObject(), "emptyJsonHasNoProperties"));
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(jsonObject0, "missingProperty"));

    // if the property exists but is not a JsonArray, an exception will be thrown
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(jsonObject0, "key2"));
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(jsonObject0, "subObject"));
  }

  @Test
  public void test_getJsonArrayOrEmpty() {
    JsonArray jsonArray1 = jsonArray(ImmutableList.of(
        jsonString("element1"),
        jsonString("element2"),
        jsonString("element3")));
    JsonObject jsonObject0 = jsonObject(
        "key1", jsonArray1,
        "key2", jsonString("notAnArray"),
        "subObject",  singletonJsonObject("subKey", jsonDouble(7.89)));

    assertThat(
        getJsonArrayOrEmpty(jsonObject0, "key1"),
        jsonArrayExactMatcher(jsonArray1));

    // if the property doesn't exist (or if the jsonObject is empty), an empty JsonArray will be returned
    assertThat(
        getJsonArrayOrEmpty(emptyJsonObject(), "emptyJsonHasNoProperties"),
        jsonArrayExactMatcher(emptyJsonArray()));
    assertThat(
        getJsonArrayOrEmpty(jsonObject0, "missingProperty"),
        jsonArrayExactMatcher(emptyJsonArray()));

    // if the property exists but is not a JsonArray, an exception will be thrown
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(jsonObject0, "key2"));
    assertIllegalArgumentException( () -> getJsonArrayOrThrow(jsonObject0, "subObject"));
  }

  @Test
  public void test_getJsonNestedObjectOrThrow() {
    JsonObject jsonObject4 = singletonJsonObject(
        "n4",   jsonInteger(4));
    JsonObject jsonObject3 = jsonObject(
        "key3",  jsonObject4,
        "n3",    jsonInteger(3),
        "text3", jsonString("level3"));
    JsonObject jsonObject2 = jsonObject(
        "key2",  jsonObject3,
        "n2",    jsonInteger(2),
        "text2", jsonString("level2"));
    JsonObject jsonObject1 = jsonObject(
        "key1", jsonObject2,
        "n1",   jsonInteger(1),
        "text1", jsonString("level1"));
    JsonObject jsonObject0 = jsonObject(
        "key0", jsonObject1,
        "n0",   jsonInteger(0));

    // non-nested version for top level:
    assertThat(
        getJsonObjectOrThrow(jsonObject0, "key0"),
        jsonObjectEpsilonMatcher(jsonObject1));
    // nested versions for lower levels:
    assertThat(
        getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1"),
        jsonObjectEpsilonMatcher(jsonObject2));
    assertThat(
        getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "key2"),
        jsonObjectEpsilonMatcher(jsonObject3));
    assertThat(
        getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "key2", "key3"),
        jsonObjectEpsilonMatcher(jsonObject4));
    assertEquals(
        4,
        getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "key2", "key3").get("n4").getAsInt());

    // wrong order of nesting
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "key1", "key0"));

    // missing properties
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "key0", "missingProperty"));
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "missingProperty"));
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "key2", "missingProperty"));
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "key0", "key1", "key2", "key3", "missingProperty"));
    assertIllegalArgumentException( () -> getNestedJsonObjectOrThrow(jsonObject0, "missingProperty", "key1", "key2"));
  }

  @Test
  public void test_getJsonObjectOrDefault() {
    JsonObject jsonObject = singletonJsonObject("a", singletonJsonObject("xyz", jsonString("123")));
    assertEquals(
        "...",
        getJsonObjectOrDefault(jsonObject, "b", v -> v.get("xyz").getAsString(), "..."));
    assertEquals(
        "123",
        getJsonObjectOrDefault(jsonObject, "a", v -> v.get("xyz").getAsString(), "..."));
  }

  @Test
  public void test_getJsonElementOrDefault() {
    JsonObject jsonObject = singletonJsonObject("a", jsonString("123"));
    assertEquals(
        "...",
        getJsonElementOrDefault(jsonObject, "b", v -> v.getAsString(), "..."));
    assertEquals(
        "123",
        getJsonElementOrDefault(jsonObject, "a", v -> v.getAsString(), "..."));
  }

  @Test
  public void test_getJsonObjectOrDefaultFromSupplier() {
    JsonObject jsonObject = singletonJsonObject("a", singletonJsonObject("xyz", jsonString("123")));
    assertEquals(
        "...",
        getJsonObjectOrDefaultFromSupplier(jsonObject, "b", v -> v.get("xyz").getAsString(), () -> "..."));
    assertEquals(
        "123",
        getJsonObjectOrDefaultFromSupplier(jsonObject, "a", v -> v.get("xyz").getAsString(), () -> "..."));
  }

  @Test
  public void test_getJsonStringOrDefault() {
    JsonObject jsonObject = jsonObject(
        "a",          jsonString("xyz"),
        "notAString", jsonDouble(1.23));
    assertEquals(
        "...",
        getJsonStringOrDefault(jsonObject, "b", v -> Strings.format("_%s_", v), "..."));
    assertEquals(
        "_xyz_",
        getJsonStringOrDefault(jsonObject, "a", v -> Strings.format("_%s_", v), "..."));

    // property "notAString" maps to a double
    assertIllegalArgumentException( () -> getJsonStringOrDefault(jsonObject, "notAString", v -> DUMMY_STRING, DUMMY_STRING));
  }

  @Test
  public void test_getJsonBigDecimalOrDefault() {
    JsonObject jsonObject = jsonObject(
        "a",          jsonDouble(7.7),
        "notANumber", jsonString("xyz"));
    assertAlmostEquals(
        price(8.8),
        getJsonBigDecimalOrDefault(jsonObject, "b", v -> price(v), price(8.8)),
        1e-15);
    assertAlmostEquals(
        price(7.7),
        getJsonBigDecimalOrDefault(jsonObject, "a", v -> price(v), price(8.8)),
        1e-15);

    // value for property "notANumber" is a String
    assertIllegalArgumentException( () -> getJsonBigDecimalOrDefault(jsonObject, "notANumber", v -> DUMMY_PRICE, DUMMY_PRICE));
  }

  @Test
  public void test_getJsonDoubleOrDefault() {
    JsonObject jsonObject = jsonObject(
        "a",          jsonDouble(7.7),
        "notANumber", jsonString("xyz"));
    assertAlmostEquals(
        price(8.8),
        getJsonDoubleOrDefault(jsonObject, "b", v -> price(v), price(8.8)),
        1e-15);
    assertAlmostEquals(
        price(7.7),
        getJsonDoubleOrDefault(jsonObject, "a", v -> price(v), price(8.8)),
        1e-15);

    // value for property "notANumber" is a String
    assertIllegalArgumentException( () -> getJsonDoubleOrDefault(jsonObject, "notANumber", v -> DUMMY_PRICE, DUMMY_PRICE));
  }

  @Test
  public void test_getJsonBooleanOrDefault() {
    JsonObject jsonObject = jsonObject(
        "n",            jsonInteger(123),
        "booleanTrue",  jsonBoolean(true),
        "booleanFalse", jsonBoolean(false));

    assertTrue( getJsonBooleanOrDefault(jsonObject, "booleanTrue", v -> v, false));
    assertFalse(getJsonBooleanOrDefault(jsonObject, "booleanFalse", v -> v, true ));

    assertTrue( getJsonBooleanOrDefault(jsonObject, "missingKey", v -> v, true ));
    assertFalse(getJsonBooleanOrDefault(jsonObject, "missingKey", v -> v, false));

    // property "n" is not a boolean
    assertIllegalArgumentException( () -> getJsonBooleanOrDefault(jsonObject, "n", v -> v, false));

    assertEquals("isTrue",    getJsonBooleanOrDefault(jsonObject, "booleanTrue", v -> v ? "isTrue" : "isFalse", "isDefault"));
    assertEquals("isFalse",   getJsonBooleanOrDefault(jsonObject, "booleanFalse", v -> v ? "isTrue" : "isFalse", "isDefault"));
    assertEquals("isDefault", getJsonBooleanOrDefault(jsonObject, "missingKey", v -> v ? "isTrue" : "isFalse", "isDefault"));
  }

  @Test
  public void test_getJsonPrimitiveOrThrow() {
    JsonObject jsonObject = jsonObject(
        "n",     jsonInteger(10),
        "x",     jsonDouble(1.23),
        "bool",  jsonBoolean(true),
        "text",  jsonString("abc"),
        "null",  JsonNull.INSTANCE,
        "array", jsonArray(jsonString("A"), jsonString("B")),
        "obj",   jsonObject(
            "A", jsonInteger(111),
            "B", jsonInteger(222)));

    assertEquals(
        10,
        getJsonPrimitiveOrThrow(jsonObject, "n").getAsInt());
    assertEquals(
        1.23,
        getJsonPrimitiveOrThrow(jsonObject, "x").getAsDouble());
    assertTrue(
        getJsonPrimitiveOrThrow(jsonObject, "bool").getAsBoolean());
    assertEquals(
        "abc",
        getJsonPrimitiveOrThrow(jsonObject, "text").getAsString());

    assertIllegalArgumentException( () -> getJsonPrimitiveOrThrow(jsonObject, "missingProperty"));

    // "null" is not a JsonPrimitive; it's a JsonNull
    assertThrowsAnyException( () -> getJsonPrimitiveOrThrow(jsonObject, "null"));
    // "array" is not a JsonPrimitive; it's a JsonArray
    assertThrowsAnyException( () -> getJsonPrimitiveOrThrow(jsonObject, "array"));
    // "obj" is not a JsonPrimitive; it's a JsonObject
    assertThrowsAnyException( () -> getJsonPrimitiveOrThrow(jsonObject, "obj"));
  }

}
