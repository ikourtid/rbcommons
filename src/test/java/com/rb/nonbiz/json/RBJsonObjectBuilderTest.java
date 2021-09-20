package com.rb.nonbiz.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.nonbiz.testutils.RBTestMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import static com.rb.biz.types.SignedMoney.ZERO_SIGNED_MONEY;
import static com.rb.biz.types.SignedMoney.signedMoney;
import static com.rb.nonbiz.json.RBGson.jsonBoolean;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonArraysTest.jsonArrayExactMatcher;
import static com.rb.nonbiz.json.RBJsonObjectBuilder.rbJsonObjectBuilder;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.Match.match;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testmatchers.RBMatchers.makeMatcher;
import static com.rb.nonbiz.testmatchers.RBValueMatchers.doubleAlmostEqualsMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertAlmostEquals;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEmpty;
import static com.rb.nonbiz.testutils.Asserters.assertOptionalEquals;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RBJsonObjectBuilderTest extends RBTestMatcher<RBJsonObjectBuilder> {

  @Test
  public void testBuildAsOptionalOrEmpty() {
    assertOptionalEmpty(rbJsonObjectBuilder().buildAsOptionalOrEmpty());
    assertOptionalEquals(
        singletonJsonObject("a", jsonInteger(1)),
        rbJsonObjectBuilder()
            .setInt("a", 1)
            .buildAsOptionalOrEmpty());
  }

  @Test
  public void duplicateKeys_throw() {
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setInt("anInt", 123)
        .setInt("anInt", 456));
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setString("aString", "abc")
        .setString("aString", "xyz"));
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setLocalDate("date1", LocalDate.of(2010,  1,  1))
        .setLocalDate("date1", LocalDate.of(2010, 12, 31)));

    // same key AND same value throws too
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setInt("anInt", 123)
        .setInt("anInt", 123));
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setString("aString", "abc")
        .setString("aString", "abc"));
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setLocalDate("date1", LocalDate.of(2010, 1, 1))
        .setLocalDate("date1", LocalDate.of(2010, 1, 1)));

    // duplicate VALUES for differing keys do not throw
    RBJsonObjectBuilder doesNotThrow;
    doesNotThrow = rbJsonObjectBuilder()
        .setInt("int1", 123)
        .setInt("int2", 123);
    doesNotThrow = rbJsonObjectBuilder()
        .setString("string1", "abc")
        .setString("string2", "abc");
    doesNotThrow = rbJsonObjectBuilder()
        .setLocalDate("date1", LocalDate.of(2010, 1, 1))
        .setLocalDate("date2", LocalDate.of(2010, 1, 1));

    // can use setBooleanIfTrue() multiple times for the same key if false
    doesNotThrow = rbJsonObjectBuilder()
        .setBooleanIfTrue("falseBoolean", false)
        .setBooleanIfTrue("falseBoolean", false);

    // can use setInNonEmpty() multiple times for the same key if the array is empty
    doesNotThrow = rbJsonObjectBuilder()
        .setArrayIfNonEmpty("emptyArray", emptyJsonArray())
        .setArrayIfNonEmpty("emptyArray", emptyJsonArray());

    // can use setPreciseValueIfNotAlmostZero() multiple times for the same key if value is zero
    doesNotThrow = rbJsonObjectBuilder()
        .setPreciseValueIfNotAlmostZero("zeroSignedMoney", ZERO_SIGNED_MONEY, 1e-8)
        .setPreciseValueIfNotAlmostZero("zeroSignedMoney", ZERO_SIGNED_MONEY, 1e-8);
  }

  @Test
  public void testSimpleContents() {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder()
        .set("jsonElement", jsonInteger(123))
        .setInt("int", 456)
        .setString("string", "abc")
        .setDouble("double", 3.14)
        .setDoublePercentage("doublePct", 0.123)
        .setBoolean("booleanFalse", false)
        .setBoolean("booleanTrue",  true)
        .setLocalDate("date", LocalDate.of(2010, 1, 1));

    assertEquals(
        jsonInteger(123),
        builder.getJsonObject().getAsJsonPrimitive("jsonElement"));
    assertEquals(
        jsonInteger(456),
        builder.getJsonObject().getAsJsonPrimitive("int"));
    assertEquals(
        jsonString("abc"),
        builder.getJsonObject().getAsJsonPrimitive("string"));
    assertThat(
        builder.getJsonObject().getAsJsonPrimitive("double").getAsDouble(),
        doubleAlmostEqualsMatcher(3.14, 1e-8));
    assertThat(
        builder.getJsonObject().getAsJsonPrimitive("doublePct").getAsDouble(),
        doubleAlmostEqualsMatcher(12.3, 1e-8));
    assertFalse(builder.getJsonObject().getAsJsonPrimitive("booleanFalse").getAsBoolean());
    assertTrue( builder.getJsonObject().getAsJsonPrimitive("booleanTrue" ).getAsBoolean());
    assertEquals(
        new JsonPrimitive("2010-01-01"),
        builder.getJsonObject().getAsJsonPrimitive("date"));
  }

  @Test
  public void testSubObject() {
    JsonObject jsonSubObject = jsonObject(
        "int",    jsonInteger(123),
        "string", jsonString("abc"));
    RBJsonObjectBuilder builder = rbJsonObjectBuilder()
        .setJsonSubObject("subObject", jsonSubObject);

    assertThat(
        builder.getJsonObject().getAsJsonObject("subObject"),
        jsonObjectEpsilonMatcher(jsonSubObject));
  }

  @Test
  public void testSubObjectIfPredicate() {
    Function<Integer, JsonObject> maker = n -> rbJsonObjectBuilder()
        .setJsonSubObjectIf("subObject", n, v -> v > 10, v -> jsonObject(
            "int",    jsonInteger(v),
            "string", jsonString("abc")))
        .build();

    assertThat(
        maker.apply(0),
        jsonObjectEpsilonMatcher(emptyJsonObject()));

    assertThat(
        maker.apply(11),
        jsonObjectEpsilonMatcher(singletonJsonObject(
            "subObject", jsonObject(
                "int",    jsonInteger(11),
                "string", jsonString("abc")))));
  }

  // as above, but using the boolean overload instead of the predicate
  @Test
  public void testSubObjectIfBoolean() {
    Function<Integer, JsonObject> maker = n -> rbJsonObjectBuilder()
        .setJsonSubObjectIf("subObject", n > 10, jsonObject(
            "int",    jsonInteger(n),
            "string", jsonString("abc")))
        .build();

    assertThat(
        maker.apply(0),
        jsonObjectEpsilonMatcher(emptyJsonObject()));

    assertThat(
        maker.apply(11),
        jsonObjectEpsilonMatcher(singletonJsonObject(
            "subObject", jsonObject(
                "int",    jsonInteger(11),
                "string", jsonString("abc")))));
  }

  @Test
  public void testSetJsonArray() {
    JsonArray nonEmptyJsonArray = jsonArray(
        jsonInteger(123),
        jsonInteger(456),
        jsonInteger(789));
    JsonArray emptyJsonArray = emptyJsonArray();
    RBJsonObjectBuilder builder = rbJsonObjectBuilder()
        .setArray("nonEmptyArray", nonEmptyJsonArray)
        .setArray("emptyArray",    emptyJsonArray);

    assertTrue(builder.getJsonObject().has("nonEmptyArray"));
    assertTrue(builder.getJsonObject().has("emptyArray"));

    assertThat(
        builder.getJsonObject().getAsJsonArray("nonEmptyArray"),
        jsonArrayExactMatcher(nonEmptyJsonArray));
    assertThat(
        builder.getJsonObject().getAsJsonArray("emptyArray"),
        jsonArrayExactMatcher(emptyJsonArray));
  }

  @Test
  public void testSetIfNonEmptyJsonArray() {
    JsonArray nonEmptyJsonArray = jsonArray(
        jsonInteger(123),
        jsonInteger(456),
        jsonInteger(789));
    JsonArray emptyJsonArray = emptyJsonArray();
    RBJsonObjectBuilder builder = rbJsonObjectBuilder()
        .setArrayIfNonEmpty("nonEmptyArray", nonEmptyJsonArray)
        .setArrayIfNonEmpty("emptyArray",    emptyJsonArray);

    assertTrue(builder.getJsonObject().has("nonEmptyArray"));
    assertFalse(builder.getJsonObject().has("emptyArray")); // the empty array was not added

    assertThat(
        builder.getJsonObject().getAsJsonArray("nonEmptyArray"),
        jsonArrayExactMatcher(nonEmptyJsonArray));
  }

  @Test
  public void testConditionalContents() {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder()
        .setDoubleIfNotAlmostZero("nonZeroDouble", 3.14, 1e-8)
        .setDoubleIfNotAlmostZero("zeroDouble",    1e-9, 1e-8)  // almost zero; 1e-9 < 1e-8
        .setDoublePercentageIfNotAlmostZero("nonZeroDoublePct", 0.0567, 1e-8)
        .setDoublePercentageIfNotAlmostZero("zeroDoublePct", 1e-9, 1e-8)
        .setBooleanIfTrue( "trueBoolean1",  true)
        .setBooleanIfTrue( "falseBoolean1", false)
        .setBooleanIfFalse("trueBoolean2",  true)
        .setBooleanIfFalse("falseBoolean2", false)
        .setBoolean(       "falseBoolean3", false)
        .setIfOptionalPresent("optionalPresent", Optional.of(123), v -> jsonInteger(v))
        .setIfOptionalPresent("optionalEmpty",   Optional.empty(), v -> emptyJsonObject())
        .setIfOptionalIntPresent("optionalIntPresent", OptionalInt.of(456))
        .setIfOptionalIntPresent("optionalIntEmpty",   OptionalInt.empty())
        .setIf("ifEvenPredicate456", 456, v -> v % 2 == 0, v -> jsonInteger(v))  // version with predicate
        .setIf("ifEvenPredicate567", 567, v -> v % 2 == 0, v -> jsonInteger(v))
        .setIf("ifEvenBoolean456",   456, true,            v -> jsonInteger(v))  // can also pass in a Boolean instead of a predicate
        .setIf("ifEvenBoolean567",   567, false,           v -> jsonInteger(v))
        .setPreciseValueIfNotAlmostZero("nonZeroSignedMoney", signedMoney(1.23), 1e-8)
        .setPreciseValueIfNotAlmostZero("zeroSignedMoney", ZERO_SIGNED_MONEY, 1e-8)
        .setIfNonEmpty("nonEmptySubObject", singletonJsonObject("subObject", jsonString("aSubObject")))
        .setIfNonEmpty("emptySubObject",    emptyJsonObject())
        .setArrayIfNonEmpty("nonEmptyArray", jsonArray(jsonString("first"), jsonString("second")))
        .setArrayIfNonEmpty("emptyArray",     emptyJsonArray())
        .setJsonSubObjectIf("subObjectPassesPredicate", 11, v -> v == 11, v -> singletonJsonObject("passes", jsonInteger(v)))
        .setJsonSubObjectIf("subObjectFailsPredicate",  12, v -> v == 11, v -> singletonJsonObject("fails",  jsonInteger(v)))
        .setJsonSubObjectIf("subObjectBooleanTrue",  true,  singletonJsonObject("_true",  jsonInteger(22)))
        .setJsonSubObjectIf("subObjectBooleanFalse", false, singletonJsonObject("_false", jsonInteger(12)));

    assertEquals(
        3.14,
        builder.getJsonObject().getAsJsonPrimitive("nonZeroDouble").getAsDouble(),
        1e-8);
    // using setDoubleIfNotAlmostZero for 0.0 does not add a JsonElement
    assertFalse(builder.getJsonObject().has("zeroDouble"));

    assertEquals(
        5.67,
        builder.getJsonObject().getAsJsonPrimitive("nonZeroDoublePct").getAsDouble(),
        1e-8);
    // using setDoublePercentageIfNotAlmostZero for 1e-9 (with epsilon 1e-8) does not add a JsonElement
    assertFalse(builder.getJsonObject().has("zeroDoublePct"));

    assertEquals(
        jsonBoolean(true),
        builder.getJsonObject().getAsJsonPrimitive("trueBoolean1"));
    // using setBooleanIfTrue for a 'false' does not add a JsonElement
    assertFalse(builder.getJsonObject().has("falseBoolean1"));

    assertEquals(
        jsonBoolean(false),
        builder.getJsonObject().getAsJsonPrimitive("falseBoolean2"));
    // using setBooleanIfFalse for a 'true' does not add a JsonElement
    assertFalse(builder.getJsonObject().has("trueBoolean2"));

    assertEquals(
        jsonInteger(123),
        builder.getJsonObject().getAsJsonPrimitive("optionalPresent"));
    // using setIfOptionalPresent for Optional.empty() does not add a JsonElement
    assertFalse(builder.getJsonObject().has("optionalEmpty"));

    assertEquals(
        jsonInteger(456),
        builder.getJsonObject().getAsJsonPrimitive("optionalIntPresent"));
    // using setIfOptionalIntPresent for Optional.empty() does not add a JsonInteger
    assertFalse(builder.getJsonObject().has("optionalIntEmpty"));

    assertEquals(
        jsonInteger(456),
        builder.getJsonObject().getAsJsonPrimitive("ifEvenPredicate456"));
    // using setIf() for a value with predicate false does not add a JsonElement
    assertFalse(builder.getJsonObject().has("ifEvenPredicate567"));

    assertEquals(
        jsonInteger(456),
        builder.getJsonObject().getAsJsonPrimitive("ifEvenBoolean456"));
    // using setIf() for a value with Boolean false does not add a JsonElement
    assertFalse(builder.getJsonObject().has("ifEvenBoolean567"));

    assertAlmostEquals(
        signedMoney(1.23),
        signedMoney(builder.getJsonObject().getAsJsonPrimitive("nonZeroSignedMoney").getAsDouble()),
        1e-8);
    // using setPreciseValueIfNotAlmostZero for a zero value does not add a JsonElement
    assertFalse(builder.getJsonObject().has("zeroSignedMoney"));

    assertThat(
        builder.getJsonObject().getAsJsonObject("nonEmptySubObject"),
        jsonObjectEpsilonMatcher(singletonJsonObject("subObject", jsonString("aSubObject"))));
    // using setArrayIfNonEmpty using an empty JsonObject will not create a jsonElement
    assertFalse(builder.getJsonObject().has("emptySubObject"));

    assertThat(
        builder.getJsonObject().getAsJsonObject("subObjectPassesPredicate"),
        jsonObjectEpsilonMatcher(singletonJsonObject("passes", jsonInteger(11))));
    assertFalse(builder.getJsonObject().has("subObjectFailsPredicate"));

    assertThat(
        builder.getJsonObject().getAsJsonObject("subObjectBooleanTrue"),
        jsonObjectEpsilonMatcher(singletonJsonObject("_true", jsonInteger(22))));
    assertFalse(builder.getJsonObject().has("subObjectBooleanFalse"));

    // compare the .build() result explicitly to the expected JsonObject
    assertThat(
        builder.build(),
        jsonObjectEpsilonMatcher(rbJsonObjectBuilder()
            .set("nonZeroDouble",      jsonDouble(3.14))
            .set("nonZeroDoublePct",   jsonDouble(5.67))
            .set("trueBoolean1",       jsonBoolean(true))
            .set("falseBoolean2",      jsonBoolean(false))
            .set("falseBoolean3",      jsonBoolean(false))
            .set("optionalPresent",    jsonInteger(123))
            .set("optionalIntPresent", jsonInteger(456))
            .set("ifEvenPredicate456", jsonInteger(456))
            .set("ifEvenBoolean456",   jsonInteger(456))
            .set("nonZeroSignedMoney", jsonDouble(1.23))
            .set("nonEmptySubObject",  singletonJsonObject("subObject", "aSubObject"))
            .set("nonEmptyArray",      jsonArray(jsonString("first"), jsonString("second")))
            .set("subObjectPassesPredicate", singletonJsonObject("passes", jsonInteger(11)))
            .set("subObjectBooleanTrue",     singletonJsonObject("_true",  jsonInteger(22)))
            .build()));
  }

  @Test
  public void testAddNoOverlap() {
    JsonObject jsonObjectBase = jsonObject(
        "int1",    jsonInteger(123),
        "string1", jsonString("abc"));
    JsonObject jsonObjectNoOverlap = jsonObject(
        "int2",    jsonInteger(456),
        "string2", jsonString("xyz"));
    // a JsonObject that shares key 'int1' with jsonObjectBase. Its value for 'int1' does NOT match
    JsonObject jsonObjectWithOverlap1 = jsonObject(
        "int1",    jsonInteger(789),
        "string2", jsonString("xyz"));
    // a JsonObject that shares key 'string1' with jsonObjectBase. Its value for 'string1' does NOT match
    JsonObject jsonObjectWithOverlap2 = jsonObject(
        "int2",    jsonInteger(456),
        "string1", jsonString("pdq"));

    // keys 'int1' overlap
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(jsonObjectBase)
        .setAllAssumingNoOverlap(jsonObjectWithOverlap1));

    // keys 'string1' overlap
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(jsonObjectBase)
        .setAllAssumingNoOverlap(jsonObjectWithOverlap2));

    RBJsonObjectBuilder doesNotThrow;
    doesNotThrow = rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(jsonObjectBase)
        .setAllAssumingNoOverlap(jsonObjectNoOverlap);

    // any non-empty JsonObject overlaps with itself:
    assertIllegalArgumentException( () -> rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(jsonObjectBase)
        .setAllAssumingNoOverlap(jsonObjectBase));

    // can start with an emptyJsonObject and append any JsonObject
    doesNotThrow = rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(emptyJsonObject())
        .setAllAssumingNoOverlap(emptyJsonObject());
    doesNotThrow = rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(emptyJsonObject())
        .setAllAssumingNoOverlap(jsonObjectBase);

    // can append an emptyJsonObject a non-empty JsonObject
    doesNotThrow = rbJsonObjectBuilder()
        .setAllAssumingNoOverlap(jsonObjectBase)
        .setAllAssumingNoOverlap(emptyJsonObject());
  }

  @Override
  public RBJsonObjectBuilder makeTrivialObject() {
    return rbJsonObjectBuilder();
  }

  @Override
  public RBJsonObjectBuilder makeNontrivialObject() {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    builder
        .set("element", jsonInteger(123))
        .setInt("int", 456)
        .setLong("long", 123_456_789L)
        .setString("string", "abc")
        .setDouble("double", 3.14)
        .setDoublePercentage("doublePct", 0.123)
        .setBoolean("falseBoolean", false)
        .setLocalDate("date", LocalDate.of(2010, 4, 4))
        .setBooleanIfTrue("trueBoolean", true)
        .setIfOptionalPresent("optionalPresent", Optional.of(123), v -> jsonInteger(v))
        .setDoubleIfNotAlmostZero("nonZeroDouble", 7.89, 1e-8)
        .setPreciseValueIfNotAlmostZero("nonZero", signedMoney(456.78), 1e-8)
        .setArrayIfNonEmpty("array", jsonStringArray("abc", "def"))
        .setIf("predicateTrue", 789, v -> true, i -> jsonInteger(i))
        .setJsonSubObject("subObject", singletonJsonObject("subObject", jsonDouble(6.78)));
    return builder;
  }

  @Override
  public RBJsonObjectBuilder makeMatchingNontrivialObject() {
    RBJsonObjectBuilder builder = rbJsonObjectBuilder();
    double e = 1e-9; // epsilon
    builder
        .set("element", jsonInteger(123))
        .setInt("int", 456)
        .setLong("long", 123_456_789L)
        .setString("string", "abc")
        .setDouble("double", 3.14 + e)
        .setDoublePercentage("doublePct", 0.123 + e * 0.01)  // epsilon will be scaled up by 100
        .setBoolean("falseBoolean", false)
        .setLocalDate("date", LocalDate.of(2010, 4, 4))
        .setBooleanIfTrue("trueBoolean", true)
        .setIfOptionalPresent("optionalPresent", Optional.of(123), v -> jsonInteger(v))
        .setDoubleIfNotAlmostZero("nonZeroDouble", 7.89 + e, 1e-8)
        .setPreciseValueIfNotAlmostZero("nonZero", signedMoney(456.78 + e), 1e-8)
        .setArrayIfNonEmpty("array", jsonStringArray("abc", "def"))
        .setIf("predicateTrue", 789, v -> true, i -> jsonInteger(i))
        .setJsonSubObject("subObject", singletonJsonObject("subObject", jsonDouble(6.78 + e)));
    return builder;
  }

  @Override
  protected boolean willMatch(RBJsonObjectBuilder expected, RBJsonObjectBuilder actual) {
    return rbJsonObjectBuilderMatcher(expected).matches(actual);
  }

  public static TypeSafeMatcher<RBJsonObjectBuilder> rbJsonObjectBuilderMatcher(RBJsonObjectBuilder expected) {
    return makeMatcher(expected,
        match(v -> v.getJsonObject(), f -> jsonObjectEpsilonMatcher(f)));
  }

}
