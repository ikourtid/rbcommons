package com.rb.nonbiz.json;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.rb.biz.types.Money.money;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.jsonStringArray;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addAllAssumingNoOverlap;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addPreciseValueToJsonObjectIfNonZero;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addStringToJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addToJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addToJsonObjectIf;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addToJsonObjectIfNonEmpty;
import static com.rb.nonbiz.json.RBJsonObjectAdders.addToJsonObjectIfOptionalPresent;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.jsonObject;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.singletonJsonObject;
import static com.rb.nonbiz.testmatchers.RBJsonMatchers.jsonObjectEpsilonMatcher;
import static com.rb.nonbiz.testutils.Asserters.assertIllegalArgumentException;
import static com.rb.nonbiz.types.Epsilon.DEFAULT_EPSILON_1e_8;
import static org.hamcrest.MatcherAssert.assertThat;

public class RBJsonObjectAddersTest {

  @Test
  public void testAddToJsonObjectIf() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    BiFunction<String, Double, JsonObject> adder = (property, value) -> addToJsonObjectIf(
        jsonObject, property, value, v -> Math.abs(v) > 1e-8, v -> jsonDouble(v));

    assertIllegalArgumentException( () -> adder.apply("a", 3.3)); // Property "a" already exists
    assertIllegalArgumentException( () -> adder.apply("a", 0.0)); // Property "a" already exists; problem even if value is non-zero

    assertThat(
        "We will not add a value to property b because it is zero",
        adder.apply("b", 0.0),
        jsonObjectEpsilonMatcher(
            singletonJsonObject("a", jsonDouble(1.1))));
    assertThat(
        adder.apply("b", 7.7),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", jsonDouble(7.7))));
  }

  @Test
  public void testAddPreciseValueToJsonObjectIfNonZero() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    BiFunction<String, Double, JsonObject> adder = (property, value) -> addPreciseValueToJsonObjectIfNonZero(
        jsonObject, property, money(value), DEFAULT_EPSILON_1e_8);

    assertIllegalArgumentException( () -> adder.apply("a", 3.3)); // Property "a" already exists
    assertIllegalArgumentException( () -> adder.apply("a", 0.0)); // Property "a" already exists; problem even if value is non-zero

    assertThat(
        "We will not add a value to property b because it is zero",
        adder.apply("b", 0.0),
        jsonObjectEpsilonMatcher(
            singletonJsonObject("a", jsonDouble(1.1))));
    assertThat(
        adder.apply("b", 7.7),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", jsonDouble(7.7))));
  }

  @Test
  public void testAddToJsonObject() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    assertIllegalArgumentException( () -> addToJsonObject(jsonObject, "a", jsonDouble(1.1)));
    assertIllegalArgumentException( () -> addToJsonObject(jsonObject, "a", jsonDouble(2.2)));
    assertThat(
        addToJsonObject(jsonObject, "b", jsonDouble(2.2)),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", jsonDouble(2.2))));
  }

  @Test
  public void testAddStringToJsonObject() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    assertIllegalArgumentException( () -> addStringToJsonObject(jsonObject, "a", "keyAlreadyPresent"));
    assertThat(
        addStringToJsonObject(jsonObject, "b", "newValue"),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", jsonString("newValue"))));
  }

  @Test
  public void testAddToJsonObjectIfNonEmpty_overloadWithJsonObject() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    assertIllegalArgumentException( () -> addToJsonObjectIfNonEmpty(jsonObject, "a", emptyJsonObject()));
    assertIllegalArgumentException( () -> addToJsonObjectIfNonEmpty(jsonObject, "a", singletonJsonObject("x", jsonDouble(2.2))));

    // does nothing b/c the 'JSON sub-object' (3rd arg) is empty
    addToJsonObjectIfNonEmpty(jsonObject, "b", emptyJsonObject());
    assertThat(
        jsonObject,
        jsonObjectEpsilonMatcher(
            singletonJsonObject("a", jsonDouble(1.1))));

    // We add property "b" because its JsonObject value is not empty.
    addToJsonObjectIfNonEmpty(jsonObject, "b", singletonJsonObject("x", jsonDouble(2.2)));
    assertThat(
        jsonObject,
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", singletonJsonObject("x", jsonDouble(2.2)))));
  }

  @Test
  public void testAddToJsonObjectIfNonEmpty_overloadWithJsonArray() {
    JsonObject jsonObject = singletonJsonObject("a", jsonDouble(1.1));
    assertIllegalArgumentException( () -> addToJsonObjectIfNonEmpty(jsonObject, "a", emptyJsonArray()));
    assertIllegalArgumentException( () -> addToJsonObjectIfNonEmpty(jsonObject, "a", jsonStringArray("x", "y")));

    // does nothing b/c the 'JSON sub-object' (3rd arg) is empty
    addToJsonObjectIfNonEmpty(jsonObject, "b", emptyJsonArray());
    assertThat(
        jsonObject,
        jsonObjectEpsilonMatcher(
            singletonJsonObject("a", jsonDouble(1.1))));

    // We add property "b" because its JsonObject value is not empty.
    addToJsonObjectIfNonEmpty(jsonObject, "b", jsonStringArray("x", "y"));
    assertThat(
        jsonObject,
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonDouble(1.1),
                "b", jsonStringArray("x", "y"))));
  }

  @Test
  public void testAddToJsonObjectIfOptionalPresent() {
    // add a jsonString to an empty JsonObject if the Optional<String> is present
    BiConsumer<Optional<String>, JsonObject> asserter = (maybeJsonElement, expectedJsonObject) ->
        assertThat(
            addToJsonObjectIfOptionalPresent(emptyJsonObject(), "maybeProperty", maybeJsonElement, v -> jsonString(v)),
            jsonObjectEpsilonMatcher(expectedJsonObject));

    asserter.accept(Optional.empty(), emptyJsonObject());
    asserter.accept(Optional.of("present"), singletonJsonObject("maybeProperty", jsonString("present")));

    // it doesn't matter if the string being added is empty, only that Optional<String> is present
    asserter.accept(Optional.of(""), singletonJsonObject("maybeProperty", jsonString("")));

    // can't add a duplicate property, even if the argument is Optional.empty()
    assertIllegalArgumentException( () -> addToJsonObjectIfOptionalPresent(
        singletonJsonObject(
            "existingKey", jsonString("ABC")),
        "existingKey",
        Optional.<String>empty(),
        v -> jsonString(v)));
  }

  @Test
  public void testAddAllAssumingNoOverlap() {
    assertThat(
        addAllAssumingNoOverlap(
            singletonJsonObject("a", "_a"),
            singletonJsonObject("b", "_b")),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonString("_a"),
                "b", jsonString("_b"))));
    assertThat(
        addAllAssumingNoOverlap(
            singletonJsonObject("a", "_a"),
            jsonObject(
                "b", jsonString("_b"),
                "c", jsonString("_c"))),
        jsonObjectEpsilonMatcher(
            jsonObject(
                "a", jsonString("_a"),
                "b", jsonString("_b"),
                "c", jsonString("_c"))));
    assertIllegalArgumentException( () -> addAllAssumingNoOverlap(
        singletonJsonObject("a", "_a"),
        singletonJsonObject("a", "_a")));
    assertIllegalArgumentException( () -> addAllAssumingNoOverlap(
        singletonJsonObject("a", "_a"),
        singletonJsonObject("a", "XYZ")));

    assertIllegalArgumentException( () -> addAllAssumingNoOverlap(
        singletonJsonObject("a", "_a"),
        jsonObject(
            "a", jsonString("XYZ"),
            "c", jsonString("_c"))));
  }

}
