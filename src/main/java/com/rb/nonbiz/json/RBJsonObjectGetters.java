package com.rb.nonbiz.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rb.nonbiz.util.RBPreconditions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.primitives.Ints.checkedCast;
import static com.rb.nonbiz.collections.RBOptionalTransformers.transformOptional2;
import static com.rb.nonbiz.collections.RBOptionals.toSpecializedOptionalDouble;
import static com.rb.nonbiz.date.RBDates.dateFromYyyyMmDd;
import static com.rb.nonbiz.json.JsonObjectPath.jsonObjectPath;
import static com.rb.nonbiz.json.RBGson.PERCENTAGE_TO_FRACTION;
import static com.rb.nonbiz.json.RBJsonArrays.emptyJsonArray;
import static com.rb.nonbiz.json.RBJsonArrays.singletonJsonArray;
import static com.rb.nonbiz.json.RBJsonObjectSimpleConstructors.emptyJsonObject;
import static com.rb.nonbiz.types.Epsilon.epsilon;
import static com.rb.nonbiz.types.RBDoubles.getDoubleAsLongAssumingIsRound;

/**
 * Utilities for extracting data from Json objects.
 */
public class RBJsonObjectGetters {

  /**
   * From 'jsonObject', get the value of 'property'.
   * If missing, return empty Optional.
   */
  public static Optional<JsonElement> getOptionalJsonElement(
      JsonObject jsonObject,
      String property) {
    return jsonObject.has(property)
           ? Optional.of(jsonObject.get(property))
           : Optional.empty();
  }

  /**
   * From 'jsonObject', get the value of 'property' if it exists (which we assume to be a JsonPrimitive).
   * If missing, return empty optional.
   */
  public static Optional<JsonPrimitive> getOptionalJsonPrimitive(
      JsonObject jsonObject,
      String property) {
    return jsonObject.has(property)
           ? Optional.of(jsonObject.getAsJsonPrimitive(property))
           : Optional.empty();
  }

  /**
   * From 'jsonObject', get the value of 'property' if it exists (which we assume to be a JsonObject).
   * If missing, return empty optional.
   */
  public static Optional<JsonObject> getOptionalJsonObject(
      JsonObject jsonObject,
      String property) {
    return jsonObject.has(property)
           ? Optional.of(getJsonObjectOrThrow(jsonObject, property))
           : Optional.empty();
  }

  /**
   * From 'jsonObject', get the value of 'property' if it exists (which we assume to be another JsonObject)
   * and return a transformed version of it, otherwise return empty optional.
   */
  // FIXME SWA JSON rename to getOptionalTransformedJsonSubObject?
  public static <T> Optional<T> getOptionalJsonSubObject(
      JsonObject jsonObject,
      String property,
      Function<JsonObject, T> ifPresent) {
    return jsonObject.has(property)
           ? Optional.of(ifPresent.apply(getJsonObjectOrThrow(jsonObject, property)))
           : Optional.empty();
  }

  /**
   * From 'jsonObject', get the value of 'property' if it exists (which we assume to be a JsonPrimitive)
   * and return a transformed version of it, otherwise return empty optional.
   */
  public static <T> Optional<T> getOptionalTransformedJsonPrimitive(
      JsonObject jsonObject,
      String property,
      Function<JsonPrimitive, T> ifPresent) {
    return jsonObject.has(property)
           ? Optional.of(ifPresent.apply(getJsonPrimitiveOrThrow(jsonObject, property)))
           : Optional.empty();
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing, or is not a number, throw an exception.
   * Return as a JsonElement.
   */
  public static JsonElement getJsonNumberElementOrThrow(
      JsonObject jsonObject,
      String property) {
    JsonElement jsonElement = getJsonElementOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber(),
        "JSON object property ( %s ) is not a number: %s : json was %s",
        property, jsonElement, jsonObject);
    return jsonElement.getAsJsonPrimitive();
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing or is not a number, throw an exception.
   * Return as a BigDecimal.
   */
  public static BigDecimal getJsonBigDecimalOrThrow(
      JsonObject jsonObject,
      String property) {
    return getJsonNumberElementOrThrow(jsonObject, property).getAsBigDecimal();
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing or is not a number, throw an exception.
   * Return as a double.
   */
  public static double getJsonDoubleOrThrow(
      JsonObject jsonObject,
      String property) {
    return getJsonNumberElementOrThrow(jsonObject, property).getAsDouble();
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing or is not a number, throw an exception.
   * Convert from a percentage by dividing by 100 and return as a double.
   */
  public static double getJsonDoubleFromPercentageOrThrow(
      JsonObject jsonObject,
      String property) {
    return getJsonNumberElementOrThrow(jsonObject, property).getAsDouble() * 0.01;
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * Convert from a percentage by dividing by 100 and return as a double.
   * If it is missing, return a default.
   */
  public static double getJsonDoubleFromPercentageOrDefault(
      JsonObject jsonObject,
      String property,
      double defaultValue) {
    return jsonObject.has(property)
           ? getJsonNumberElementOrThrow(jsonObject, property).getAsDouble() * 0.01
           : defaultValue;
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing or is not a number, throw an exception.
   * Convert from a percentage by dividing by 100 and return as a BigDecimal.
   */
  public static BigDecimal getJsonBigDecimalFromPercentageOrThrow(
      JsonObject jsonObject,
      String property) {
    return getJsonNumberElementOrThrow(jsonObject, property)
        .getAsBigDecimal()
        .multiply(PERCENTAGE_TO_FRACTION);
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number, and that the number
   * can be cast to an int without changing its value.
   * If missing or is not an int, throw an exception.
   * Return as an int.
   */
  public static int getJsonIntOrThrow(
      JsonObject jsonObject,
      String property) {
    double value = getJsonDoubleOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        value == (int) value,  // check if the value is an integer
        "JSON object property ( %s ) is not an integer: %s",
        property, value);
    return (int) value;
  }

  /**
   * From 'jsonObject', check if 'property' is present.
   * If it is present, check that it is an integer and return it. If it is not an integer, throw an exception.
   * If 'property' is missing, return the default value.
   */
  public static int getJsonIntOrDefault(
      JsonObject jsonObject,
      String property,
      int defaultValue) {
    return jsonObject.has(property)
           ? getJsonIntOrThrow(jsonObject, property)
           : defaultValue;
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number, and that the number
   * can be cast to a long without changing its value.
   * If missing or is not a long, throw an exception.
   * Return as a long.
   */
  public static long getJsonLongOrThrow(
      JsonObject jsonObject,
      String property) {
    double value = getJsonDoubleOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        value == (long) value,  // check if the value is a long
        "JSON object property ( %s ) is not a long: %s",
        property, value);
    return (long) value;
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a string.
   * If missing, or is not a string, throw an exception.
   * Return as a String.
   */
  public static String getJsonStringOrThrow(
      JsonObject jsonObject,
      String property) {
    JsonElement jsonElement = getJsonElementOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString(),
        "JSON object property ( %s ) is not a string: %s",
        property, jsonElement);
    return jsonElement.getAsString();
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a string.
   * If missing, or present but with a value of JsonNull, return empty optional.
   * If present, but not a string, throw an exception.
   * Otherwise, return it as a non-empty optional String.
   */
  public static Optional<String> getOptionalJsonString(
      JsonObject jsonObject,
      String property) {
    return transformOptional2(
        getOptionalJsonElement(jsonObject, property),
        jsonElement -> {
          if (jsonElement.isJsonNull()) {
            return Optional.empty();
          };
          RBPreconditions.checkArgument(
              jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString(),
              "JSON object property ( %s ) is not a string: %s",
              property, jsonElement);
          String asString = jsonElement.getAsString();
          return Optional.of(asString);
        });
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a number.
   * If missing, or present but with a value of JsonNull, return empty optional.
   * If present, but not a number, throw an exception.
   * Otherwise, return it as a non-empty {@link OptionalDouble}.
   */
  public static OptionalDouble getOptionalJsonDouble(
      JsonObject jsonObject,
      String property) {
    Optional<Double> number = transformOptional2(
        getOptionalJsonElement(jsonObject, property),
        jsonElement -> {
          if (jsonElement.isJsonNull()) {
            return Optional.empty();
          }
          RBPreconditions.checkArgument(
              jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isNumber(),
              "JSON object property ( %s ) is not a number: %s",
              property, jsonElement);
          return Optional.of(jsonElement.getAsNumber().doubleValue());
        });
    return toSpecializedOptionalDouble(number);
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is an int.
   * If missing, or present but with a value of JsonNull, return empty optional.
   * If present, but not an int (i.e. either a non-number, or a non-round double), throw an exception.
   * Otherwise, return it as a non-empty {@link OptionalInt}.
   */
  public static OptionalInt getOptionalJsonInt(
      JsonObject jsonObject,
      String property) {
    OptionalDouble optionalDouble = getOptionalJsonDouble(jsonObject, property);
    return !optionalDouble.isPresent()
           ? OptionalInt.empty()
           : OptionalInt.of(checkedCast(getDoubleAsLongAssumingIsRound(optionalDouble.getAsDouble(), epsilon(1e-12))));
  }

  /**
   *  From 'jsonObject', get the value of 'property' and check that it can be converted into a LocalDate
   *  using the format YYYY-MM-DD. If it is missing or cannot be parsed as a date with this format,
   *  throw an exception.
   *  Otherwise, return as a LocalDate.
   */
  public static LocalDate getJsonDateOrThrow(
      JsonObject jsonObject,
      String property) {
    return dateFromYyyyMmDd(getJsonStringOrThrow(jsonObject, property));
  }

  /**
   *  From 'jsonObject', get the value of 'property' and check that it can be converted into a LocalDateTime
   *  using the format "ISO-DATE" + "T" + "ISO-TIME", e.g. 2022-12-31T15:59:59.123456789
   *  where the final NNNNNNNNN is the number of nano-seconds.
   *
   *  <p> This is the format from the UTC standard; we didn't make it up. </p>
   *
   *  <p> If it is missing or cannot be parsed as a date with this format, throw an exception.
   *  Otherwise, return as a LocalDateTime. </p>
   */
  public static LocalDateTime getJsonDateTimeOrThrow(
      JsonObject jsonObject,
      String property) {
    return LocalDateTime.parse(getJsonStringOrThrow(jsonObject, property));
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a JsonObject
   * (a map of property {@code ->} value).
   * If missing, or is not a JsonObject, throw an exception.
   * Return as a JsonObject.
   */
  public static JsonObject getJsonObjectOrThrow(
      JsonObject jsonObject,
      String property) {
    JsonElement jsonElement = getJsonElementOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonObject(),
        "JSON object property ( %s ) is not a JsonObject: %s",
        property, jsonElement);
    return jsonElement.getAsJsonObject();
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If 'property' is missing, return an empty JsonObject.
   * If it is present, verify that it is a JsonObject and return it.
   */
  public static JsonObject getJsonObjectOrEmpty(
      JsonObject jsonObject,
      String property) {
    if (!jsonObject.has(property)) {
      return emptyJsonObject();
    }
    return getJsonObjectOrThrow(jsonObject, property);
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If 'property' is missing or is not a JsonArray, throw an exception.
   * Otherwise, return as a JsonArray.
   */
  public static JsonArray getJsonArrayOrThrow(
      JsonObject jsonObject,
      String property) {
    JsonElement jsonElement = getJsonElementOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonArray(),
        "JSON object property ( %s ) is not a JsonArray: %s",
        property, jsonElement);
    return jsonElement.getAsJsonArray();
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If 'property' is missing, return an empty JsonArray.
   * If it is present, verify that it is a JsonArray and return it.
   */
  public static JsonArray getJsonArrayOrEmpty(
      JsonObject jsonObject,
      String property) {
    if (!jsonObject.has(property)) {
      return emptyJsonArray();
    }
    return getJsonArrayOrThrow(jsonObject, property);
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If 'property' is missing, return a singleton JsonArray consisting of the defaultElement.
   * If 'property' is present, verify that it is a JsonArray and return it.
   */
  public static JsonArray getJsonArrayOrDefaultSingleton(
      JsonObject jsonObject,
      String property,
      JsonElement defaultElement) {
    if (!jsonObject.has(property)) {
      return singletonJsonArray(defaultElement);
    }
    return getJsonArrayOrThrow(jsonObject, property);
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If missing, return empty Optional.
   * If present, return that array, and throw if it's not an array.
   */
  public static Optional<JsonArray> getOptionalJsonArray(
      JsonObject jsonObject,
      String property) {
    return jsonObject.has(property)
        ? Optional.of(jsonObject.get(property).getAsJsonArray())
        : Optional.empty();
  }

  /**
   * Convenience method for use in extracting a "nested" (inner) jsonObject:
   * Use instead of:
   * jsonObject.getAsJsonObject(property1).getAsJsonObject(property2).getAsJsonObject(property3)
   */
  public static JsonObject getNestedJsonObjectOrThrow(
      JsonObject jsonObject,
      String firstProperty,
      String ... restOfProperties) {
    return getNestedJsonObjectOrThrow(jsonObject, jsonObjectPath(firstProperty, restOfProperties));
  }

  public static JsonObject getNestedJsonObjectOrThrow(
      JsonObject jsonObject,
      JsonObjectPath jsonObjectPath) {
    JsonObject innerJsonObject = jsonObject;
    for (String jsonProperty : jsonObjectPath.getJsonProperties()) {
      innerJsonObject = getJsonObjectOrThrow(innerJsonObject, jsonProperty);
    }
    return innerJsonObject;
  }

  /**
   * Extracts a nested / inner {@link JsonObject} when all the properties in its
   * 'path' are present, if you think of the consecutive properties as folders in a filesystem.
   *
   * <p> Returns empty optional if the 'path' is not valid.
   * If the path is valid, but the last {@link JsonElement} is not a {@link JsonObject}, this throws an exception. </p>
   */
  public static Optional<JsonObject> getOptionalNestedJsonObject(
      JsonObject jsonObject,
      String firstProperty,
      String ... restOfProperties) {
    return getOptionalNestedJsonObject(jsonObject, jsonObjectPath(firstProperty, restOfProperties));
  }

  public static Optional<JsonObject> getOptionalNestedJsonObject(
      JsonObject jsonObject,
      JsonObjectPath jsonObjectPath) {
    JsonObject intermediateJsonObject = jsonObject;

    for (String jsonProperty : jsonObjectPath.getJsonProperties()) {
      if (!intermediateJsonObject.has(jsonProperty)) {
        return Optional.empty();
      }
      JsonElement nestedJsonElement = intermediateJsonObject.get(jsonProperty);
      RBPreconditions.checkArgument(
          nestedJsonElement.isJsonObject(),
          "Nested JsonElement under property %s is not a JsonObject: %s",
          jsonProperty, nestedJsonElement);
      intermediateJsonObject = nestedJsonElement.getAsJsonObject();
    }
    // If by this point we haven't exited, the nested JsonObject exists.
    return Optional.of(intermediateJsonObject);
  }

  /**
   * From 'jsonObject', get the value of 'property' and check that it is a boolean.
   * If missing, or is not a boolean, throw an exception.
   * Return as a boolean.
   */
  public static boolean getJsonBooleanOrThrow(
      JsonObject jsonObject,
      String property) {
    JsonElement jsonElement = getJsonElementOrThrow(jsonObject, property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isBoolean(),
        "JSON object property ( %s ) is not a boolean: %s",
        property, jsonElement);
    return jsonElement.getAsBoolean();
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be another JsonObject)
   * and return a transformed value of the JsonObject, or (if missing) the supplied default value.
   */
  public static <T> T getJsonObjectOrDefault(
      JsonObject jsonObject,
      String property,
      Function<JsonObject, T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonObjectOrThrow(jsonObject, property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which can be any JsonElement, not just a JsonObject)
   * and return a transformed value of the JsonElement, or (if missing) the supplied default value.
   */
  public static <T> T getJsonElementOrDefault(
      JsonObject jsonObject,
      String property,
      Function<JsonElement, T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(jsonObject.get(property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be another JsonObject)
   * and return a transformed value of the JsonObject, or (if missing) a value from a supplier.
   */
  public static <T> T getJsonObjectOrDefaultFromSupplier(
      JsonObject jsonObject,
      String property,
      Function<JsonObject, T> ifPresent,
      Supplier<T> ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonObjectOrThrow(jsonObject, property))
           : ifAbsent.get();
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be a String JsonPrimitive)
   * and return a String, or (if missing) the supplied default String.
   */
  public static String getJsonStringOrDefault(
      JsonObject jsonObject,
      String property,
      String ifAbsent) {
    return jsonObject.has(property)
           ? getJsonStringOrThrow(jsonObject, property)
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be a String JsonPrimitive)
   * and return a transformed value of the String, or (if missing) the supplied default value.
   */
  public static <T> T getJsonStringOrDefault(
      JsonObject jsonObject,
      String property,
      Function<String, T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonStringOrThrow(jsonObject, property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be a BigDecimal JsonPrimitive)
   * and return a transformed value of the BigDecimal, or (if missing) the supplied default value.
   */
  public static <T> T getJsonBigDecimalOrDefault(
      JsonObject jsonObject,
      String property,
      Function<BigDecimal, T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonBigDecimalOrThrow(jsonObject, property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be a double JsonPrimitive)
   * and return a transformed value of the double, or (if missing) the supplied default value.
   */
  public static <T> T getJsonDoubleOrDefault(
      JsonObject jsonObject,
      String property,
      DoubleFunction<T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonDoubleOrThrow(jsonObject, property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property' (which we assume to be a boolean JsonPrimitive)
   * and return a transformation of the boolean, or (if missing) the supplied default value.
   */
  public static <T> T getJsonBooleanOrDefault(
      JsonObject jsonObject,
      String property,
      Function<Boolean, T> ifPresent,
      T ifAbsent) {
    return jsonObject.has(property)
           ? ifPresent.apply(getJsonBooleanOrThrow(jsonObject, property))
           : ifAbsent;
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If missing, throw an exception.
   * Return as a JsonElement.
   */
  public static JsonElement getJsonElementOrThrow(
      JsonObject jsonObject,
      String property) {
    RBPreconditions.checkArgument(
        jsonObject.has(property),
        "JSON object does not have property: %s : json was %s",
        property, jsonObject);
    return jsonObject.get(property);
  }

  /**
   * From 'jsonObject', get the value of 'property'.
   * If missing, throw an exception.
   * Return as a JsonPrimitive.
   */
  public static JsonPrimitive getJsonPrimitiveOrThrow(
      JsonObject jsonObject,
      String property) {
    RBPreconditions.checkArgument(
        jsonObject.has(property),
        "JSON object does not have property: %s : json was %s",
        property, jsonObject);
    JsonElement jsonElement = jsonObject.get(property);
    RBPreconditions.checkArgument(
        jsonElement.isJsonPrimitive(),
        "jsonElement %s is not a jsonPrimitive",
        jsonElement);
    return jsonElement.getAsJsonPrimitive();
  }

}
