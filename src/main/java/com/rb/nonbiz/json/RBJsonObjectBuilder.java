package com.rb.nonbiz.json;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.nonbiz.types.Epsilon;
import com.rb.nonbiz.types.ImpreciseValue;
import com.rb.nonbiz.types.PreciseValue;
import com.rb.nonbiz.types.RBNumeric;
import com.rb.nonbiz.types.UnitFraction;
import com.rb.nonbiz.util.JsonRoundTripStringConvertibleEnum;
import com.rb.nonbiz.util.RBBuilder;
import com.rb.nonbiz.util.RBPreconditions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.rb.nonbiz.json.RBGson.jsonBigDecimal;
import static com.rb.nonbiz.json.RBGson.jsonBoolean;
import static com.rb.nonbiz.json.RBGson.jsonDate;
import static com.rb.nonbiz.json.RBGson.jsonDouble;
import static com.rb.nonbiz.json.RBGson.jsonInteger;
import static com.rb.nonbiz.json.RBGson.jsonLong;
import static com.rb.nonbiz.json.RBGson.jsonString;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;

/**
 * A nice, fluent way to build a JsonObject, with functionality for conditional addition of property/value pairs.
 */
public class RBJsonObjectBuilder implements RBBuilder<JsonObject> {

  private final JsonObject jsonObject;

  private RBJsonObjectBuilder(JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public static RBJsonObjectBuilder rbJsonObjectBuilder() {
    return new RBJsonObjectBuilder(emptyJsonObject());
  }

  public static RBJsonObjectBuilder rbJsonObjectBuilderWithInitialJson(JsonObject initialJsonObject) {
    return new RBJsonObjectBuilder(initialJsonObject);
  }

  /**
   * Use the more specific setters, if you know more details about the JsonElement (e.g. is it a string? int?)
   */
  public RBJsonObjectBuilder setJsonElement(String property, JsonElement jsonElement) {
    jsonObject.add(checkPropertyNotAlreadySet(property), jsonElement);
    return this;
  }

  public RBJsonObjectBuilder setJsonPrimitive(String property, JsonPrimitive jsonPrimitive) {
    return setJsonElement(property, jsonPrimitive);
  }

  public RBJsonObjectBuilder setString(String property, String value) {
    return setJsonPrimitive(property, jsonString(value));
  }

  public <E extends Enum<E>, T extends JsonRoundTripStringConvertibleEnum<E>> RBJsonObjectBuilder setEnum(
      String property, T enumValue) {
    return setString(property, enumValue.toUniqueStableString());
  }

  public RBJsonObjectBuilder setInt(String property, int value) {
    return setJsonPrimitive(property, jsonInteger(value));
  }

  public RBJsonObjectBuilder setLong(String property, long value) {
    return setJsonPrimitive(property, jsonLong(value));
  }

  public RBJsonObjectBuilder setDouble(String property, double value) {
    return setJsonPrimitive(property, jsonDouble(value));
  }

  public RBJsonObjectBuilder setDoublePercentage(String property, double value) {
    return setJsonPrimitive(property, jsonDouble(value * 100));
  }

  public RBJsonObjectBuilder setDoublePercentage(String property, UnitFraction unitFraction) {
    return setJsonPrimitive(property, jsonDouble(unitFraction.doubleValue() * 100));
  }

  public <N extends RBNumeric<N>> RBJsonObjectBuilder setDoublePercentage(String property, N rbNumeric) {
    return setJsonPrimitive(property, jsonDouble(rbNumeric.doubleValue() * 100));
  }

  public RBJsonObjectBuilder setBoolean(String property, Boolean value) {
    return setJsonPrimitive(property, jsonBoolean(value));
  }

  public RBJsonObjectBuilder setLocalDate(String property, LocalDate value) {
    return setJsonPrimitive(property, jsonDate(value));
  }

  public RBJsonObjectBuilder setLocalDateTime(String property, LocalDateTime value) {
    return setJsonPrimitive(property, jsonString(value.toString()));
  }

  public RBJsonObjectBuilder setJsonSubObject(String property, JsonObject jsonSubObject) {
    return setJsonElement(property, jsonSubObject);
  }

  public RBJsonObjectBuilder setJsonArray(String property, JsonArray jsonArray) {
    return setJsonElement(property, jsonArray);
  }

  public RBJsonObjectBuilder setJsonSubObjectIfNonEmpty(String property, Optional<JsonObject> jsonSubObject) {
    checkPropertyNotAlreadySet(property);
    jsonSubObject.ifPresent(v -> jsonObject.add(property, v));
    return this;
  }

  public <T> RBJsonObjectBuilder setJsonSubObjectIf(
      String property, T value, Predicate<T> onlyIncludeIf, Function<T, JsonObject> valueSerializer) {
    if (onlyIncludeIf.test(value)) {
      setJsonSubObject(property, valueSerializer.apply(value));
    }
    return this;
  }

  public RBJsonObjectBuilder setJsonSubObjectIf(
      String property, boolean onlyIncludeIf, JsonObject jsonSubObject) {
    if (onlyIncludeIf) {
      setJsonSubObject(property, jsonSubObject);
    }
    return this;
  }

  public RBJsonObjectBuilder setJsonSubArrayIfNonEmpty(String property, Optional<JsonArray> jsonSubArray) {
    checkPropertyNotAlreadySet(property);
    jsonSubArray.ifPresent(v -> jsonObject.add(property, v));
    return this;
  }

  public RBJsonObjectBuilder setAllAssumingNoOverlap(JsonObject source) {
    source.entrySet().forEach(entry -> {
      String property = entry.getKey();
      JsonElement value = entry.getValue();
      jsonObject.add(checkPropertyNotAlreadySet(property), value);
    });
    return this;
  }

  /**
   * Adds { property : jsonSubObject } to jsonObject if jsonSubObject is non-empty.
   * Throws if property already exists in jsonObject.
   */
  public RBJsonObjectBuilder setIfNonEmpty(String property, JsonObject jsonSubObject) {
    RBPreconditions.checkArgument(
        !jsonObject.has(property),
        "Json already has property '%s' so we cannot add jsonSubObject '%s': json was %s",
        property, jsonSubObject, jsonObject);
    if (!jsonSubObject.entrySet().isEmpty()) {
      jsonObject.add(property, jsonSubObject);
    }
    return this;
  }

  /**
   * Adds { property : jsonSubArray } to jsonObject.
   * Throws if property already exists in jsonObject.
   *
   * This is very simple but it allows us to add stuff fluently, which makes the code shorter.
   */
  public RBJsonObjectBuilder setArray(String property, JsonArray jsonSubArray) {
    checkPropertyNotAlreadySet(property);
    jsonObject.add(property, jsonSubArray);
    return this;
  }

  /**
   * Adds { property : jsonSubArray } to jsonObject if jsonSubArray is non-empty.
   * Throws if property already exists in jsonObject.
   */
  public RBJsonObjectBuilder setArrayIfNonEmpty(String property, JsonArray jsonSubArray) {
    checkPropertyNotAlreadySet(property);
    if (jsonSubArray.size() > 0) {
      jsonObject.add(property, jsonSubArray);
    }
    return this;
  }

  /**
   * Adds { property : jsonElement } to jsonObject if {@code Optional<T>} is present.
   * Throws if 'property' already exists in jsonObject.
   */
  public <T> RBJsonObjectBuilder setIfOptionalPresent(
      String property, Optional<T> maybeValue, Function<T, JsonElement> valueSerializer) {
    checkPropertyNotAlreadySet(property);
    maybeValue.ifPresent(v ->
        jsonObject.add(property, valueSerializer.apply(v)));
    return this;
  }

  /**
   * Adds { property : jsonElement } to jsonObject if {@code Optional<T>} is present
   * and passes a predicate.
   * Throws if 'property' already exists in jsonObject.
   */
  public <T> RBJsonObjectBuilder setIfOptionalPresent(
      String property, Optional<T> maybeValue, Predicate<T> onlyIncludeIf, Function<T, JsonElement> valueSerializer) {
    checkPropertyNotAlreadySet(property);
    maybeValue.ifPresent(v -> {
      if (onlyIncludeIf.test(v)) {
        jsonObject.add(property, valueSerializer.apply(v));
      }
    });
    return this;
  }

  /**
   * Adds { property : jsonInteger } to jsonObject if OptionalInt is present.
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setIfOptionalIntPresent(String property, OptionalInt maybeValue) {
    checkPropertyNotAlreadySet(property);
    maybeValue.ifPresent(v ->
        jsonObject.add(property, jsonInteger(v)));
    return this;
  }

  /**
   * Adds { property : jsonDouble } to jsonObject if OptionalDouble is present.
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setIfOptionalDoublePresent(String property, OptionalDouble maybeValue) {
    checkPropertyNotAlreadySet(property);
    maybeValue.ifPresent(v ->
        jsonObject.add(property, jsonDouble(v)));
    return this;
  }

  /**
   * Adds { property : jsonElement } to jsonObject if onlyIncludeIf is true.
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setIf(
      String property, boolean onlyIncludeIf, JsonElement jsonElement) {
    checkPropertyNotAlreadySet(property);
    if (onlyIncludeIf) {
      jsonObject.add(property, jsonElement);
    }
    return this;
  }

  /**
   * Adds { property : jsonElement } to jsonObject if T passes the predicate.
   * Throws if 'property' already exists in jsonObject.
   */
  public <T> RBJsonObjectBuilder setIf(
      String property, T value, Predicate<T> onlyIncludeIf, Function<T, JsonElement> valueSerializer) {
    checkPropertyNotAlreadySet(property);
    if (onlyIncludeIf.test(value)) {
      jsonObject.add(property, valueSerializer.apply(value));
    }
    return this;
  }

  /**
   * Adds { property : jsonElement } to jsonObject if onlyIncludeIf is true.
   * Throws if 'property' already exists in jsonObject.
   */
  public <T> RBJsonObjectBuilder setIf(
      String property, T value, boolean onlyIncludeIf, Function<T, JsonElement> valueSerializer) {
    checkPropertyNotAlreadySet(property);
    if (onlyIncludeIf) {
      jsonObject.add(property, valueSerializer.apply(value));
    }
    return this;
  }

  /**
   * Adds { property : jsonBoolean(value) } to jsonObject if value is true.
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setBooleanIfTrue(String property, boolean value) {
    checkPropertyNotAlreadySet(property);
    if (value) {
      setBoolean(property, value);
    }
    return this;
  }

  /**
   * Adds { property : jsonBoolean(value) } to jsonObject if value is false.
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setBooleanIfFalse(String property, boolean value) {
    checkPropertyNotAlreadySet(property);
    if (!value) {
      setBoolean(property, value);
    }
    return this;
  }

  /**
   * Adds { property : jsonDouble(value) } to jsonObject if value is not zero (to within epsilon).
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setDoubleIfNotAlmostZero(String property, double value, Epsilon epsilon) {
    return setIf(property, value, v -> !epsilon.isAlmostZero(v), v -> jsonDouble(v));
  }

  /**
   * Adds { property : jsonDouble(value * 100) } to jsonObject if value is not zero (to within epsilon).
   * Throws if 'property' already exists in jsonObject.
   */
  public RBJsonObjectBuilder setDoublePercentageIfNotAlmostZero(String property, double value, Epsilon epsilon) {
    return setIf(property, value, v -> !epsilon.isAlmostZero(v), v -> jsonDouble(v * 100));
  }

  /**
   * Adds { property : jsonDouble } to jsonObject.
   * Throws if 'property' already exists in jsonObject.
   */
  public <P extends PreciseValue<? super P>> RBJsonObjectBuilder setPreciseValue(String property, P value) {
    return setJsonElement(property, jsonBigDecimal(value));
  }

  /**
   * Adds { property : jsonDouble } to jsonObject.
   * Throws if 'property' already exists in jsonObject.
   */
  public <P extends ImpreciseValue<? super P>> RBJsonObjectBuilder setImpreciseValue(String property, P value) {
    return setJsonElement(property, jsonDouble(value));
  }

  /**
   * Adds { property : jsonDouble } to jsonObject if this PreciseValue is non-zero (subject to an epsilon).
   * Throws if 'property' already exists in jsonObject.
   */
  public <P extends PreciseValue<? super P>> RBJsonObjectBuilder setPreciseValueIfNotAlmostZero(
      String property, P value, Epsilon epsilon) {
    return setIf(property, value, v -> !v.isAlmostZero(epsilon), v -> jsonDouble(v));
  }

  @Override
  public void sanityCheckContents() {
    // Nothing to sanity-check here; if we call none of the setter methods,
    // we will just end up building an empty JSON object.
  }

  @Override
  public JsonObject buildWithoutPreconditions() {
    return jsonObject;
  }

  private String checkPropertyNotAlreadySet(String property) {
    RBPreconditions.checkArgument(
        !jsonObject.has(property),
        "Json object already has property '%s': json object was %s",
        property, jsonObject);
    return property;
  }

  /**
   * Returns Optional.of(the JSON object in this builder), or Optional.empty if the jsonObject is empty.
   *
   * This is not part of the RBBuilder interface, because not all objects have a concept of being empty,
   * like JsonObject does.
   */
  public Optional<JsonObject> buildAsOptionalOrEmpty() {
    sanityCheckContents();
    return jsonObject.size() == 0
        ? Optional.empty()
        : Optional.of(jsonObject);
  }

  // Don't use this; it's here to help the test matcher
  @VisibleForTesting
  public JsonObject getJsonObject() {
    return jsonObject;
  }

}
