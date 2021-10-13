package com.rb.nonbiz.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import org.junit.Test;

import java.util.function.BiConsumer;

import static com.rb.nonbiz.json.JsonElementType.JSON_ARRAY;
import static com.rb.nonbiz.json.JsonElementType.JSON_BOOLEAN;
import static com.rb.nonbiz.json.JsonElementType.JSON_NULL;
import static com.rb.nonbiz.json.JsonElementType.JSON_NUMBER;
import static com.rb.nonbiz.json.JsonElementType.JSON_OBJECT;
import static com.rb.nonbiz.json.JsonElementType.JSON_STRING;
import static com.rb.nonbiz.json.JsonElementTypeTest.jsonElementTypeMatcher;
import static com.rb.nonbiz.json.JsonElementTypes.getJsonElementType;
import static com.rb.nonbiz.json.RBGson.jsonBoolean;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_DOUBLE;
import static com.rb.nonbiz.testutils.RBCommonsTestConstants.DUMMY_STRING;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonElementTypesTest {

  @Test
  public void test_getJsonElementType() {
    BiConsumer<JsonElement, JsonElementType> asserter = (jsonElement, jsonElementType) ->
        assertThat(
            getJsonElementType(jsonElement),
            jsonElementTypeMatcher(jsonElementType));

    asserter.accept(emptyJsonArray(), JSON_ARRAY);
    asserter.accept(singletonJsonArray(jsonDouble(DUMMY_DOUBLE)), JSON_ARRAY);

    asserter.accept(jsonBoolean(true), JSON_BOOLEAN);
    asserter.accept(jsonBoolean(false), JSON_BOOLEAN);

    asserter.accept(JsonNull.INSTANCE, JSON_NULL);

    asserter.accept(jsonDouble(-123),   JSON_NUMBER);
    asserter.accept(jsonDouble(-0.123), JSON_NUMBER);
    asserter.accept(jsonDouble(0),      JSON_NUMBER);
    asserter.accept(jsonDouble(0.123),  JSON_NUMBER);
    asserter.accept(jsonDouble(123),    JSON_NUMBER);

    asserter.accept(jsonInteger(-123),  JSON_NUMBER);
    asserter.accept(jsonInteger(0),     JSON_NUMBER);
    asserter.accept(jsonInteger(123),   JSON_NUMBER);

    asserter.accept(jsonLong(-123),     JSON_NUMBER);
    asserter.accept(jsonLong(0),        JSON_NUMBER);
    asserter.accept(jsonLong(123),      JSON_NUMBER);

    asserter.accept(jsonString(""),     JSON_STRING);
    asserter.accept(jsonString("abc"),  JSON_STRING);

    asserter.accept(emptyJsonObject(),  JSON_OBJECT);
    asserter.accept(singletonJsonObject(DUMMY_STRING, jsonDouble(DUMMY_DOUBLE)), JSON_OBJECT);
  }

}
